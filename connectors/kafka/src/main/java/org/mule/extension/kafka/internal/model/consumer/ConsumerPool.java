package org.mule.extension.kafka.internal.model.consumer;

import java.io.Closeable;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;

public interface ConsumerPool extends Closeable {
  MuleConsumer checkOut(Duration timeout) throws ConsumerPoolClosedException;

  MuleConsumer checkOut(String topic, int partition, Duration timeout) throws ConsumerPoolClosedException;

  Set<MuleConsumer> checkoutAll(Duration timeout) throws ConsumerPoolClosedException;

  void checkIn(MuleConsumer consumer);

  boolean isValid();

  void invalidate();

  void checkPoolIsValid(Optional<MuleConsumer> result) throws ConsumerPoolClosedException;

  void checkPoolIsValid(Set<MuleConsumer> muleConsumers) throws ConsumerPoolClosedException;
}
