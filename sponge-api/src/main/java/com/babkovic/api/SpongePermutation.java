package com.babkovic.api;

/**
 * The state can be viewed as a 3-dimensional array as shown. The state array consists of b = 5×5×w
 * bits, where w = 2**l The w bits for a given (x, y) coordinate are called a lane (i.e., the bits
 * in the word along the z-axis). source: <a
 * href="https://www.crypto-textbook.com/download/Understanding-Cryptography-Keccak.pdf"></a>
 */
public interface SpongePermutation extends Permutation {
  void theta(final byte[] state);

  void rhoPi(final byte[] state);

  void chi(final byte[] state);

  void iota(final byte[] state, final int round);
}
