package org.mule.extension.kafka.internal.source;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.sdk.api.runtime.operation.Result;
import org.mule.sdk.api.runtime.source.SourceCallback;
import org.mule.sdk.api.runtime.source.SourceCallbackContext;

public class SourceCallbackWrapper<P, A> implements SourceCallback<P, A> {
   private final SourceCallback<P, A> sourceCallback;

   public SourceCallbackWrapper(SourceCallback<P, A> sourceCallback) {
      this.sourceCallback = sourceCallback;
   }

   public void handle(Result<P, A> result) {
      this.sourceCallback.handle(result);
   }

   public void handle(Result<P, A> result, SourceCallbackContext context) {
      this.sourceCallback.handle(result, context);
   }

   public void onConnectionException(ConnectionException e) {
      try {
         synchronized(this) {
            this.wait(2000L);
         }
      } catch (InterruptedException var5) {
         Thread.currentThread().interrupt();
      }

      this.sourceCallback.onConnectionException(e);
   }

   public SourceCallbackContext createContext() {
      return this.sourceCallback.createContext();
   }
}