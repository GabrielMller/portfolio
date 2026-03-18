package org.mule.extension.kafka.api.source;

import java.io.Serializable;

import org.mule.sdk.api.annotation.param.Parameter;

public class TopicPartition implements Serializable {
  @Parameter
  private String topic;
  @Parameter
  private int partition;

  public TopicPartition() {
  }

  public TopicPartition(String topic, int partition) {
    this.topic = topic;
    this.partition = partition;
  }

  public String getTopic() {
    return this.topic;
  }

  public int getPartition() {
    return this.partition;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public void setPartition(int partition) {
    this.partition = partition;
  }
}
