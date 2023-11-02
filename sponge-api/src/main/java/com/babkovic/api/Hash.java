package com.babkovic.api;

import java.io.InputStream;
import java.io.OutputStream;

public interface Hash {
  /**
   * this method returns the hashed message
   *
   * @param message this can be arbitrary length
   * @return a hashed message of sizes either 224, 256, 384 or 512 bits which makes up for a byte
   *     array of sizes 28, 32, 48 or 64 respectively
   */
  byte[] hash(byte[] message);

  byte[] hash(final InputStream message, final int messageSize);
}
