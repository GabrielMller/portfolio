package org.mule.extension.kafka.api.operation;

import java.io.Serializable;
import java.util.Objects;

public class ResponseMetadata implements Serializable{

  private String topic;
  private int partition;
  private long offset;

  public ResponseMetadata() {

  }

  public ResponseMetadata(String topic, int partition, long offset) {
    this.topic = topic;
    this.partition = partition;
    this.offset = offset;
  }

  public String getTopic() {
    return topic;
  }

  public int getPartition() {
    return partition;
  }

  public long getOffset() {
    return offset;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public void setPartition(int partition) {
    this.partition = partition;
  }

  public void setOffset(long offset) {
    this.offset = offset;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ResponseMetadata that = (ResponseMetadata) o;

    if (partition != that.partition) return false;
    if (offset != that.offset) return false;
    return topic != null ? topic.equals(that.topic) : that.topic == null;
  }

  public int hashCode() {
    return Objects.hash(topic, partition, offset);
  }
}
