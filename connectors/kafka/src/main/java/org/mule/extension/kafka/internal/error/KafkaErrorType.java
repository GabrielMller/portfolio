package org.mule.extension.kafka.internal.error;

import java.util.Optional;

import org.mule.sdk.api.error.ErrorTypeDefinition;
import org.mule.sdk.api.error.MuleErrors;

public enum KafkaErrorType implements ErrorTypeDefinition<KafkaErrorType> {
   NOT_FOUND,
   AUTHORIZATION_ERROR(MuleErrors.CLIENT_SECURITY),
   AUTHENTICATION_ERROR(MuleErrors.SECURITY),
   COMMIT_FAILED,
   ALREADY_COMMITED,
   INVALID_CONFIGURATION,
   INVALID_ACK_MODE,
   SESSION_NOT_FOUND,
   TIMEOUT,
   INVALID_OFFSET,
   INVALID_INPUT,
   ILLEGAL_STATE,
   OUT_OF_RANGE,
   INVALID_TOPIC,
   INVALID_TOPIC_PARTITION,
   INPUT_TOO_LARGE(INVALID_INPUT),
   NO_POLL_MADE,
   PRODUCER_FENCED,
   INVALID_CONNECTION(MuleErrors.CONNECTIVITY),
   PREVIOUS_ASSIGNATION,
   SERIALIZATION_ERROR,
   DESERIALIZATION_ERROR,
   SECURITY(MuleErrors.SECURITY);

   private ErrorTypeDefinition<?> parent;

   private KafkaErrorType() {
      this(MuleErrors.ANY);
   }

   private KafkaErrorType(ErrorTypeDefinition<?> parent) {
      this.parent = parent;
   }

   public Optional<ErrorTypeDefinition<? extends Enum<?>>> getParent() {
      return Optional.of(this.parent);
   }
}
