package org.mule.extension.kafka.api;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Map;

import org.mule.extension.kafka.api.source.ConsumerContext;

public class KafkaRecordAttributes extends ConsumerContext {

  private String topic;
  private int partition;
  private Map<String, byte[]> headers;
  private transient InputStream key;
  private long offset;
  private ZonedDateTime creationTimestamp;
  private ZonedDateTime logAppendTimestamp;
  private Integer serializedKeySize;
  private Integer serializedValueSize;
  private Integer leaderEpoch;

  public KafkaRecordAttributes() {
  }

  public KafkaRecordAttributes(String consumerCommitKey, String topic, int partition, Map<String, byte[]> headers, InputStream key, long offset,
      ZonedDateTime creationTimestamp, ZonedDateTime logAppendTimestamp, Integer serializedKeySize,
      Integer serializedValueSize, Integer leaderEpoch) {
    super(consumerCommitKey);
    this.topic = topic;
    this.partition = partition;
    this.headers = headers;
    this.key = key;
    this.offset = offset;
    this.creationTimestamp = creationTimestamp;
    this.logAppendTimestamp = logAppendTimestamp;
    this.serializedKeySize = serializedKeySize;
    this.serializedValueSize = serializedValueSize;
    this.leaderEpoch = leaderEpoch;
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

  public Map<String, byte[]> getHeaders() {
    return this.headers;
  }

  public InputStream getKey() {
    return this.key;
  }

  public ZonedDateTime getCreationTimestamp() {
    return this.creationTimestamp;
  }

  public ZonedDateTime getLogAppendTimestamp() {
    return this.logAppendTimestamp;
  }

  public Integer getSerializedKeySize() {
    return this.serializedKeySize;
  }

  public Integer getSerializedValueSize() {
    return this.serializedValueSize;
  }

  public Integer getLeaderEpoch() {
    return this.leaderEpoch;
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

  public void setHeaders(Map<String, byte[]> headers) {
    this.headers = headers;
  }

  public void setKey(InputStream key) {
    this.key = key;
  }

  public void setCreationTimestamp(ZonedDateTime creationTimestamp) {
    this.creationTimestamp = creationTimestamp;
  }

  public void setLogAppendTimestamp(ZonedDateTime logAppendTimestamp) {
    this.logAppendTimestamp = logAppendTimestamp;
  }

  public void setSerializedKeySize(Integer serializedKeySize) {
    this.serializedKeySize = serializedKeySize;
  }

  public void setSerializedValueSize(Integer serializedValueSize) {
    this.serializedValueSize = serializedValueSize;
  }

  public void setLeaderEpoch(Integer leaderEpoch) {
    this.leaderEpoch = leaderEpoch;
  }

}
