package org.mule.extension.kafka.internal.model.consumer;

public class ConsumerPoolClosedException extends Exception {
   public ConsumerPoolClosedException() {
      super("The consumer Pool is closed");
   }
}

