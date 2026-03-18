package org.mule.extension.kafka.internal.error.exception;

import org.mule.sdk.api.error.ErrorTypeDefinition;
import org.mule.sdk.api.exception.ModuleException;

public class CommitFailedException extends ModuleException {
   public CommitFailedException(String message, ErrorTypeDefinition<?> errorType, Throwable cause) {
      super(message, errorType, cause);
   }

   public CommitFailedException(String message, ErrorTypeDefinition<?> errorTypeDefinition) {
      super(message, errorTypeDefinition);
   }
}
