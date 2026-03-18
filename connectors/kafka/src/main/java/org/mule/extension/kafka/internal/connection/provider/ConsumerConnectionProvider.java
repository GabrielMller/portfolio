package org.mule.extension.kafka.internal.connection.provider;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.InvalidConfigurationException;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.mule.extension.kafka.api.connection.providers.AutoOffsetReset;
import org.mule.extension.kafka.api.params.SubscriptionParamGroup;
import org.mule.extension.kafka.api.source.TopicPartition;
import org.mule.extension.kafka.internal.connection.ConsumerConnection;
import org.mule.extension.kafka.internal.model.consumer.DefaultConsumerPool;
import org.mule.extension.kafka.internal.model.consumer.DefaultMuleConsumer;
import org.mule.extension.kafka.internal.model.consumer.MuleConsumer;
import org.mule.extension.kafka.internal.model.serializer.InputStreamDeserializer;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.sdk.api.annotation.Expression;
import org.mule.sdk.api.annotation.param.Optional;
import org.mule.sdk.api.annotation.param.Parameter;
import org.mule.sdk.api.annotation.param.ParameterGroup;
import org.mule.sdk.api.annotation.param.RefName;
import org.mule.sdk.api.annotation.param.display.DisplayName;
import org.mule.sdk.api.annotation.param.display.Example;
import org.mule.sdk.api.annotation.param.display.Placement;
import org.mule.sdk.api.annotation.param.display.Summary;
import org.mule.sdk.api.connectivity.ConnectionValidationResult;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.scheduler.SchedulerConfig;
import org.mule.sdk.api.meta.ExpressionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConsumerConnectionProvider extends KafkaConnectionProvider<ConsumerConnection> {
  private static final Logger logger = LoggerFactory.getLogger(ConsumerConnectionProvider.class);

  @Parameter
  @Optional
  @Summary("O ID do grupo de consumidores ao qual este consumidor pertence. Consumidores que compartilham o mesmo groupId formam um grupo de consumidores e compartilham a carga de consumo das partições dos tópicos aos quais estão inscritos.")
  @Example("test-consumer-group")
  @DisplayName("Group ID")
  @Placement(order = 20)
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private String groupId;
  @ParameterGroup(name = "Topics")
  @Summary("A configuração dos tópicos para consumir mensagens.")
  @DisplayName("topics")
  @Placement(order = 30)
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private SubscriptionParamGroup topics;

  @Parameter
  @Placement(order = 2, tab = "Advanced")
  @Summary("Tempo máximo entre invocações de poll().")
  @DisplayName("Maximum polling interval")
  @Optional(defaultValue = "60")
  @Example("60")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private long maximumPollingInterval;
  @Parameter
  @Placement(order = 3, tab = "Advanced")
  @Summary("Unidade de tempo para o intervalo de poll máximo.")
  @Example("SECONDS")
  @Optional(defaultValue = "SECONDS")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private TimeUnit maximumPollingIntervalTimeUnit;

  @Parameter
  @Summary("Número de consumidores a serem criados para esta conexão. O número de consumidores deve ser menor ou igual ao número total de partições dos tópicos aos quais o consumidor está inscrito, caso contrário, alguns consumidores ficarão ociosos.")
  @Placement(order = 1, tab = "Advanced")
  @Optional(defaultValue = "1")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private int consumerAmount;

  @Parameter
  @Placement(order = 6, tab = "Advanced")
  @Summary("Determina o comportamento do consumidor quando não há offset inicial ou se o offset atual não está mais disponível. LATEST fará com que o consumidor comece a consumir a partir do final da partição, ou seja, apenas mensagens produzidas após o início do consumidor serão consumidas. EARLIEST fará com que o consumidor comece a consumir a partir do início da partição, ou seja, todas as mensagens disponíveis serão consumidas. ERROR fará com que o consumidor lance uma exceção caso não haja offset inicial ou se o offset atual não estiver mais disponível.")
  @Optional(defaultValue = "LATEST")
  @DisplayName("Auto offset reset")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private AutoOffsetReset autoOffsetReset;

  @Parameter
  @Placement(order = 14, tab = "Advanced")
  @Summary("Tempo máximo para resposta do poll().")
  @Example("30")
  @Optional(defaultValue = "30")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private long requestTimeout;

  @Parameter
  @Placement(order = 15, tab = "Advanced")
  @Summary("Unidade de tempo para o timeout da solicitação.")
  @Example("SECONDS")
  @Optional(defaultValue = "SECONDS")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private TimeUnit requestTimeoutTimeUnit;

  @Parameter
  @Placement(order = 16, tab = "Advanced")
  @Summary("Número máximo de registros retornados em um único poll.")
  @Example("500")
  @Optional(defaultValue = "500")
  @DisplayName("Limite padrão de registros")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private Integer recordLimit;

  @Parameter
  @Placement(order = 18, tab = "Advanced")
  @Summary("Intervalo de tempo entre os polls quando o consumidor está ocioso, ou seja, quando não há mensagens para consumir. Definir um intervalo de tempo entre os polls pode ajudar a reduzir a carga no cluster Kafka e melhorar a eficiência do consumidor.")
  @Optional(defaultValue = "3")
  @DisplayName("Intervalo de heartbeat")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private int heartbeatInterval;

  @Parameter
  @Placement(order = 19, tab = "Advanced")
  @Summary("Unidade de tempo para o intervalo de heartbeat")
  @Optional(defaultValue = "SECONDS")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private TimeUnit heartbeatIntervalTimeUnit;

  @Parameter
  @Placement(order = 20, tab = "Advanced")
  @Summary("Tempo a ser aguardado antes que o consumidor seja removido do grupo.")
  @Optional(defaultValue = "10")
  @DisplayName("Session timeout")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private int sessionTimeout;

  @Parameter
  @Placement(order = 21, tab = "Advanced")
  @Summary("Unidade de tempo para o timeout da sessão")
  @Optional(defaultValue = "SECONDS")
  @DisplayName("Session timeout time unit")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private TimeUnit sessionTimeoutTimeUnit;

  @Parameter
  @Placement(order = 22, tab = "Advanced")
  @Summary("Tempo a ser aguardado antes de fechar a conexão ociosa.")
  @Optional(defaultValue = "540")
  @DisplayName("Connection maximum idle time")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private int connectionsMaximumIdleTime;

  @Parameter
  @Placement(order = 23, tab = "Advanced")
  @Summary("Unidade de tempo para o tempo máximo de conexão ociosa.")
  @Optional(defaultValue = "SECONDS")
  @DisplayName("Connection maximum idle time time unit")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private TimeUnit connectionsMaximumIdleTimeUnit;

  @Parameter
  @Optional
  @Placement(tab = "Advanced", order = 24)
  @Expression(ExpressionSupport.SUPPORTED)
  @Summary("Additional properties used to configure pooling profile.")
  private Map<String, Object> additionalProperties = Collections.emptyMap();
  @Inject
  private SchedulerService schedulerService;
  @RefName
  private String configName;
  private Scheduler workerScheduler;

  protected ConsumerConnectionProvider(SecurityProtocol plainProtocol) {
    super(plainProtocol);
  }

  public ConsumerConnection connect(Properties properties) throws ConnectionException {
    if (this.consumerAmount < 1) {
      throw new InvalidConfigurationException("Consumer amount must be at least 1.");
    } else {
      Set<MuleConsumer> consumers = new HashSet<>();
      for (int i = 0; i < this.consumerAmount; ++i) {
        try {
          logger.info("Creating consumer {} with properties {}", i, properties);
          consumers.add(new DefaultMuleConsumer(this.getKafkaConsumerFunction(), properties));
        } catch (KafkaException e) {
          consumers.forEach(IOUtils::closeQuietly);
          this.handleConnectionException(e);
        }
      }
      ConsumerConnection consumerConnection = new ConsumerConnection(new DefaultConsumerPool(consumers),
          this.workerScheduler);

      try {
        List<TopicPartition> assignments = this.topics.getAssignments();
        List<String> topicPatterns = this.topics.getTopicPatterns();
        if (assignments != null && !assignments.isEmpty()) {
          logger.info("Consumers will use assignments: {}", assignments);
          consumerConnection.assign(Duration.ofMillis(-1L), assignments);
        } else if (topicPatterns != null && !topicPatterns.isEmpty()) {
          logger.info("Consumers will use subscriptions: {}", topicPatterns);
          consumerConnection.subscribe(Duration.ofMillis(-1L), topicPatterns);
        }

        return consumerConnection;
      } catch (RuntimeException e) {
        consumerConnection.disconnect();
        logger.error("There was an error when trying to establish the connection.", e);
        throw new ConnectionException(e);
      }
    }
  }

  protected void initialise(Properties properties) throws InitialisationException {
    try {
      this.workerScheduler = this.schedulerService
          .ioScheduler(SchedulerConfig.config().withName(this.configName + "-worker"));
      this.setPropertyAsString("group.id",
          java.util.Optional.ofNullable(this.groupId).orElse(this.configName + "-" + UUID.randomUUID()));
      this.setPropertyAsString("auto.offset.reset",
          java.util.Optional.ofNullable(this.autoOffsetReset).map(Enum::name).map(String::toLowerCase).orElse(null));
      this.setPropertyAsString("max.poll.records", this.recordLimit);
      this.setPropertyAsString("max.poll.interval.ms",
          (int) this.maximumPollingIntervalTimeUnit.toMillis(this.maximumPollingInterval));
      this.setPropertyAsString("request.timeout.ms", (int) this.requestTimeoutTimeUnit.toMillis(this.requestTimeout));
      this.setPropertyAsString("key.deserializer", InputStreamDeserializer.class.getName());
      this.setPropertyAsString("value.deserializer", InputStreamDeserializer.class.getName());
      this.setPropertyAsString("enable.auto.commit", "false");
      this.setPropertyAsString("heartbeat.interval.ms",
          Math.toIntExact(this.heartbeatIntervalTimeUnit.toMillis((long) this.heartbeatInterval)));
      this.setPropertyAsString("session.timeout.ms",
          Math.toIntExact(this.sessionTimeoutTimeUnit.toMillis((long) this.sessionTimeout)));
      this.setPropertyAsString("connections.max.idle.ms",
          this.connectionsMaximumIdleTimeUnit.toMillis((long) this.connectionsMaximumIdleTime));
      this.setPropertyAsString("metadata.max.age.ms",
          Math.toIntExact(this.heartbeatIntervalTimeUnit.toMillis((long) this.heartbeatInterval)));
      this.additionalProperties.forEach((key, value) -> {
        this.setPropertyAsString(key, value);
      });
    } catch (KafkaException ex) {
      this.handleConnectionException(ex);
    }
  }

  public ConnectionValidationResult validate(ConsumerConnection connection) {
    return connection.validateWithResult();
  }

  public void dispose() {
    if (Objects.nonNull(this.workerScheduler)) {
      this.workerScheduler.stop();
    }
  }

  public void disconnect(ConsumerConnection connection) {
    connection.disconnect();
  }
}
