package org.mule.extension.kafka.internal.model.consumer;

import java.io.Closeable;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.mule.extension.kafka.api.source.TopicPartition;
import org.mule.extension.kafka.internal.model.TopicPartitionDescription;

public interface MuleConsumer extends Closeable {
   UUID getId();

   void assign(List<TopicPartition> topicPartitionPairs);

   void subscribe(List<String> partitionPatterns);

   Set<TopicPartition> assignment();

   Set<TopicPartitionDescription> assignmentDescription();

   void seek(String topic, int partition, long offset);

   List<ConsumerRecord<InputStream, InputStream>> poll(Duration timeout);

   ConsumerRecord<InputStream, InputStream> singleElementPoll(Duration timeout);

   void commit();

   void asyncCommit();

   void resetBuffer();

   void setPool(DefaultConsumerPool pool);

   void setStopping();
}