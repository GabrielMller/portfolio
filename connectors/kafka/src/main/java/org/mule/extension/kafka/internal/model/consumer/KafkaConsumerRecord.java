package org.mule.extension.kafka.internal.model.consumer;

import java.io.InputStream;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.mule.extension.kafka.api.ByteArrayInputStreamWrapper;
import org.mule.runtime.api.util.IOUtils;

public class KafkaConsumerRecord<K, V> extends ConsumerRecord<K, V> {
   private V value;

   public KafkaConsumerRecord(ConsumerRecord<K, V> message) {
      super(message.topic(), message.partition(), message.offset(), message.timestamp(), message.timestampType(), message.serializedKeySize(), message.serializedValueSize(), message.key(), message.value(), message.headers(), message.leaderEpoch());
      this.value = this.getRepeatableValue(message.value());
   }

   public V value() {
      return this.value != null ? this.getRepeatableValue(this.value) : null;
   }

   private V getRepeatableValue(V value) {
      if (value instanceof InputStream) {
         ByteArrayInputStreamWrapper inputStreamWrapper = new ByteArrayInputStreamWrapper(IOUtils.toByteArray((InputStream)value));
         this.value = (V) inputStreamWrapper.getKey();
         return (V) inputStreamWrapper.getKey();
      } else {
         return value;
      }
   }
}
