package org.mule.extension.kafka.api.connection.providers;

public enum ProducerAck {
  NONE(0),
  LEADER_ONLY (1),
  ALL (-1);

  public final int ackValue;

  ProducerAck(int ackValue) {
    this.ackValue = ackValue;
  }
}
