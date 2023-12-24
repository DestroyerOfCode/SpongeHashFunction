package com.babkovic.api;

public interface Permutation<T> {

  /**
   * here we receive the already mixed message block m with the state and return the permutated
   * state
   *
   * @param state the mixed message block m with the state
   */
  void permute(final T state);
}
