package org.mule.extension.kafka.internal.error.exception;

import org.mule.extension.kafka.internal.error.KafkaErrorType;

public class InvalidTopicException extends KafkaModuleException {
   public InvalidTopicException(String message, Throwable cause) {
      super(message, KafkaErrorType.INVALID_TOPIC, cause);
   }
}

