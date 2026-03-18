package org.mule.extension.kafka.internal.error.exception;

import org.mule.extension.kafka.internal.error.KafkaErrorType;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.sdk.api.exception.ModuleException;

public class AuthenticationException extends ModuleException {
   private AuthenticationException(String message, Throwable cause) {
      super(message, KafkaErrorType.AUTHENTICATION_ERROR, new ConnectionException(cause));
   }

   public AuthenticationException(Throwable e) {
      this("There was an authentication error", e);
   }
}

