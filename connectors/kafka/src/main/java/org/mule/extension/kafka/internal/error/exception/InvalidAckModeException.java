package org.mule.extension.kafka.internal.error.exception;

import org.mule.extension.kafka.api.source.AckMode;
import org.mule.extension.kafka.internal.error.KafkaErrorType;
import org.mule.sdk.api.error.ErrorTypeDefinition;

public class InvalidAckModeException extends InvalidInputException {
   public InvalidAckModeException(AckMode ackMode) {
      super(String.format("Acknowledgement mode '%s' is invalid.", ackMode), (ErrorTypeDefinition<?>)KafkaErrorType.INVALID_ACK_MODE);
   }
}

