package org.mule.extension.kafka.internal.error.exception;

import org.mule.extension.kafka.internal.error.KafkaErrorType;
import org.mule.sdk.api.error.ErrorTypeDefinition;

public class InvalidInputException extends KafkaModuleException {
   public InvalidInputException(String message, Throwable cause) {
      super(message, KafkaErrorType.INVALID_INPUT, cause);
   }

   public InvalidInputException(String message) {
      super(message, KafkaErrorType.INVALID_INPUT);
   }

   public InvalidInputException(String message, ErrorTypeDefinition<?> errorTypeDefinition) {
      super(message, errorTypeDefinition);
   }
}

