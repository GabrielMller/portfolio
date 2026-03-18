package org.mule.extension.kafka.internal.error.exception;

import org.mule.extension.kafka.internal.error.KafkaErrorType;
import org.mule.sdk.api.exception.ModuleException;

public class AuthorizationException extends ModuleException {
   public AuthorizationException(Throwable cause) {
      this("There was an authentication error", cause);
   }

   public AuthorizationException(String message, Throwable cause) {
      super(message, KafkaErrorType.AUTHENTICATION_ERROR, cause);
   }
}
