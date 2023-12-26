package com.babkovic.api;

import java.io.InputStream;

/**
 * Interface defining the basic operations of a hash function.
 * Provides methods to compute a hash from a message or a stream of data.
 */
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
