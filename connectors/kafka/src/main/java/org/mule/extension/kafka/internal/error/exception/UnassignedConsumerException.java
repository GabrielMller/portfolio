package org.mule.extension.kafka.internal.error.exception;

import org.mule.extension.kafka.internal.error.KafkaErrorType;

public class UnassignedConsumerException extends KafkaModuleException {
   public UnassignedConsumerException(Throwable cause) {
      super("The consumer used for this operation is not assigned nor subscribed to any topic.", KafkaErrorType.ILLEGAL_STATE, cause);
   }
}

