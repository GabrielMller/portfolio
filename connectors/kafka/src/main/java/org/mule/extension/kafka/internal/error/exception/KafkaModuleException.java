package org.mule.extension.kafka.internal.error.exception;

import org.mule.sdk.api.error.ErrorTypeDefinition;
import org.mule.sdk.api.exception.ModuleException;

public class KafkaModuleException extends ModuleException {
   private static final long serialVersionUID = -8367263310319899767L;

   public KafkaModuleException(String message, ErrorTypeDefinition<?> errorType, Throwable cause) {
      super(message, errorType, cause);
   }

   public KafkaModuleException(String message, ErrorTypeDefinition<?> errorType) {
      super(message, errorType);
   }
}
