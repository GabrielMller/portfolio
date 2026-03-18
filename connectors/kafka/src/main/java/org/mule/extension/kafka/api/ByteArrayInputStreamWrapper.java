package org.mule.extension.kafka.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;

public class ByteArrayInputStreamWrapper extends ByteArrayInputStream {
   public ByteArrayInputStreamWrapper(byte[] buf) {
      super(buf);
   }

   public InputStream getKey() {
      return new ByteArrayInputStream(super.buf);
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.buf});
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         return this.buf == ((ByteArrayInputStreamWrapper)obj).buf;
      } else {
         return false;
      }
   }
}