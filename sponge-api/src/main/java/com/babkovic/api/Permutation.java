package com.babkovic.api;

/**
 * The Permutation interface provides a generalized framework for cryptographic permutation
 * operations. It's primarily used in cryptographic algorithms to ensure thorough mixing of the
 * input data, enhancing security by dispersing patterns and reducing predictability.
 *
 * @param <T> The type of data structure that represents the state (e.g., an array of bytes or
 *     longs). This type is used for the permutation operation.
 */
public interface Permutation<T> {

  /**
   * Here we receive the already mixed message block m with the state and return the permutated
   * state Performs a permutation operation on the given state. This method applies a series of
   * transformations to the input state, altering its structure in a predetermined but complex
   * manner.
   *
   * @param state The state array that is to be permuted. This array contains the data that
   *     undergoes the transformation during the permutation process.
   */
  void permute(final T state);
}
