package org.mule.extension.kafka.internal.connection.provider;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.errors.InvalidConfigurationException;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.mule.extension.kafka.api.connection.providers.ProducerAck;
import org.mule.extension.kafka.internal.connection.ProducerConnection;
import org.mule.extension.kafka.internal.model.serializer.InputStreamSerializer;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.sdk.api.annotation.Expression;
import org.mule.sdk.api.annotation.param.Optional;
import org.mule.sdk.api.annotation.param.Parameter;
import org.mule.sdk.api.annotation.param.display.DisplayName;
import org.mule.sdk.api.annotation.param.display.Example;
import org.mule.sdk.api.annotation.param.display.Placement;
import org.mule.sdk.api.annotation.param.display.Summary;
import org.mule.sdk.api.connectivity.ConnectionValidationResult;
import org.mule.sdk.api.meta.ExpressionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProducerConnectionProvider extends KafkaConnectionProvider<ProducerConnection> {
  private static final Logger logger = LoggerFactory.getLogger(ProducerConnectionProvider.class);
  @Parameter
  @Placement(order = 9, tab = "Advanced")
  @Summary("Tempo maximo para resposta do send().")
  @DisplayName("Delivery timeout")
  @Optional(defaultValue = "120")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private int deliveryTimeout;

  @Parameter
  @Placement(order = 10, tab = "Advanced")
  @Summary("Unidade de tempo para o timeout de entrega.")
  @Example("SECONDS")
  @Optional(defaultValue = "SECONDS")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private TimeUnit deliveryTimeoutTimeUnit;

  @Parameter
  @Placement(order = 11, tab = "Advanced")
  @DisplayName("Enable idempotence")
  @Summary("Ativa idempotência.")
  @Optional(defaultValue = "false")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private boolean idempotence;

  @Parameter
  @Placement(order = 16, tab = "Advanced")
  @Summary("Número máximo de requisições em espera.")
  @Optional(defaultValue = "5")
  @DisplayName("Maximum in flight requests")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private int maximumInFlightRequests;

  @Parameter
  @Placement(order = 19, tab = "Advanced")
  @DisplayName("Producer acknowledge mode")
  @Summary("Define o nível de confirmação que o produtor espera do Kafka para considerar uma mensagem como enviada com sucesso.")
  @Example("ALL")
  @Optional(defaultValue = "NONE")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private ProducerAck producerAck;

  @Parameter
  @Optional
  @Placement(tab = "Advanced", order = 29)
  @Expression(ExpressionSupport.SUPPORTED)
  @Summary("Additional properties used to configure pooling profile.")
  private Map<String, Object> additionalProperties = Collections.emptyMap();

  private KafkaProducer<InputStream, InputStream> defaultProducer;
  private Properties defaultProperties;

  public ProducerConnectionProvider(SecurityProtocol plainProtocol) {
    super(plainProtocol);
  }

  protected void testConnectivity(Properties properties) throws ConnectionException {
    super.testConnectivity(properties);
  }

  public ProducerConnection connect() throws ConnectionException {
    this.defaultProducer = new KafkaProducer<>(this.defaultProperties);
    return super.connect();
  }

  public ProducerConnection connect(Properties properties) {
    return new ProducerConnection(this.defaultProducer, properties);
  }

  public void dispose() {
    try {
      if (this.defaultProducer != null) {
        this.defaultProducer.close();
        logger.debug("Producer closed", this.getClass().getSimpleName());
      }

    } catch (Exception ex) {
      throw new MuleRuntimeException(ex);
    }
  }

  protected void initialise(Properties properties) throws InitialisationException {
    try {
      this.setPropertyAsString("key.serializer", InputStreamSerializer.class.getName());
      this.setPropertyAsString("value.serializer", InputStreamSerializer.class.getName());
      this.setPropertyAsString("acks", this.producerAck != null ? String.valueOf(this.producerAck.ackValue) : null);
      this.setPropertyAsString("delivery.timeout.ms",
          String.valueOf(this.deliveryTimeoutTimeUnit.toMillis(this.deliveryTimeout)));
      this.setPropertyAsString("enable.idempotence", String.valueOf(this.idempotence));
      this.setPropertyAsString("max.in.flight.requests.per.connection", String.valueOf(this.maximumInFlightRequests));
      this.setPropertyAsString("retries", String.valueOf(1));
       this.setPropertyAsString("retry.backoff.ms", String.valueOf(100));
      this.defaultPropertiesSet(properties);
      if (this.additionalProperties != null) {
        this.additionalProperties.forEach((key, value) -> {
          this.setPropertyAsString(key, value);
        });
      }
      this.defaultProperties = properties;
    } catch (Exception ex) {
      throw new InvalidConfigurationException(
          "Error initializing ProducerConnectionProvider. Check the provided configuration.", ex);
    }
  }

  protected void defaultPropertiesSet(Properties properties) throws InitialisationException {
  }

  public void disconnect(ProducerConnection connection) {
    connection.disconnect();
  }

  public ConnectionValidationResult validate(ProducerConnection connection) {
    try {
      connection.validate();
      return ConnectionValidationResult.success();
    } catch (Exception ex) {
      return ConnectionValidationResult.failure("Failed to validate the connection: " + ex.getMessage(), ex);
    }
  }

}
