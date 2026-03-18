package org.mule.extension.kafka.internal.error.exception;

import org.mule.extension.kafka.internal.error.KafkaErrorType;

public class OperationTimeoutException extends KafkaModuleException {
   public OperationTimeoutException(long timeout) {
      super(String.format("Operation timed out after %dms", timeout), KafkaErrorType.TIMEOUT);
   }

   public OperationTimeoutException(String operationName, Throwable cause) {
      super(String.format("Operation '%s' timed out.", operationName), KafkaErrorType.TIMEOUT, cause);
   }

   public OperationTimeoutException(String message, long timeout) {
      super(message, KafkaErrorType.TIMEOUT);
   }
}
