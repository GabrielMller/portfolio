package org.mule.extension.kafka.internal.connection;

import java.io.InputStream;
import java.time.Duration;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.Map.Entry;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.message.ErrorType;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.sdk.api.connectivity.ConnectionValidationResult;
import org.mule.sdk.api.runtime.operation.FlowListener;
import org.mule.sdk.api.runtime.operation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mule.extension.kafka.api.KafkaRecordAttributes;
import org.mule.extension.kafka.api.source.AckMode;
import org.mule.extension.kafka.api.source.TopicPartition;
import org.mule.extension.kafka.internal.PollingTask;
import org.mule.extension.kafka.internal.error.KafkaErrorType;
import org.mule.extension.kafka.internal.error.exception.InvalidAckModeException;
import org.mule.extension.kafka.internal.error.exception.InvalidOffsetException;
import org.mule.extension.kafka.internal.error.exception.InvalidTopicNameException;
import org.mule.extension.kafka.internal.error.exception.NegativeDurationException;
import org.mule.extension.kafka.internal.error.exception.NotFoundException;
import org.mule.extension.kafka.internal.error.exception.OperationInterruptedException;
import org.mule.extension.kafka.internal.error.exception.OperationTimeoutException;
import org.mule.extension.kafka.internal.error.exception.TimeoutTooLargeException;
import org.mule.extension.kafka.internal.error.exception.UnassignedConsumerException;
import org.mule.extension.kafka.internal.error.exception.UnexpectedException;
import org.mule.extension.kafka.internal.error.exception.SessionNotFoundException;
import org.mule.extension.kafka.internal.model.Session;
import org.mule.extension.kafka.internal.model.consumer.ConsumerPool;
import org.mule.extension.kafka.internal.model.consumer.ConsumerPoolClosedException;
import org.mule.extension.kafka.internal.model.consumer.MuleConsumer;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.InterruptException;
import org.apache.kafka.common.errors.InvalidTopicException;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthenticationException;
import org.apache.kafka.common.errors.TimeoutException;


public class ConsumerConnection {
  private static final Logger logger = LoggerFactory.getLogger(ConsumerConnection.class);
  private final ConsumerPool consumerPool;
  private final Scheduler workerScheduler;
  private final Map<String, Session<MuleConsumer>> openSessions = new ConcurrentHashMap<>();

  public ConsumerConnection(ConsumerPool consumerPool, Scheduler workerScheduler) {
    this.consumerPool = consumerPool;
    this.workerScheduler = workerScheduler;
  }

  public void seek(String topic, int partition, long offset, Duration timeout) throws ConnectionException {
    try {
      MuleConsumer consumer = this.consumerPool.checkOut(topic, partition, timeout);

      Session<MuleConsumer> session = new Session<>(consumer, this.consumerPool::checkIn);
      try {
        session.run((muleConsumer) -> {
          muleConsumer.seek(topic, partition, offset);
        });
      } catch (Throwable t) {
        throw t;
      } finally {
        try {
          session.close();
        } catch (Throwable t) {
          logger.warn("Failed to close session after seeking consumer for topic {} and partition {}", topic, partition,
              t);
        }
      }

    } catch (ConsumerPoolClosedException ex) {
      throw new ConnectionException("Failed to seek consumer for topic " + topic + " and partition " + partition
          + " because the consumer pool is closed.", ex);
    } catch (IllegalArgumentException ex) {
      throw new InvalidOffsetException("Failed to seek consumer for topic " + topic + " and partition " + partition
          + " because no consumer is assigned to the specified topic and partition.", ex);
    } catch (IllegalStateException ex) {
      throw new NotFoundException("Failed to seek consumer for topic " + topic + " and partition " + partition
          + " because the consumer pool is invalid.", ex);
    }
  }

