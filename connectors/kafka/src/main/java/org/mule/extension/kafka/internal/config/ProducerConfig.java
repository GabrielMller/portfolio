package org.mule.extension.kafka.internal.config;

import org.mule.extension.kafka.api.operation.ResponseMetadata;
import org.mule.extension.kafka.internal.connection.provider.plaintext.PlaintextProducerConnectionProvider;
import org.mule.extension.kafka.internal.operations.Publish;
import org.mule.sdk.api.annotation.Configuration;
import org.mule.sdk.api.annotation.Expression;
import org.mule.sdk.api.annotation.Operations;
import org.mule.sdk.api.annotation.connectivity.ConnectionProviders;
import org.mule.sdk.api.annotation.param.Optional;
import org.mule.sdk.api.annotation.param.Parameter;
import org.mule.sdk.api.annotation.param.display.DisplayName;
import org.mule.sdk.api.annotation.param.display.Placement;
import org.mule.sdk.api.annotation.param.display.Summary;
import org.mule.sdk.api.meta.ExpressionSupport;
import org.mule.sdk.api.runtime.operation.Result;
import org.apache.kafka.clients.producer.RecordMetadata;

@Configuration(name = "producerConfig")
@ConnectionProviders({
		PlaintextProducerConnectionProvider.class
})
@Operations({
		Publish.class
})
public class ProducerConfig {

	@Parameter
  @DisplayName("Send correlation id")
  @Summary("Indica se o correlationId deve ser enviado nos headers.")
  @Optional(defaultValue = "true")
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private boolean sendCorrelationId;

	private String topic;

	public String getTopic() {
		return this.topic;
	}

	public boolean isSendCorrelationId() {
		return this.sendCorrelationId;
	}

	public Result<ResponseMetadata, Void> parseMetadata(RecordMetadata metadata) {
		if (metadata == null) {
			return null;
		}
		return Result.<ResponseMetadata, Void>builder().output(this.buildResponseMetadata(metadata)).build();
	}

	public ResponseMetadata buildResponseMetadata(RecordMetadata metadata) {
		return new ResponseMetadata(metadata.topic(), metadata.partition(), metadata.offset());
	}

}
