package org.mule.extension.kafka.api.source;

import java.io.Serializable;

public class ConsumerContext implements Serializable {
  private String consumerCommitKey;

  public ConsumerContext() {

  }

  public ConsumerContext(String consumerCommitKey) {
    this.consumerCommitKey = consumerCommitKey;
  }

  public String getConsumerCommitKey() {
    return this.consumerCommitKey;
  }

  public void setConsumerCommitKey(String consumerCommitKey) {
    this.consumerCommitKey = consumerCommitKey;
  }
}