  public Result<InputStream, KafkaRecordAttributes> consume(
      BiFunction<String, ConsumerRecord<InputStream, InputStream>, Result<InputStream, KafkaRecordAttributes>> parser,
      Duration pollTimeout, Duration operationTimeout, AckMode ackMode, FlowListener flowListener)
      throws ConnectionException {
    Session<MuleConsumer> session = this.createSession(operationTimeout);
    AtomicReference<ConsumerRecord<InputStream, InputStream>> result = new AtomicReference<>();

    try {
      return session.apply((muleConsumer) -> {
        try {
          result.set(muleConsumer.singleElementPoll(pollTimeout));
          return parser.apply(this.consumeResultByAckMode(ackMode, flowListener, session, result, muleConsumer),
              result.get());
        } catch (org.apache.kafka.clients.consumer.InvalidOffsetException ex) {
          throw new InvalidOffsetException(ex);
        } catch (AuthorizationException ex) {
          throw new org.mule.extension.kafka.internal.error.exception.AuthorizationException(ex);
        } catch (AuthenticationException ex) {
          throw new org.mule.extension.kafka.internal.error.exception.AuthenticationException(ex);
        } catch (IllegalArgumentException ex) {
          throw new NegativeDurationException(pollTimeout, ex);
        } catch (IllegalStateException ex) {
          throw new UnassignedConsumerException(ex);
        } catch (ArithmeticException ex) {
          throw new TimeoutTooLargeException(pollTimeout, ex);
        } catch (CommitFailedException ex) {
          throw new org.mule.extension.kafka.internal.error.exception.CommitFailedException(
              ex.getMessage(), KafkaErrorType.COMMIT_FAILED, ex.getCause());
        } catch (InvalidTopicException ex) {
          throw new InvalidTopicNameException(ex);
        } catch (InterruptException ex) {
          throw new OperationInterruptedException(ex);
        } catch (KafkaException ex) {
          throw new UnexpectedException(ex);
        }
      });
    } catch (org.mule.extension.kafka.internal.error.exception.CommitFailedException t) {
      session.close();
      throw new ConnectionException(t, this);
    } finally {
      if (result.get() == null || AckMode.IMMEDIATE == ackMode || AckMode.DUPS_OK == ackMode) {
        session.close();
      }
    }
  }

  private Session<MuleConsumer> createSession(Duration timeout) throws ConnectionException {
    try {
      MuleConsumer consumer = this.consumerPool.checkOut(timeout);
      Session<MuleConsumer> session = new Session<>(consumer, this.consumerPool::checkIn);
      return session;
    } catch (ConsumerPoolClosedException ex) {
      throw new ConnectionException(ex, this);
    }
  }

  private String consumeResultByAckMode(AckMode ackMode, FlowListener flowListener, Session<MuleConsumer> session,
      AtomicReference<ConsumerRecord<InputStream, InputStream>> result, MuleConsumer consumer) {
    if (result.get() != null) {
      switch (ackMode) {
        case AUTO:
          flowListener.onSuccess((s) -> {
            consumer.commit();
            session.close();
          });
          flowListener.onError((e) -> {
            consumer.resetBuffer();
            session.close();
          });
          break;
        case IMMEDIATE:
          consumer.commit();
          break;
        case DUPS_OK:
          consumer.asyncCommit();
          break;
        case MANUAL:
          String sessionId = session.getId().toString();
          this.addSession(ackMode, sessionId, session);
          flowListener.onError((e) -> {
            consumer.resetBuffer();
            this.removeSession(ackMode, sessionId);
          });
          flowListener.onComplete(() -> {
            session.close();
          });
          break;
      }
    }

    return session.getId().toString();
  }

  private void addSession(AckMode ackMode, String sessionId, Session<MuleConsumer> session) {
    this.openSessions.put(String.format("%s-%s", ackMode, sessionId), session);
  }

  private void removeSession(AckMode ackMode, String sessionId) {
    this.openSessions.remove(String.format("%s-%s", ackMode, sessionId));
  }

