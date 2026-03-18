package org.mule.extension.kafka.internal.operations;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import org.mule.extension.kafka.api.operation.ResponseMetadata;
import org.mule.extension.kafka.internal.config.ProducerConfig;
import org.mule.extension.kafka.internal.connection.ProducerConnection;
import org.mule.sdk.api.annotation.param.Config;
import org.mule.sdk.api.annotation.param.Connection;
import org.mule.sdk.api.annotation.param.Content;
import org.mule.sdk.api.annotation.param.NullSafe;
import org.mule.sdk.api.annotation.param.Optional;
import org.mule.sdk.api.runtime.parameter.CorrelationInfo;
import org.mule.sdk.api.runtime.process.CompletionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Publish {
  private static final Logger LOGGER = LoggerFactory.getLogger(Publish.class);

  public void publishMessage(@Config ProducerConfig config, @Connection ProducerConnection connection,
      String topic, @Optional Integer partition, @Optional InputStream key,
      @Content(primary = true) InputStream message, @Content @Optional @NullSafe Map<String, InputStream> headers,
      CompletionCallback<ResponseMetadata, Void> completionCallback, CorrelationInfo correlationInfo) {
    if (config.isSendCorrelationId()) {
      String correlationId = correlationInfo.getCorrelationId();
      if (correlationId != null) {
        if (headers != null) {
          headers.put("correlationId", new ByteArrayInputStream(correlationId.getBytes()));
        } else {
          headers = Map.of("correlationId", new ByteArrayInputStream(correlationId.getBytes()));
        }
      }
    }
    connection.publish(topic, partition, key, message, headers, (recordMetadata) -> {
      completionCallback.success(config.parseMetadata(recordMetadata));
    }, completionCallback::error);
  }
}
