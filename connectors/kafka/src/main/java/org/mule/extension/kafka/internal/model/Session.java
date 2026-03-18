package org.mule.extension.kafka.internal.model;

import java.io.Closeable;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class Session<C> implements Closeable {
  private final UUID id = UUID.randomUUID();
  private final Consumer<C> checkInOperation;
  private C sessionElement;

  public Session(C sessionElement, Consumer<C> checkInOperation) {
    this.sessionElement = sessionElement;
    this.checkInOperation = checkInOperation;
  }

  public void run(Consumer<C> operation) {
    operation.accept(this.sessionElement);
  }

  public <T> T apply(Function<C, T> operation) {
    return operation.apply(this.sessionElement);
  }

  public UUID getId() {
    return this.id;
  }

  public void close() {
    this.checkInOperation.accept(this.sessionElement);
  }
}
