package org.mule.extension.kafka.api.source;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Objects;

import org.mule.extension.kafka.api.KafkaRecordAttributes;
import org.mule.extension.kafka.internal.model.serializer.SerByteArray;

public class Record implements Serializable {
   private transient InputStream payload;
   private KafkaRecordAttributes attributes;
   private SerByteArray serByteArray;

   public void setPayload(InputStream payload) {
      this.payload = payload;
   }

   public void setAttributes(KafkaRecordAttributes attributes) {
      this.attributes = attributes;
   }

   public SerByteArray getSerByteArray() {
      return this.serByteArray;
   }

   public void setSerByteArray(SerByteArray serByteArray) {
      this.serByteArray = serByteArray;
   }

   public Record() {
   }

   public Record(InputStream payload, KafkaRecordAttributes attributes) {
      this.attributes = attributes;
      this.payload = payload;
   }

   public Record(InputStream payload, KafkaRecordAttributes attributes, SerByteArray serByteArray) {
      this.payload = payload;
      this.attributes = attributes;
      this.serByteArray = serByteArray;
   }

   public InputStream getPayload() {
      return this.payload != null ? this.payload : (this.payload = this.serByteArray.getInputStream());
   }

   public KafkaRecordAttributes getAttributes() {
      return this.attributes;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Record record = (Record)o;
         return Objects.equals(this.payload, record.payload) && Objects.equals(this.attributes, record.attributes);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.payload, this.attributes});
   }
}
