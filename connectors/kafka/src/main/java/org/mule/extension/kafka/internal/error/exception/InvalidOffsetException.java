package org.mule.extension.kafka.internal.error.exception;

import org.mule.extension.kafka.internal.error.KafkaErrorType;

public class InvalidOffsetException extends KafkaModuleException {
   public InvalidOffsetException(String message, long offset, Throwable cause) {
      super(String.format(message, offset), KafkaErrorType.INVALID_OFFSET, cause);
   }

   public InvalidOffsetException(long offset, Throwable cause) {
      this(String.format("Offset %d is invalid.", offset), offset, cause);
   }

   public InvalidOffsetException(String message, Throwable cause) {
      super(message, KafkaErrorType.INVALID_OFFSET, cause);
   }

   public InvalidOffsetException(Throwable cause) {
      this("Existing offset is invalid.", cause);
   }
}
