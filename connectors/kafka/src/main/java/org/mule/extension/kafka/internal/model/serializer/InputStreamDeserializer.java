package org.mule.extension.kafka.internal.model.serializer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import org.apache.kafka.common.serialization.Deserializer;

public class InputStreamDeserializer implements Deserializer<InputStream> {
   public InputStream deserialize(String s, byte[] bytes) {
      return new ByteArrayInputStream((byte[])Optional.ofNullable(bytes).orElseGet(() -> {
         return new byte[0];
      }));
   }
}
