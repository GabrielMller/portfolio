package org.mule.extension.kafka.internal;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.mule.extension.kafka.api.source.AckMode;
import org.mule.extension.kafka.internal.config.ConsumerConfig;
import org.mule.extension.kafka.internal.connection.ConsumerConnection;
import org.mule.extension.kafka.internal.source.SourceCallbackWrapper;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.sdk.api.annotation.execution.OnError;
import org.mule.sdk.api.annotation.execution.OnSuccess;
import org.mule.sdk.api.annotation.execution.OnTerminate;
import org.mule.sdk.api.annotation.param.Config;
import org.mule.sdk.api.annotation.param.ConfigOverride;
import org.mule.sdk.api.annotation.param.Connection;
import org.mule.sdk.api.annotation.param.Optional;
import org.mule.sdk.api.annotation.param.Parameter;
import org.mule.sdk.api.annotation.param.display.DisplayName;
import org.mule.sdk.api.annotation.param.display.Summary;
import org.mule.sdk.api.runtime.source.Source;
import org.mule.sdk.api.runtime.source.SourceCallback;
import org.mule.sdk.api.runtime.source.SourceCallbackContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MessageListenerSource<P, A> extends Source<P, A> {
  private static final Logger logger = LoggerFactory.getLogger(MessageListenerSource.class);
  @Config
  protected ConsumerConfig config;
  @Connection
  private ConnectionProvider<ConsumerConnection> connectionProvider;
  @Inject
  protected SchedulerService schedulerService;
  @Summary("The timeout for the poll")
  @ConfigOverride
  @Optional
  @Parameter
  @DisplayName("Poll timeout")
  private int pollTimeout;
  @Summary("Time unit for poll timeout.")
  @ConfigOverride
  @Optional
  @Parameter
  @DisplayName("Poll timeout time unit")
  private TimeUnit pollTimeoutTimeUnit;
  @Summary("Declares the kind of Acknowledgement mode supported.")
  @ConfigOverride
  @Optional
  @Parameter
  @DisplayName("Acknowledgement mode")
  private AckMode ackMode;
  @Parameter
  @DisplayName("Amount of parallel consumers")
  @Optional(defaultValue = "1")
  private int parallelConsumersAmount;
  private List<PollingTask<P, A, ?>> pollingTasks;
  private ConsumerConnection connection;

  public void onStart(SourceCallback<P, A> sourceCallback) throws MuleException {
    SourceCallbackWrapper<P, A> sourceCallbackWrapper = new SourceCallbackWrapper<>(sourceCallback);
    this.connection = this.connectionProvider.connect();

    this.pollingTasks = new ArrayList<>();

    for (int i = 0; i < this.parallelConsumersAmount; ++i) {
      this.createPollingTask(this.pollingTasks::add, this.config, this.connection, sourceCallbackWrapper, this.ackMode,
          this.config.asDuration((long) this.pollTimeout, this.pollTimeoutTimeUnit));
      this.connection.startPolling(this.pollingTasks.get(i));
    }
  }

  public abstract void createPollingTask(Consumer<PollingTask<P, A, ?>> consumer, ConsumerConfig config,
      ConsumerConnection connection, SourceCallback<P, A> sourceCallback, AckMode ackMode, Duration pollingTimeout);

  @OnSuccess
  public void onSuccess(SourceCallbackContext context) {
    if (this.ackMode.equals(AckMode.AUTO)) {
      try {
        this.connection.commit(this.ackMode, (String) context.getVariable("sessionKey").orElse(""));
      } catch (Exception ex) {
        logger.error("Failed to commit offsets in source", ex);
        context.getSourceCallback().onConnectionException(new ConnectionException(ex, context.getConnection()));
      }
    }

  }

  @OnError
  public void onFailure(SourceCallbackContext context) {
    if (this.ackMode.equals(AckMode.MANUAL)) {
      logger.warn("The flow failed, the listener will consume the same messages if the commit was not invoked");
      this.connection.refreshBuffer(AckMode.MANUAL, (String) context.getVariable("sessionKey").orElse(""));
    } else if (this.ackMode.equals(AckMode.AUTO)) {
      logger.warn("The flow failed, the listener will consume the same messages");
      this.connection.refreshBuffer(AckMode.AUTO, (String) context.getVariable("sessionKey").orElse(""));
    }

  }

  @OnTerminate
  public void onTerminate(SourceCallbackContext context) {
    if (this.ackMode.equals(AckMode.MANUAL)) {
      this.connection.release(this.ackMode, (String) context.getVariable("sessionKey").orElse(""));
    } else if (this.ackMode.equals(AckMode.AUTO)) {
      this.connection.release(this.ackMode, (String) context.getVariable("sessionKey").orElse(""));
    }
  }

  public void onStop() {
    if (this.pollingTasks != null) {
      this.pollingTasks.stream().map((pollingTask) -> {
        return this.schedulerService.cpuIntensiveScheduler().submit(() -> {
          IOUtils.closeQuietly(pollingTask);
        });
      }).collect(Collectors.toList()).forEach((x) -> {
        try {
          x.get();
        } catch (ExecutionException | InterruptedException var3) {
          logger.error("There was an error Closing Polling Task", this.getClass().getSimpleName());
        }

      });
    }
  }
}
