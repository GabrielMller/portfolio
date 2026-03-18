package org.mule.extension.kafka.internal.model.serializer;

import java.io.InputStream;
import java.util.Optional;
import org.apache.kafka.common.serialization.Serializer;
import org.mule.runtime.api.util.IOUtils;

public class InputStreamSerializer implements Serializer<InputStream> {
   private static final boolean USE_NULL = Boolean.parseBoolean(System.getProperty("mule.kafka.publish.useNull", "false"));

   public byte[] serialize(String topic, InputStream inputStream) {
      return (byte[])Optional.ofNullable(inputStream).map(IOUtils::toByteArray).orElseGet(() -> {
         return USE_NULL ? null : new byte[0];
      });
   }
}
