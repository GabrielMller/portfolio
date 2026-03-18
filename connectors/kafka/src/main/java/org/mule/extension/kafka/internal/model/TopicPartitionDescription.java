package org.mule.extension.kafka.internal.model;

public class TopicPartitionDescription {
   private String topic;
   private int partition;
   private long offset;
   private long initialOffset;
   private long latestOffset;

   public TopicPartitionDescription() {
   }

   public TopicPartitionDescription(String topic, int partition, long offset, long initialOffset, long latestOffset) {
      this.topic = topic;
      this.partition = partition;
      this.offset = offset;
      this.initialOffset = initialOffset;
      this.latestOffset = latestOffset;
   }

   public String getTopic() {
      return this.topic;
   }

   public int getPartition() {
      return this.partition;
   }

   public long getOffset() {
      return this.offset;
   }

   public long getInitialOffset() {
      return this.initialOffset;
   }

   public long getLatestOffset() {
      return this.latestOffset;
   }

}