  public void commit(AckMode ackMode, String sessionId) throws ConnectionException {
    try {
      this.getSession(ackMode, sessionId).run(MuleConsumer::commit);
    } catch (AuthenticationException ex) {
      throw new org.mule.extension.kafka.internal.error.exception.AuthenticationException(ex);
    } catch (AuthorizationException ex) {
      throw new org.mule.extension.kafka.internal.error.exception.AuthorizationException(ex);
    } catch (TimeoutException ex) {
      logger.warn("The commit timeout for seesionId {}", sessionId);
      throw new UnexpectedException(ex);
    } catch (CommitFailedException ex) {
      throw new org.mule.extension.kafka.internal.error.exception.CommitFailedException(ex.getMessage(),
          KafkaErrorType.COMMIT_FAILED, ex.getCause());
    } catch (InterruptException ex) {
      throw new OperationInterruptedException(ex);
    } catch (KafkaException | IllegalArgumentException ex) {
      logger.debug("There was an unexpected exception while doing a commit of the sessionId {}", sessionId);
      throw new UnexpectedException(ex);
    }
  }

  public void subscribe(Duration timeout, List<String> topicPatterns) throws ConnectionException {
    try {
      Session<Set<MuleConsumer>> session = new Session<Set<MuleConsumer>>(this.consumerPool.checkoutAll(timeout),
          (consumerSet) -> {
            consumerSet.stream().forEach(this.consumerPool::checkIn);
          });
      try {
        session.run((consumers) -> {
          consumers.stream().forEach((consumer) -> {
            consumer.subscribe(topicPatterns);
          });
        });
      } catch (Throwable t) {
        throw t;
      } finally {
        try {
          session.close();
        } catch (Throwable t) {
          logger.warn("Failed to close session after subscribing consumer to topics {}", topicPatterns, t);
        }
      }
    } catch (AuthorizationException ex) {
      throw new org.mule.extension.kafka.internal.error.exception.AuthorizationException(ex);
    } catch (AuthenticationException ex) {
      throw new org.mule.extension.kafka.internal.error.exception.AuthenticationException(ex);
    } catch (TimeoutException ex) {
      throw new OperationTimeoutException("subscribe", ex);
    } catch (ConsumerPoolClosedException ex) {
      throw new ConnectionException("The consumer Pool is closed, can't subscribe", ex, (ErrorType) null, this);
    }
  }

  public void assign(Duration timeout, List<TopicPartition> partitions) throws ConnectionException {
    try {
      Session<Set<MuleConsumer>> session = new Session<Set<MuleConsumer>>(this.consumerPool.checkoutAll(timeout),
          (consumerSet) -> {
            consumerSet.stream().forEach(this.consumerPool::checkIn);
          });
      try {
        session.run((consumers) -> {
          Iterator<List<TopicPartition>> assignmentIterator = this.dividePartitions(partitions, consumers.size())
              .iterator();
          consumers.stream().forEach((consumer) -> {
            consumer.assign((List<TopicPartition>) assignmentIterator.next());
          });
        });
      } catch (Throwable t) {
        throw t;
      } finally {
        try {
          session.close();
        } catch (Throwable t) {
          logger.warn("Failed to close session after assigning consumer to partitions {}", partitions, t);
        }
      }
    } catch (AuthorizationException ex) {
      throw new org.mule.extension.kafka.internal.error.exception.AuthorizationException(ex);
    } catch (AuthenticationException ex) {
      throw new org.mule.extension.kafka.internal.error.exception.AuthenticationException(ex);
    } catch (TimeoutException ex) {
      throw new OperationTimeoutException("assignment", ex);
    } catch (ConsumerPoolClosedException ex) {
      throw new ConnectionException("The consumer Pool was closed when trying to execute the assign operation",
          ex, (ErrorType) null, this);
    }
  }

  private Session<MuleConsumer> getSession(AckMode ackMode, String sessionId) {
    if (sessionId == null || sessionId.isEmpty()) {
      throw new SessionNotFoundException(ackMode, sessionId);
    } else {
      return this.openSessions.keySet().stream().filter((key) -> {
        return key.endsWith(sessionId);
      }).peek((key) -> {
        if (!key.startsWith(ackMode.name())) {
          throw new InvalidAckModeException(ackMode);
        }
      }).findFirst().map(this.openSessions::get).orElseThrow(() -> {
        return new SessionNotFoundException(ackMode, sessionId);
      });
    }
  }

  public Future<?> startPolling(PollingTask<?, ?, ?> pollingTask) {
    return this.workerScheduler.submit(pollingTask);
  }

