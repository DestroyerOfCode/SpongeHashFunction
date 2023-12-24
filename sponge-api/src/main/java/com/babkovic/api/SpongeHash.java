package com.babkovic.api;

public interface SpongeHash<T> extends Hash<T> {

  /**
   * If the original message is shorter than n bits (200 for keccak_200 etc...), this method creates
   * a new array with correct size length
   *
   * @return the original message with additional bits added to its end (only 0s)
   */
  T applyPadding(final T message);

  void initState(final T message);

  void absorb(final T state, final T message);

  T squeeze(final T message);

  T squeeze(final T message, final int outputOffsetPosition);
}
