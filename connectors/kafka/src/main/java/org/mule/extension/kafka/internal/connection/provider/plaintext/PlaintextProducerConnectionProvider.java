package org.mule.extension.kafka.internal.connection.provider.plaintext;

import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.mule.extension.kafka.internal.connection.ProducerConnection;
import org.mule.extension.kafka.internal.connection.provider.ProducerConnectionProvider;
import org.mule.sdk.api.annotation.Alias;
import org.mule.sdk.api.annotation.param.display.DisplayName;
import org.mule.sdk.api.connectivity.CachedConnectionProvider;

@Alias("producer-plaintext-connection-provider")
@DisplayName("Producer Plaintext Connection")
public class PlaintextProducerConnectionProvider extends ProducerConnectionProvider implements CachedConnectionProvider<ProducerConnection> {
   public PlaintextProducerConnectionProvider() {
      super(SecurityProtocol.PLAINTEXT);
   }

}

