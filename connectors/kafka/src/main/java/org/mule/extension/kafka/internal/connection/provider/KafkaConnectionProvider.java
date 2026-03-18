package org.mule.extension.kafka.internal.connection.provider;

import java.io.InputStream;
import java.util.Properties;

import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.InvalidConfigurationException;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.mule.extension.kafka.internal.connection.ProducerConnection;
import org.mule.extension.kafka.internal.model.serializer.InputStreamDeserializer;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.sdk.api.annotation.Expression;
import org.mule.sdk.api.annotation.param.Parameter;
import org.mule.sdk.api.annotation.param.display.DisplayName;
import org.mule.sdk.api.annotation.param.display.Example;
import org.mule.sdk.api.annotation.param.display.Summary;
import org.mule.sdk.api.connectivity.ConnectionProvider;
import org.mule.sdk.api.meta.ExpressionSupport;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import java.util.function.Function;
import org.apache.kafka.clients.consumer.Consumer;

public abstract class KafkaConnectionProvider<T> implements ConnectionProvider<T>, Initialisable, Disposable {

  @Parameter
  @Summary("Urls dos brokers do cluster Kafka, separados por vírgula.")
  @Example("localhost:9092,localhost:9093")
  @DisplayName("Bootstrap Servers")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private String bootstrapServers;

  private final SecurityProtocol plainProtocol;
  private Properties properties;
  private Function<Properties, Consumer<InputStream, InputStream>> kafkaConsumerFactory = KafkaConsumer::new;

  public KafkaConnectionProvider(SecurityProtocol plainProtocol) {
    this.plainProtocol = plainProtocol;
  }

  public void initialise() throws InitialisationException {
    this.properties = new Properties();
    this.properties.put("bootstrap.servers", this.bootstrapServers);
    this.properties.put("security.protocol", this.plainProtocol.name());
    this.initialise(this.properties);
  }

  protected abstract void initialise(Properties properties) throws InitialisationException;

  protected Function<Properties, Consumer<InputStream, InputStream>> getKafkaConsumerFunction() {
    return this.kafkaConsumerFactory;
  }

  protected void testConnectivity(Properties properties) throws ConnectionException {
    Properties consumerProperties = (Properties) properties.clone();
    consumerProperties.setProperty("key.deserializer", InputStreamDeserializer.class.getName());
    consumerProperties.setProperty("value.deserializer", InputStreamDeserializer.class.getName());
    consumerProperties.setProperty("group.id", "connectivity");
    consumerProperties.remove("compression.type");
    consumerProperties.remove("enable.idempotence");
    consumerProperties.remove("delivery.timeout.ms");
    consumerProperties.remove("buffer.memory");
    consumerProperties.remove("key.serializer");
    consumerProperties.remove("max.block.ms");
    consumerProperties.remove("max.in.flight.requests.per.connection");
    consumerProperties.remove("acks");
    consumerProperties.remove("batch.size");
    consumerProperties.remove("retries");
    consumerProperties.remove("max.request.size");
    consumerProperties.remove("value.serializer");
    consumerProperties.remove("linger.ms");
    consumerProperties.remove("metadata.max.idle.ms");
    consumerProperties.remove("delivery.timeout.ms");
    consumerProperties.remove("partitioner.class");
    consumerProperties.remove("transaction.timeout.ms");
    consumerProperties.remove("transactional.id");
    try {
      Consumer<InputStream, InputStream> client = this.getKafkaConsumerFunction().apply(consumerProperties);
      try {
        client.listTopics();
      } finally {
        if (client != null) {
          client.close();
        }
      }
    } catch (Exception e) {
      throw new ConnectionException("Failed to test Kafka connectivity", e);
    }
  }

  public T connect() throws ConnectionException {
    this.testConnectivity(this.properties);
    return this.connect(this.properties);
  }

  protected abstract T connect(Properties properties) throws ConnectionException;

  protected void setPropertyAsString(String propertyName, Object value) {
    if (value != null) {
      this.properties.put(propertyName, value.toString());
    }

  }

  protected void handleConnectionException(KafkaException kafkaException) {
    throw new InvalidConfigurationException("The provided configuration is invalid!", kafkaException);
  }
}
