package org.mule.extension.kafka.internal.model.consumer;

import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.RetriableException;
import org.mule.extension.kafka.api.source.TopicPartition;
import org.mule.extension.kafka.internal.error.KafkaErrorType;
import org.mule.extension.kafka.internal.error.exception.CommitFailedException;
import org.mule.extension.kafka.internal.error.exception.InvalidInputException;
import org.mule.extension.kafka.internal.error.exception.NoPollException;
import org.mule.extension.kafka.internal.error.exception.NotFoundException;
import org.mule.extension.kafka.internal.model.TopicPartitionDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultMuleConsumer implements MuleConsumer {
  private static final Logger logger = LoggerFactory.getLogger(DefaultMuleConsumer.class);
  private final UUID id = UUID.randomUUID();
  private final Properties properties;
  private final long maxPollTimeout;
  private long lastPollTime = 0L;
  private Consumer<InputStream, InputStream> consumer;
  private List<ConsumerRecord<InputStream, InputStream>> bufRecords = Collections.synchronizedList(new ArrayList<>());
  private List<ConsumerRecord<InputStream, InputStream>> inFlightRecords;
  private Semaphore commitSemaphore = new Semaphore(1);
  private DefaultConsumerPool pool;
  private AtomicBoolean isStopping = new AtomicBoolean(false);

  public DefaultMuleConsumer(Function<Properties, Consumer<InputStream, InputStream>> consumerFactory,
      Properties properties) {
    this.properties = properties;
    this.maxPollTimeout = Long.parseLong((String) properties.get("max.poll.interval.ms"));
    this.consumer = consumerFactory.apply(this.getProperties());
  }

  public UUID getId() {
    return this.id;
  }

  public void setPool(DefaultConsumerPool pool) {
    this.pool = pool;
  }

  private Properties getProperties() {
    return this.properties;
  }

  public void assign(List<TopicPartition> assignments) {
    this.consumer.unsubscribe();
    this.consumer.assign(assignments.stream().map((each) -> {
      return new org.apache.kafka.common.TopicPartition(each.getTopic(), each.getPartition());
    }).collect(Collectors.toList()));
  }

  public void subscribe(List<String> topicPatterns) {
    this.consumer.unsubscribe();
    try {
      Pattern singlePattern = Pattern.compile((String) topicPatterns.stream().peek((eachTopicPattern) -> {
        logger.info("Subscribing to topic pattern '{}'.", eachTopicPattern);
      }).map(Pattern::compile).map(Pattern::pattern).collect(Collectors.joining("|")));
      this.consumer.subscribe(singlePattern, new ConsumerRebalanceListener() {
        public void onPartitionsRevoked(Collection<org.apache.kafka.common.TopicPartition> collection) {
          DefaultMuleConsumer.logger.warn("Partitions revoked: {}", collection);
        }

        public void onPartitionsAssigned(Collection<org.apache.kafka.common.TopicPartition> collection) {
          DefaultMuleConsumer.logger.warn("Partitions assigned: {}", collection);
        }
      });
    } catch (PatternSyntaxException e) {
      throw new InvalidInputException("Failed to subscribe to topic pattern.", e);
    }
  }

  public Set<TopicPartition> assignment() {
    Set<org.apache.kafka.common.TopicPartition> assignments = this.consumer.assignment();
    return assignments.stream().map((topicPartition) -> {
      return new TopicPartition(topicPartition.topic(), topicPartition.partition());
    }).peek((description) -> {
      logger.trace("Found description: {}", description);
    }).collect(Collectors.toSet());
  }

  public Set<TopicPartitionDescription> assignmentDescription() {
    return this.consumer.assignment().stream().map((topicPartition) -> {
      return new TopicPartitionDescription(topicPartition.topic(), topicPartition.partition(),
          this.consumer.position(topicPartition),
          this.consumer.beginningOffsets(Collections.singletonList(topicPartition)).get(topicPartition),
          this.consumer.endOffsets(Collections.singletonList(topicPartition)).get(topicPartition));
    }).peek((description) -> {
      logger.trace("Found description: {}", description);
    }).collect(Collectors.toSet());
  }

  public void seek(String topic, int partition, long offset) {
    this.consumer.seek(new org.apache.kafka.common.TopicPartition(topic, partition), offset);
    List<ConsumerRecord<InputStream, InputStream>> remainingRecords = this.bufRecords.stream().filter((record) -> {
      return !record.topic().equals(topic) || record.partition() != partition;
    }).collect(Collectors.toList());
    this.bufRecords.clear();
    this.bufRecords.addAll(remainingRecords);
  }

  public List<ConsumerRecord<InputStream, InputStream>> poll(Duration timeout) {
    if (this.isStopping.get()) {
      logger.debug("The consumer is stopping, canceling poll");
      return null;
    } else {
      this.inFlightRecords = this.getRecords(timeout).peek(this::logRecord)
          .collect(Collectors.toList());
      return !this.inFlightRecords.isEmpty() ? this.inFlightRecords : null;
    }
  }

  private void logRecord(ConsumerRecord<InputStream, InputStream> record) {
    if (logger.isTraceEnabled()) {
      logger.trace("Message key: {}", Optional.ofNullable(record.key()).map((v) -> v.toString()).orElse(" no key"));
      logger.trace("Message value: {}",
          Optional.ofNullable(record.value()).map((v) -> v.toString()).orElse(" no value"));
      logger.trace("Message topic: {}", Optional.ofNullable(record.topic()).orElse(" no topic"));
      logger.trace("Message partition: {}",
          Optional.ofNullable(record.partition()).map(String::valueOf).orElse(" no partition"));
      logger.trace("Message offset: {}",
          Optional.ofNullable(record.offset()).map(String::valueOf).orElse(" no offset"));
    }

  }

  public ConsumerRecord<InputStream, InputStream> singleElementPoll(Duration timeout) {
    if (this.isStopping.get()) {
      logger.debug("The consumer is stopping, single element poll cancel poll operation");
      return null;
    } else {
      this.inFlightRecords = Stream.of(this.getRecords(timeout).findFirst().orElseThrow(NotFoundException::new))
          .peek(this::logRecord).collect(Collectors.toList());
      return !this.inFlightRecords.isEmpty() ? this.inFlightRecords.get(0) : null;
    }
  }

  private void throwIfMultipleCommits(boolean throwException) {
    if (throwException) {
      if (logger.isDebugEnabled()) {
        logger.debug("Failed to commit because the commit operation is already made by another thread.");
      }

      throw new CommitFailedException(
          "Failed to commit because the commit operation is already made by another thread.",
          KafkaErrorType.ALREADY_COMMITED);
    }
  }

  private Stream<ConsumerRecord<InputStream, InputStream>> getRecords(Duration timeout) {
    boolean isMaxPollOverdue = this.maxPollTimeout < System.currentTimeMillis() - this.lastPollTime;
    if (isMaxPollOverdue || this.bufRecords.isEmpty()) {
      this.bufRecords.clear();
      this.consumer.poll(timeout).forEach((message) -> {
        this.bufRecords.add(new KafkaConsumerRecord<>(message));
      });
      this.lastPollTime = System.currentTimeMillis();
      logger.trace("Retrieved {} records.", this.bufRecords.size());
      if (this.bufRecords.isEmpty()) {
        throw new NotFoundException("No message was found when executing the poll on KafkaConsumer");
      }
    }

    return this.bufRecords.stream();
  }

  public void commit() {
    try {
      this.throwIfMultipleCommits(!this.commitSemaphore.tryAcquire());
      Map<org.apache.kafka.common.TopicPartition, OffsetAndMetadata> commits = new HashMap<>();
      this.validatePreviousPollReturnedResults();
      this.inFlightRecords.forEach((record) -> {
        OffsetAndMetadata om = commits.put(
            new org.apache.kafka.common.TopicPartition(record.topic(), record.partition()),
            new OffsetAndMetadata(record.offset() + 1L));
      });
      this.consumer.commitSync(commits);
      this.throwIfMultipleCommits(!this.bufRecords.removeAll(this.inFlightRecords));
    } catch (KafkaException ex) {
      this.bufRecords.clear();
      this.inFlightRecords.clear();
      this.pool.invalidate();
      throw ex;
    } finally {
      this.commitSemaphore.release();
    }

  }

  public void asyncCommit() {
    Map<org.apache.kafka.common.TopicPartition, OffsetAndMetadata> commits = new HashMap<>();
    this.validatePreviousPollReturnedResults();
    this.inFlightRecords.forEach((record) -> {
      OffsetAndMetadata om = commits.put(new org.apache.kafka.common.TopicPartition(record.topic(), record.partition()),
          new OffsetAndMetadata(record.offset() + 1L));
    });
    this.commitAsync(commits);
    this.bufRecords.removeAll(this.inFlightRecords);
  }

  private void commitAsync(Map<org.apache.kafka.common.TopicPartition, OffsetAndMetadata> commits) {
    this.consumer.commitAsync(commits, (offsets, exception) -> {
      if (exception != null) {
        if (exception instanceof RetriableException) {
          logger.debug(
              "A retriable exception happened during the execution. The next execution will re-try committing the processed messages.");
        } else {
          logger.error("An unrecoverable exception happened during the asyncCommit.");
          this.bufRecords.clear();
          this.inFlightRecords.clear();
          this.pool.invalidate();
        }
      }

    });
  }

  public void resetBuffer() {
    Map<String, Map<Integer, Long>> resetPartitionsMap = new HashMap<>();
    List<ConsumerRecord<InputStream, InputStream>> alreadyCommitedMessages = new ArrayList<>();
    this.inFlightRecords.forEach((record) -> {
      if (this.bufRecords.contains(record)) {
        Map<Integer, Long> topicPartitionsMap = resetPartitionsMap.get(record.topic());
        if (topicPartitionsMap == null) {
          Map<Integer, Long> valueMap = new HashMap<>();
          valueMap.put(record.partition(), record.offset());
          resetPartitionsMap.put(record.topic(), valueMap);
        } else if (topicPartitionsMap.get(record.partition()) == null
            || (Long) topicPartitionsMap.get(record.partition()) > record.offset()) {
          topicPartitionsMap.put(record.partition(), record.offset());
        }
      } else {
        alreadyCommitedMessages.add(record);
      }
    });
    this.inFlightRecords.removeAll(alreadyCommitedMessages);
    resetPartitionsMap.entrySet().forEach((entry) -> {
      (entry.getValue()).entrySet().forEach((partitionEntry) -> {
        this.seek((String) entry.getKey(), (Integer) partitionEntry.getKey(), (Long) partitionEntry.getValue());
      });
    });
  }

  public void close() {
    this.consumer.close();
  }

  public void setStopping() {
    this.isStopping.compareAndSet(false, true);
  }

  protected void validatePreviousPollReturnedResults() {
    if (this.inFlightRecords == null || this.inFlightRecords.isEmpty()) {
      throw new NoPollException("There is no previous poll to commit");
    }
  }
}
