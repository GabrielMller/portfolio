package org.mule.extension.kafka.internal.model.serializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerByteArray implements Serializable {
   private static final Logger logger = LoggerFactory.getLogger(SerByteArray.class);
   private byte[] payload = null;

   public SerByteArray(InputStream stream) {
      try {
         if (stream != null) {
            this.payload = IOUtils.toByteArray(stream);
            stream.reset();
         }
      } catch (IOException var3) {
         logger.error("Failed to reset input stream.");
      }

   }

   public InputStream getInputStream() {
      return this.payload != null ? new ByteArrayInputStream(this.payload) : null;
   }
}
