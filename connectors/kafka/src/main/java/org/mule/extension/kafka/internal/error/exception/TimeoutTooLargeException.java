package org.mule.extension.kafka.internal.error.exception;

import java.time.Duration;

public class TimeoutTooLargeException extends InvalidInputException {
   public TimeoutTooLargeException(Duration duration, Throwable cause) {
      super(String.format("Timeout of ~%s days is too large. Maximum value is %d milliseconds.", duration.toDays(), Long.MAX_VALUE), cause);
   }
}

