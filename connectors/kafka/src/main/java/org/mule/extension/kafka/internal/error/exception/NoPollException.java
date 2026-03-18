package org.mule.extension.kafka.internal.error.exception;

import org.mule.extension.kafka.internal.error.KafkaErrorType;

public class NoPollException extends KafkaModuleException {
   public NoPollException(String message) {
      super(message, KafkaErrorType.NO_POLL_MADE);
   }
}

