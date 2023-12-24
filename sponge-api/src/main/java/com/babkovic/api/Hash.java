package com.babkovic.api;

import java.io.InputStream;

public interface Hash<T> {
  /**
   * this method returns the hashed message
   *
   * @param message this can be arbitrary length
   * @return a hashed message of sizes either 224, 256, 384 or 512 bits which makes up for a byte
   *     array of sizes 28, 32, 48 or 64 respectively
   */
  T hash(T message);

  T hash(final InputStream message, final int messageSize);
}
