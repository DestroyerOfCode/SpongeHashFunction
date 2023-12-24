package com.babkovic.api;

/**
 * The state can be viewed as a 3-dimensional array as shown. The state array consists of b = 5×5×w
 * bits, where w = 2**l The w bits for a given (x, y) coordinate are called a lane (i.e., the bits
 * in the word along the z-axis). source: <a
 * href="https://www.crypto-textbook.com/download/Understanding-Cryptography-Keccak.pdf"></a>
 */
public interface SpongePermutation<T> extends Permutation<T> {
  void theta(final T state);

  void rhoPi(final T state);

  void chi(final T state);

  void iota(final T state, final int round);
}
