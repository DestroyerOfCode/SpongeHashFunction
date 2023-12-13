package com.babkovic.api;

public interface Permutation {

  /**
   * here we receive the already mixed message block m with the state and return the permutated
   * state
   *
   * @param state the mixed message block m with the state
   */
  void permute(final byte[] state);
}
