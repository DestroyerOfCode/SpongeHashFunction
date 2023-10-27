package com.babkovic.api;

public interface SpongePermutation extends Permutation {
  void theta(final byte[] state);

  void rhoPi(final byte[] state);

  void chi(final byte[] state);

  void iota(final byte[] state, final int round);
}
