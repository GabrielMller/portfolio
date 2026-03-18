package org.mule.extension.kafka.internal.error.exception;

public class OperationInterruptedException extends UnexpectedException {
   public static final String DEFAULT_MESSAGE = "The execution of the operation has been interrupted by an external factor.";

   public OperationInterruptedException(InterruptedException cause) {
      super("The execution of the operation has been interrupted by an external factor.", cause);
      Thread.currentThread().interrupt();
   }

   public OperationInterruptedException(Throwable cause) {
      super("The execution of the operation has been interrupted by an external factor.", cause);
   }
}