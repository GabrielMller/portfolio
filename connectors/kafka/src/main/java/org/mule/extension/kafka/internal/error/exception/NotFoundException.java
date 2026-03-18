package org.mule.extension.kafka.internal.error.exception;

import org.mule.extension.kafka.internal.error.KafkaErrorType;

public class NotFoundException extends KafkaModuleException {
   public NotFoundException() {
      super("The sought element was not found.", KafkaErrorType.NOT_FOUND);
   }

   public NotFoundException(String message) {
      super(message, KafkaErrorType.NOT_FOUND);
   }

   public NotFoundException(String message, Throwable cause) {
      super(message, KafkaErrorType.NOT_FOUND, cause);
   }
}
