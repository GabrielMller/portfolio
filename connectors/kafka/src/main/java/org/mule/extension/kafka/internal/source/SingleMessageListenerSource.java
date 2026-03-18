package org.mule.extension.kafka.internal.source;

import java.io.InputStream;
import java.time.Duration;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.mule.extension.kafka.api.KafkaRecordAttributes;
import org.mule.extension.kafka.api.source.AckMode;
import org.mule.extension.kafka.internal.MessageListenerSource;
import org.mule.extension.kafka.internal.PollingTask;
import org.mule.extension.kafka.internal.config.ConsumerConfig;
import org.mule.extension.kafka.internal.connection.ConsumerConnection;
import org.mule.extension.kafka.internal.model.consumer.MuleConsumer;
import org.mule.sdk.api.annotation.Alias;
import org.mule.sdk.api.annotation.param.MediaType;
import org.mule.sdk.api.annotation.param.display.DisplayName;
import org.mule.sdk.api.annotation.source.BackPressure;
import org.mule.sdk.api.runtime.source.BackPressureMode;
import org.mule.sdk.api.runtime.source.SourceCallback;

@DisplayName("Message listener")
@Alias("message-listener")
@MediaType("*/*")
@BackPressure(defaultMode = BackPressureMode.WAIT, supportedModes = { BackPressureMode.WAIT })
public class SingleMessageListenerSource extends MessageListenerSource<InputStream, KafkaRecordAttributes> {
  public void createPollingTask(Consumer<PollingTask<InputStream, KafkaRecordAttributes, ?>> pollingTasks,
      ConsumerConfig config, ConsumerConnection connection,
      SourceCallback<InputStream, KafkaRecordAttributes> sourceCallback, AckMode ackMode, Duration pollingTimeout) {
    pollingTasks.accept(new PollingTask<InputStream, KafkaRecordAttributes, ConsumerRecord<InputStream, InputStream>>(connection, ackMode, pollingTimeout, MuleConsumer::singleElementPoll,
        config::parseRecord, sourceCallback, config::singleElementHeadersAction));
  }
}
