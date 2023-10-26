package com.babkovic.babkovic.hash;

public interface SpongeHash extends Hash {

  /**
   * If the original message is shorter than n bits (200 for keccak_200 etc...),
   * this method creates a new array with correct size length
   * @return the original message with additional bits added to its end (only 0s)
   */
  byte[] applyPadding(final byte[] message);

  byte[] initState(final byte[] message);

  byte[] absorb(final byte[] state, final byte[] message);

  byte[] squeeze(final byte[] message);
}
