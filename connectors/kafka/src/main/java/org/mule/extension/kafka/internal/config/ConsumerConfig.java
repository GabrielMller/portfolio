package org.mule.extension.kafka.internal.config;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.Date;

import org.mule.extension.kafka.api.source.Record;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.record.TimestampType;
import org.mule.extension.kafka.api.KafkaRecordAttributes;
import org.mule.extension.kafka.api.source.AckMode;
import org.mule.extension.kafka.api.source.ConsumerContext;
import org.mule.extension.kafka.internal.connection.provider.plaintext.PlaintextConsumerConnectionProvider;
import org.mule.extension.kafka.internal.model.serializer.SerByteArray;
import org.mule.extension.kafka.internal.source.SingleMessageListenerSource;
import org.mule.runtime.api.util.MultiMap;
import org.mule.sdk.api.annotation.Configuration;
import org.mule.sdk.api.annotation.Expression;
import org.mule.sdk.api.annotation.Sources;
import org.mule.sdk.api.annotation.connectivity.ConnectionProviders;
import org.mule.sdk.api.annotation.param.Optional;
import org.mule.sdk.api.annotation.param.Parameter;
import org.mule.sdk.api.annotation.param.display.DisplayName;
import org.mule.sdk.api.annotation.param.display.Placement;
import org.mule.sdk.api.annotation.param.display.Summary;
import org.mule.sdk.api.meta.ExpressionSupport;
import org.mule.sdk.api.runtime.operation.Result;
import org.mule.sdk.api.runtime.source.SourceCallbackContext;

@ConnectionProviders({ PlaintextConsumerConnectionProvider.class })
@Sources({ SingleMessageListenerSource.class })
@Configuration(name = "consumerConfig")
@DisplayName("Consumer Configuration")
public class ConsumerConfig {
  @Parameter
  @Placement(order = 1)
  @Summary("Define o modo de reconhecimento de mensagens. AUTO: O conector reconhece as mensagens automaticamente após o processamento. MANUAL: O usuário é responsável por reconhecer as mensagens usando a operação de reconhecimento. NONE: O conector não reconhece as mensagens, e o compromisso é gerenciado inteiramente pelo Kafka.")
  @Optional(defaultValue = "AUTO")
  @DisplayName("Default acknowledgement mode")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private AckMode ackMode;

  @Parameter
  @Placement(order = 2)
  @Summary("Define o tempo limite para a operação de polling do consumidor Kafka. O valor é expresso em milissegundos. O valor padrão é 100 ms.")
  @DisplayName("Default listener poll timeout")
  @Optional(defaultValue = "100")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private Integer pollTimeout;

  @Parameter
  @Placement(order = 3)
  @Summary("Define a unidade de tempo para o tempo limite de polling do consumidor Kafka.")
  @DisplayName("Default listener poll timeout time unit")
  @Optional(defaultValue = "MILLISECONDS")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private TimeUnit pollTimeoutTimeUnit;

  @Parameter
  @Placement(order = 4)
  @Summary("Define o tempo limite para as operações de consumo do consumidor Kafka. O valor é expresso em milissegundos. O valor padrão é -1, o que indica que não há tempo limite para as operações de consumo.")
  @DisplayName("Default operation poll timeout")
  @Optional(defaultValue = "-1")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private int operationTimeout;
  @Parameter
  @Placement(order = 5)
  @Summary("Define a unidade de tempo para o tempo limite de polling das operações de consumo do consumidor Kafka.")
  @DisplayName("Default operation poll timeout time unit")
  @Optional(defaultValue = "SECONDS")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private TimeUnit operationTimeoutTimeUnit;

  @Parameter
  @Placement(order = 6)
  @Optional
  @DisplayName("Zone ID")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private String zoneId;

  public Result<List<Record>, ConsumerContext> parseRecords(String sessionId,
      List<ConsumerRecord<InputStream, InputStream>> records) {
    return Result.<List<Record>, ConsumerContext>builder()
        .output(StreamSupport.stream(records.spliterator(), false).map((consumerRecord) -> {
          return this.parseRecord(sessionId, consumerRecord);
        }).map((record) -> {
          return new Record((InputStream) record.getOutput(), (KafkaRecordAttributes) record.getAttributes().get(),
              new SerByteArray((InputStream) record.getOutput()));
        }).collect(Collectors.toList())).attributes(new ConsumerContext(sessionId)).build();
  }

  public Result<InputStream, KafkaRecordAttributes> parseRecord(String consumerCommitKey,
      ConsumerRecord<InputStream, InputStream> record) {
    return Result.<InputStream, KafkaRecordAttributes>builder().output(record.value())
        .attributes(
            new KafkaRecordAttributes(java.util.Optional.ofNullable(consumerCommitKey).orElse(null),
                record.topic(), record.partition(), StreamSupport.stream(record.headers().spliterator(), false)
                    .collect(Collectors.toMap(Header::key, (header) -> {
                      return (byte[]) java.util.Optional.ofNullable(header.value()).orElseGet(""::getBytes);
                    }, (prevValue, currentValue) -> {
                      return currentValue;
                    }, MultiMap::new)),
                (InputStream) record.key(), record.offset(),
                TimestampType.CREATE_TIME.equals(record.timestampType())
                    ? ZonedDateTime.ofInstant((new Date(record.timestamp())).toInstant(),
                        java.util.Optional.ofNullable(this.zoneId).map(ZoneId::of)
                            .orElseGet(ZoneId::systemDefault))
                    : null,
                TimestampType.LOG_APPEND_TIME.equals(record.timestampType())
                    ? ZonedDateTime.ofInstant((new Date(record.timestamp())).toInstant(),
                        java.util.Optional.ofNullable(this.zoneId).map(ZoneId::of)
                            .orElseGet(ZoneId::systemDefault))
                    : null,
                record.serializedKeySize(), record.serializedValueSize(),
                (Integer) record.leaderEpoch().orElse(null)))
        .build();
  }

  public Duration asDuration(long amount, TimeUnit timeUnit) {
    ChronoUnit chronoUnit = null;
    switch (timeUnit) {
      case NANOSECONDS:
        chronoUnit = ChronoUnit.NANOS;
        break;
      case MICROSECONDS:
        chronoUnit = ChronoUnit.MICROS;
        break;
      case MILLISECONDS:
        chronoUnit = ChronoUnit.MILLIS;
        break;
      case SECONDS:
        chronoUnit = ChronoUnit.SECONDS;
        break;
      case MINUTES:
        chronoUnit = ChronoUnit.MINUTES;
        break;
      case HOURS:
        chronoUnit = ChronoUnit.HOURS;
        break;
      case DAYS:
        chronoUnit = ChronoUnit.DAYS;
    }

    return Duration.of(amount, chronoUnit);
  }

  public SourceCallbackContext singleElementHeadersAction(ConsumerRecord<InputStream, InputStream> record,
      SourceCallbackContext callbackContext) {
    Header correlationId = record.headers().lastHeader("correlationId");
    if (correlationId != null) {
      callbackContext.setCorrelationId(new String(correlationId.value(), StandardCharsets.UTF_8));
    } 
    return callbackContext;
  }

}
