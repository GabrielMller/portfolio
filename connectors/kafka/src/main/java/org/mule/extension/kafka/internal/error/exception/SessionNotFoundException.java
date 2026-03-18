package org.mule.extension.kafka.internal.error.exception;

import org.mule.extension.kafka.api.source.AckMode;
import org.mule.extension.kafka.internal.error.KafkaErrorType;

public class SessionNotFoundException extends KafkaModuleException {
   public SessionNotFoundException(AckMode ackMode, String sessionId) {
      super(String.format("Session matching ID '%s' on AckMode '%s' not found.", sessionId, ackMode), KafkaErrorType.SESSION_NOT_FOUND);
   }
}
