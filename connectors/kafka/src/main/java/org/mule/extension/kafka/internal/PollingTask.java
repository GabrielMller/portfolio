package org.mule.extension.kafka.internal;

import java.io.Closeable;
import java.time.Duration;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import org.apache.kafka.common.errors.WakeupException;
import org.mule.extension.kafka.api.source.AckMode;
import org.mule.extension.kafka.internal.connection.ConsumerConnection;
import org.mule.extension.kafka.internal.error.exception.NotFoundException;
import org.mule.extension.kafka.internal.error.exception.OperationTimeoutException;
import org.mule.extension.kafka.internal.model.consumer.MuleConsumer;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.message.ErrorType;
import org.mule.sdk.api.runtime.operation.Result;
import org.mule.sdk.api.runtime.source.SourceCallback;
import org.mule.sdk.api.runtime.source.SourceCallbackContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollingTask<P, A, I> implements Runnable, Closeable {
  private static final Logger logger = LoggerFactory.getLogger(PollingTask.class);
  private final SourceCallback<P, A> sourceCallback;
  private final BiFunction<String, I, Result<P, A>> parser;
  private final BiFunction<MuleConsumer, Duration, I> pollOperation;
  private final ConsumerConnection consumerConnection;
  private final AckMode ackMode;
  private final Duration pollTimeout;
  private final BiFunction<I, SourceCallbackContext, SourceCallbackContext> headersAction;
  private boolean running = true;
  private CountDownLatch closeCountDown = new CountDownLatch(1);

  public PollingTask(ConsumerConnection consumerConnection, AckMode ackMode, Duration pollTimeout,
      BiFunction<MuleConsumer, Duration, I> pollOperation, BiFunction<String, I, Result<P, A>> parser,
      SourceCallback<P, A> sourceCallback, BiFunction<I, SourceCallbackContext, SourceCallbackContext> headersAction) {
    this.consumerConnection = consumerConnection;
    this.ackMode = ackMode;
    this.pollTimeout = pollTimeout;
    this.parser = parser;
    this.pollOperation = pollOperation;
    this.sourceCallback = sourceCallback;
    this.headersAction = headersAction;
  }

  public void run() {
    logger.info("Starting the PollingTask with ackMode={}, pollTimeout={}", this.ackMode, this.pollTimeout);

    try {
      while (this.running) {
        try {

          Entry<String, I> entry = this.consumerConnection.poll(this.ackMode, this.pollTimeout, this.pollOperation);
          if (entry != null) {

            String key = (String) entry.getKey();
            SourceCallbackContext callbackContext = this.sourceCallback.createContext();
            if (key != null) {
              callbackContext.addVariable("sessionKey", key);
            }
            callbackContext = this.headersAction.apply(entry.getValue(), callbackContext);
            this.sourceCallback.handle((Result<P, A>) this.parser.apply(key, entry.getValue()), callbackContext);
            if (key == null) {
              logger.trace("Message(s) sent to flow.");
            } else {
              logger.trace("Message(s) with key '{}' sent to flow.", key);
            }
          }
        } catch (WakeupException ex) {
            if (logger.isDebugEnabled()) {
              logger.debug("Got a recoverable exception while running the polling task ({}})", ex, "Wakeup");
            }
        } catch (OperationTimeoutException ex) {
            if (logger.isDebugEnabled()) {
              logger.debug("Got a recoverable exception while running the polling task ({}})", ex, "Operation Timed out");
            }
        } catch (NotFoundException ex) {
            if (logger.isTraceEnabled()) {
              logger.trace("Did not get any results from Kafka for the last poll invocation");
            }
        } catch (ConnectionException ex) {
            logger.info("Got an unrecoverable exception while running the polling task", ex);
            this.running = false;
            this.sourceCallback.onConnectionException(ex);
        } catch (RuntimeException ex) {
            logger.info("Got an unrecoverable exception while running the polling task", ex);
            this.running = false;
            this.sourceCallback.onConnectionException(new ConnectionException("Got an unrecoverable exception while running the polling task", ex, (ErrorType)null, this.consumerConnection));
        }
      }

      logger.info("Finished the PollingTask normally with ackMode={}, pollTimeout={}", this.ackMode, this.pollTimeout);
    } finally {
      this.closeCountDown.countDown();
    }

  }

  public void close() {
    this.running = false;

    try {
      if (!this.closeCountDown.await(30L, TimeUnit.SECONDS)) {
        logger.warn("Polling task timeout while waiting to be closed");
      }
    } catch (InterruptedException var2) {
      logger.warn("PollingTask was interrupted while closing");
    }
  }
}
