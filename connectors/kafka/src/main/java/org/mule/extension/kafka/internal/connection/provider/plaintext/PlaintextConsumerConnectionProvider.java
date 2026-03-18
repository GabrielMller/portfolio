package org.mule.extension.kafka.internal.connection.provider.plaintext;

import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.mule.extension.kafka.internal.connection.ConsumerConnection;
import org.mule.extension.kafka.internal.connection.provider.ConsumerConnectionProvider;
import org.mule.sdk.api.annotation.Alias;
import org.mule.sdk.api.annotation.param.display.DisplayName;
import org.mule.sdk.api.connectivity.CachedConnectionProvider;

@Alias("consumer-plaintext-connection")
@DisplayName("Consumer Plaintext Connection")
public class PlaintextConsumerConnectionProvider extends ConsumerConnectionProvider implements CachedConnectionProvider<ConsumerConnection> {
   public PlaintextConsumerConnectionProvider() {
      super(SecurityProtocol.PLAINTEXT);
   }
}
