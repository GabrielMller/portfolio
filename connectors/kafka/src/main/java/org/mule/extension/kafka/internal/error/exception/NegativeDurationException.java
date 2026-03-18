package org.mule.extension.kafka.internal.error.exception;

import java.time.Duration;

public class NegativeDurationException extends InvalidInputException {
   public NegativeDurationException(Duration duration, Throwable cause) {
      super(String.format("Duration %dms is negative.", duration.toMillis()), cause);
   }
}
