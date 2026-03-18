package org.mule.extension.kafka.internal.error.exception;

public class InvalidTopicNameException extends InvalidTopicException {
   public InvalidTopicNameException(Throwable cause) {
      super("Topic name is invalid.", cause);
   }
}
