package org.mule.extension.kafka.internal.connection;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.mule.extension.kafka.internal.model.serializer.InputStreamSerializer;
import org.mule.runtime.api.tx.TransactionException;
import org.mule.runtime.api.util.IOUtils;
import org.mule.sdk.api.connectivity.TransactionalConnection;
import org.apache.kafka.clients.producer.KafkaProducer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthenticationException;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.errors.UnsupportedVersionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import java.util.function.Consumer;

public class ProducerConnection implements TransactionalConnection {

  private static final String PUBLISH = "publish";
  private static final Logger logger = LoggerFactory.getLogger(ProducerConnection.class);
  private final Producer<InputStream, InputStream> defaultProducer;
  private final Properties defaultProperties;
  private Producer<InputStream, InputStream> transactionProducer;
  private volatile String transactionId;
  private final Function<Properties, Producer<InputStream, InputStream>> producerFactory = KafkaProducer::new;

  public ProducerConnection(Producer<InputStream, InputStream> defaultProducer, Properties defaultProperties) {
    this.defaultProducer = defaultProducer;
    this.defaultProperties = defaultProperties;
  }

  public void publish(String topic, Integer partition, InputStream key, InputStream message,
      Map<String, InputStream> headers, Consumer<RecordMetadata> successListener, Consumer<Throwable> errorListener) {
    this.getProducer().send(this.createProducerRecord(topic, partition, key, message, headers),
        (metadata, exception) -> {
          if (exception == null) {
            successListener.accept(metadata);
          } else {
            errorListener.accept(exception);
          }

        });
  }

  private Producer<InputStream, InputStream> getProducer() {
    return Optional.ofNullable(this.transactionProducer).orElse(this.defaultProducer);
  }

  private ProducerRecord<InputStream, InputStream> createProducerRecord(String topic, Integer partition,
      InputStream key, InputStream message, Map<String, InputStream> headers) {
    try {
      return new ProducerRecord<InputStream, InputStream>(topic, partition, System.currentTimeMillis(), key, message,
          headers != null ? headers.entrySet().stream().filter((entry) -> {
            return entry != null && entry.getKey() != null;
          }).map((entry) -> {
            return new RecordHeader((String) entry.getKey(),
                entry.getValue() != null ? IOUtils.toByteArray((InputStream) entry.getValue()) : new byte[0]);
          }).collect(Collectors.toList()) : null);
    } catch (IllegalArgumentException ex) {
      throw new RuntimeException(
          "Error creating the ProducerRecord. Check the provided parameters and their compatibility with the topic's configuration.",
          ex);
    }
  }

  private void createTransactionalProducer() throws TransactionException {
    try {
      Properties transactionProperties = new Properties();
      transactionProperties.putAll(this.defaultProperties);
      if (!this.defaultProperties.containsKey("transactional.id")) {
        this.transactionId = UUID.randomUUID().toString();
        transactionProperties.put("transactional.id", this.transactionId);
      }

      if (!this.defaultProperties.containsKey("key.serializer")) {
        transactionProperties.put("key.serializer", InputStreamSerializer.class.getName());
      }

      if (!this.defaultProperties.containsKey("value.serializer")) {
        transactionProperties.put("value.serializer", InputStreamSerializer.class.getName());
      }

      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(KafkaProducer.class.getClassLoader());
      Producer<InputStream, InputStream> transactionalProducer = this.producerFactory.apply(transactionProperties);
      Thread.currentThread().setContextClassLoader(cl);
      transactionalProducer.initTransactions();
      transactionalProducer.beginTransaction();
      this.transactionProducer = transactionalProducer;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void begin() throws TransactionException {
    try {
      this.createTransactionalProducer();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public void commit() throws TransactionException {
    try {
      this.handleTransactionalException(Producer::commitTransaction);
    } catch (RuntimeException var2) {
      this.wrapThrowTx(var2);
    }

  }

  public void rollback() throws TransactionException {
    try {
      this.handleTransactionalException(Producer::abortTransaction);
    } catch (RuntimeException ex) {
      this.wrapThrowTx(ex);
    }
  }

  private void handleTransactionalException(Consumer<Producer<InputStream, InputStream>> transaction)
      throws TransactionException {
    try {
      transaction.accept(this.transactionProducer);
    } catch (AuthorizationException ex) {
      throw new RuntimeException(
          "Not authorized to perform the transaction. Check the provided credentials and permissions.", ex);
    } catch (AuthenticationException ex) {
      throw new RuntimeException("Failed to authenticate with the Kafka cluster. Check the provided credentials.", ex);
    } catch (ProducerFencedException ex) {
      throw new RuntimeException(
          "The producer has been fenced. This can occur if another producer with the same transactional.id is active or if the transaction timeout has been exceeded.",
          ex);
    } catch (UnsupportedVersionException ex) {
      logger.error("The Kafka version of the cluster does not support transactions");
      throw new RuntimeException("The Kafka version of the cluster does not support transactions", ex);
    } catch (TimeoutException ex) {
      throw new RuntimeException(
          "The transaction could not be completed within the configured timeout. Consider increasing the transaction timeout configuration.",
          ex);
    } catch (KafkaException | IllegalStateException ex) {
      throw new TransactionException(ex);
    } finally {
      this.transactionProducer.close();
      this.transactionProducer = null;
    }

  }

  protected void wrapThrowTx(Throwable exception) throws TransactionException {
    throw new TransactionException(exception);
  }

  public void validate() {
    this.getProducer().metrics();
  }

  public void disconnect() {
    Optional.ofNullable(this.transactionProducer).ifPresent(Producer::close);
  }

}
