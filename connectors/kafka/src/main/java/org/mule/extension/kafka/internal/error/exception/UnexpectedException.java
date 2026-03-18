package org.mule.extension.kafka.internal.error.exception;

public class UnexpectedException extends RuntimeException {
   public UnexpectedException(Throwable cause) {
      this("An unexpected error has occurred.", cause);
   }

   public UnexpectedException(String message, Throwable cause) {
      super(message, cause);
   }
}
