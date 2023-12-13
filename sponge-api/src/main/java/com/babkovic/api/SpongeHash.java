package com.babkovic.api;

public interface SpongeHash extends Hash {

  /**
   * If the original message is shorter than n bits (200 for keccak_200 etc...), this method creates
   * a new array with correct size length
   *
   * @return the original message with additional bits added to its end (only 0s)
   */
  byte[] applyPadding(final byte[] message);

  void initState(final byte[] message);

  void absorb(final byte[] state, final byte[] message);

  byte[] squeeze(final byte[] message);
}