  public void disconnect() {
    IOUtils.closeQuietly(this.consumerPool);
  }

  public <T> Entry<String, T> poll(AckMode ackMode, Duration pollTimeout,
      BiFunction<MuleConsumer, Duration, T> pollOperation) throws ConnectionException {
    Session<MuleConsumer> session = this.createSession(Duration.ofMillis(-1L));
    return session.apply((consumer) -> {
      try {
        return this.pollByAckMode(ackMode, session, consumer, pollOperation.apply(consumer, pollTimeout));
      } catch (AuthorizationException ex) {
        throw (org.mule.extension.kafka.internal.error.exception.AuthorizationException) this
            .ensureClosedSession(
                new org.mule.extension.kafka.internal.error.exception.AuthorizationException(ex),
                session);
      } catch (AuthenticationException ex) {
        throw (org.mule.extension.kafka.internal.error.exception.AuthenticationException) this
            .ensureClosedSession(
                new org.mule.extension.kafka.internal.error.exception.AuthenticationException(ex),
                session);
      } catch (org.apache.kafka.clients.consumer.InvalidOffsetException ex) {
        throw (InvalidOffsetException) this.ensureClosedSession(new InvalidOffsetException(ex), session);
      } catch (InvalidTopicException ex) {
        throw (org.mule.extension.kafka.internal.error.exception.InvalidTopicException) this
            .ensureClosedSession(new org.mule.extension.kafka.internal.error.exception.InvalidTopicException(
                "An invalid topic name was provided ", ex), session);
      } catch (IllegalStateException var11) {
        throw (UnassignedConsumerException) this.ensureClosedSession(new UnassignedConsumerException(var11),
            session);
      } catch (NotFoundException var12) {
        logger.trace("No messages found.");
        return this.ensureClosedSession(null, session);
      } catch (InterruptException var13) {
        throw new OperationInterruptedException(var13);
      } catch (RuntimeException var14) {
        throw (RuntimeException) this.ensureClosedSession(var14, session);
      }
    });
  }

  private <T, E> E ensureClosedSession(E object, Session<T> session) {
    session.close();
    return object;
  }

  private <T> SimpleEntry<String, T> pollByAckMode(AckMode ackMode, Session<MuleConsumer> session,
      MuleConsumer muleConsumer, T result) {
    if (result != null) {
      String sessionKey = null;
      switch (ackMode) {
        case AUTO:
        case MANUAL:
          sessionKey = session.getId().toString();
          this.addSession(ackMode, sessionKey, session);
          break;
        case IMMEDIATE:
          muleConsumer.commit();
          session.close();
          break;
        case DUPS_OK:
          muleConsumer.asyncCommit();
          session.close();
          break;
      }
      return new SimpleEntry<>(sessionKey, result);
    } else {
      session.close();
      return new SimpleEntry<>(null, null);
    }
  }

  private List<List<TopicPartition>> dividePartitions(List<TopicPartition> assignments, int size) {
    List<List<TopicPartition>> results = new ArrayList<>();
    IntStream.range(0, size).forEach((ix) -> {
      results.add(new ArrayList<>());
    });
    Iterator<TopicPartition> assignmentsIterator = assignments.iterator();

    while (assignmentsIterator.hasNext()) {
      for (int i = 0; assignmentsIterator.hasNext() && i < size; ++i) {
        ((List<TopicPartition>) results.get(i)).add(assignmentsIterator.next());
      }
    }

    return results;
  }

  public void release(AckMode ackMode, String sessionId) {
    this.getSession(ackMode, sessionId).close();
    this.removeSession(ackMode, sessionId);
  }

  public void refreshBuffer(AckMode ackMode, String sessionId) {
    Session<MuleConsumer> session = this.getSession(ackMode, sessionId);
    session.apply((sessionElement) -> {
      sessionElement.resetBuffer();
      return null;
    });
  }

  public ConnectionValidationResult validateWithResult() {
    return !this.consumerPool.isValid()
        ? ConnectionValidationResult.failure("Invalid Connection",
            new ConnectionException("Invalid Connection", (Throwable) null, (ErrorType) null, this))
        : ConnectionValidationResult.success();
  }

}
