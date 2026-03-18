package org.mule.extension.kafka.internal.model.consumer;

import org.mule.extension.kafka.internal.error.exception.NotFoundException;
import org.mule.extension.kafka.internal.error.exception.OperationInterruptedException;
import org.mule.extension.kafka.internal.error.exception.OperationTimeoutException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConsumerPool implements ConsumerPool {
  private static final Logger logger = LoggerFactory.getLogger(DefaultConsumerPool.class);
  private Map<MuleConsumer, Semaphore> consumerMap;
  private List<Entry<MuleConsumer, Semaphore>> consumerIndexes;
  private AtomicInteger currentPosition = new AtomicInteger();
  private AtomicBoolean isValid = new AtomicBoolean(true);
  private Semaphore consumerAvailabilitySemaphore;

  public DefaultConsumerPool(Set<MuleConsumer> consumerSet) {
    this.consumerMap = consumerSet.stream().peek((consumer) -> {
      consumer.setPool(this);
    }).collect(Collectors.toMap(Function.identity(), (consumer) -> {
      return new Semaphore(1, true);
    }));
    this.consumerAvailabilitySemaphore = new Semaphore(consumerSet.size(), true);
    this.consumerIndexes = new ArrayList<>(this.consumerMap.entrySet());
  }

  public MuleConsumer checkOut(Duration timeout) throws ConsumerPoolClosedException {
    Optional<MuleConsumer> muleConsumer = this.checkOut(true, timeout);
    if (!muleConsumer.isPresent()) {
      muleConsumer = this.checkOut(false, timeout);
    }

    return muleConsumer.orElseThrow(() -> {
      return new OperationTimeoutException(String.format("Unable to checkout a consumer from the consumer using timeout of %d ms", timeout.toMillis()), timeout.toMillis());
    });
  }

  public MuleConsumer checkOut(String topic, int partition, Duration timeout) throws ConsumerPoolClosedException {
    Optional<MuleConsumer> muleConsumer = this.checkOutConsumer(Optional.of((consumer) -> {
      return consumer.assignment().stream().anyMatch((assignment) -> {
        return assignment.getTopic().equals(topic) && assignment.getPartition() == partition;
      });
    }), timeout);
    MuleConsumer result = (MuleConsumer) muleConsumer.orElseThrow(() -> {
       return new NotFoundException(String.format("There is no consumer for the topic: %s and partition %d", topic, partition));
    });
    this.checkPoolIsValid(muleConsumer);
    return result;
  }

  public Set<MuleConsumer> checkoutAll(Duration timeout) throws ConsumerPoolClosedException {
    long startTime = System.currentTimeMillis();
    long finishTime = startTime + timeout.toMillis();
    List<Semaphore> acquiredSemaphores = new ArrayList<>(this.consumerMap.size());
    int consumerAvailabilitySemaphoreAcquired = 0;

    try {
      
      Iterator<Entry<MuleConsumer, Semaphore>> it = this.consumerMap.entrySet().iterator();

      while (it.hasNext()) {
        Entry<MuleConsumer, Semaphore> consumerEntry = it.next();
        this.acquireSemaphore(this.consumerAvailabilitySemaphore, timeout, true);
        ++consumerAvailabilitySemaphoreAcquired;
        Semaphore semaphore = (Semaphore) consumerEntry.getValue();
        if (!timeout.isNegative() && !timeout.isZero()) {
          long remainig = finishTime - System.currentTimeMillis();
          if (!(consumerEntry.getValue()).tryAcquire(remainig, TimeUnit.MILLISECONDS)) {
            throw new OperationTimeoutException(timeout.toMillis());
          }
        } else {
          semaphore.acquire();
        }
        acquiredSemaphores.add(semaphore);
      }

      Set<MuleConsumer> muleConsumers = this.consumerMap.keySet();
      this.checkPoolIsValid(muleConsumers);
      return muleConsumers;
    } catch (InterruptedException ex) {
      acquiredSemaphores.stream().forEach(Semaphore::release);
      this.consumerAvailabilitySemaphore.release(consumerAvailabilitySemaphoreAcquired);
      throw new OperationInterruptedException(ex);
    } catch (RuntimeException ex) {
      acquiredSemaphores.stream().forEach(Semaphore::release);
      this.consumerAvailabilitySemaphore.release(consumerAvailabilitySemaphoreAcquired);
      throw ex;
    }
  }

  public void checkIn(MuleConsumer consumer) {
    this.consumerMap.get(consumer).release();
    this.consumerAvailabilitySemaphore.release();
  }

  public boolean isValid() {
    return this.isValid.get();
  }

  public void invalidate() {
    this.isValid.set(false);
  }

  public void close() {
    try {
      this.consumerMap.keySet().forEach(MuleConsumer::setStopping);
      Set<MuleConsumer> muleConsumers = this.checkoutAll(Duration.ofMillis(-1L));
      muleConsumers.forEach(IOUtils::closeQuietly);
      muleConsumers.forEach(this::checkIn);
    } catch (ConsumerPoolClosedException ex) {
      throw new MuleRuntimeException(ex);
    }

    this.invalidate();
  }

  private Optional<MuleConsumer> checkOutConsumer(Optional<Predicate<MuleConsumer>> consumerFilter, Duration timeout)
      throws ConsumerPoolClosedException {
    Optional<MuleConsumer> result = Optional.empty();
    boolean acquirePermitForConsumerAvailabilitySemaphore = false;

    try {
      this.acquireSemaphore(this.consumerAvailabilitySemaphore, timeout, true);

      for (int i = 0; i < this.consumerIndexes.size(); ++i) {
        Entry<MuleConsumer, Semaphore> consumerEntry = this.consumerIndexes.get(i);
        boolean semaphoreAcquired = false;
        MuleConsumer consumer = consumerEntry.getKey();
        Semaphore semaphore = consumerEntry.getValue();
        semaphoreAcquired = this.acquireSemaphore(semaphore, timeout, true);
        if (consumerFilter.isPresent() && !(consumerFilter.get()).test(consumer)) {
          semaphore.release();
          semaphoreAcquired = false;
        }

        if (semaphoreAcquired) {
          result = Optional.of(consumer);
          break;
        }
      }

    } catch (InterruptedException ex) {
      if (acquirePermitForConsumerAvailabilitySemaphore) {
        this.consumerAvailabilitySemaphore.release();
      }

      throw new OperationInterruptedException(ex);
    } catch (RuntimeException ex) {
      if (acquirePermitForConsumerAvailabilitySemaphore) {
        this.consumerAvailabilitySemaphore.release();
      }

      throw ex;
    }

    this.checkPoolIsValid(result);
    return result;
  }

  private Optional<MuleConsumer> checkOut(boolean checkSemaphoreAvailablePermits, Duration timeout)
      throws ConsumerPoolClosedException {
    Optional<MuleConsumer> result = Optional.empty();
    boolean acquirePermitForConsumerAvailabilitySemaphore = false;

    try {
      this.acquireSemaphore(this.consumerAvailabilitySemaphore, timeout, true);

      while (true) {
        if (result.isPresent() || !this.isValid.get()) {
          break;
        }

        Entry<MuleConsumer, Semaphore> consumerEntry = this.consumerIndexes
            .get(this.currentPosition.getAndUpdate((currentValue) -> {
              return (currentValue + 1) % this.consumerMap.size();
            }));
        MuleConsumer consumer =  consumerEntry.getKey();
        Semaphore semaphore = consumerEntry.getValue();
        boolean shouldReturnConsumer = !checkSemaphoreAvailablePermits
            || (consumerEntry.getValue()).availablePermits() != 0;
        if (shouldReturnConsumer && this.acquireSemaphore(semaphore, timeout, false)) {
          result = Optional.of(consumer);
        }
      }
    } catch (InterruptedException ex) {
      if (acquirePermitForConsumerAvailabilitySemaphore) {
        this.consumerAvailabilitySemaphore.release();
      }

      throw new OperationInterruptedException(ex);
    }

    this.checkPoolIsValid(result);
    return result;
  }

  public void checkPoolIsValid(Set<MuleConsumer> muleConsumers) throws ConsumerPoolClosedException {
    if (!this.isValid()) {
      muleConsumers.forEach(this::checkIn);
      throw new ConsumerPoolClosedException();
    }
  }

  public void checkPoolIsValid(Optional<MuleConsumer> result) throws ConsumerPoolClosedException {
    if (!this.isValid()) {
      result.ifPresent(this::checkIn);
      if (!result.isPresent()) {
        this.consumerAvailabilitySemaphore.release();
      }

      throw new ConsumerPoolClosedException();
    } else {
      if (!result.isPresent()) {
        this.consumerAvailabilitySemaphore.release();
      }

    }
  }

  private boolean acquireSemaphore(Semaphore semaphore, Duration timeout, boolean forceAcquire)
      throws InterruptedException {
    if (!timeout.isNegative() && !timeout.isZero()) {
      if (!semaphore.tryAcquire(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
        throw new OperationTimeoutException(timeout.toMillis());
      } else {
        return true;
      }
    } else {
      return forceAcquire ? this.acquire(semaphore) : semaphore.tryAcquire();
    }
  }

  private boolean acquire(Semaphore semaphore) throws InterruptedException {
    semaphore.acquire();
    return true;
  }
}
