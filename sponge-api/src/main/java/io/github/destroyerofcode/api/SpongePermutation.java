package io.github.destroyerofcode.api;

/**
 * The SpongePermutation interface represents the core permutation operations used in the Sponge
 * construction of cryptographic hash functions like Keccak. It defines the standard permutation
 * steps such as theta, rhoPi, chi, and iota, which are fundamental to the sponge function's
 * internal mixing and transformation process. The state can be viewed as a 3-dimensional array as
 * shown. The state array consists of b = 5×5×w bits, where w = 2**l The w bits for a given (x, y)
 * coordinate are called a lane (i.e., the bits in the word along the z-axis). source: <a
 * href="https://www.crypto-textbook.com/download/Understanding-Cryptography-Keccak.pdf"></a>
 *
 * @param <T> The type of the state array on which the permutation operations are performed. This
 *     could typically be an array of bytes or longs or any other number data type, depending on the
 *     implementation.
 */
public interface SpongePermutation<T> extends Permutation<T> {
  /**
   * Performs the theta step on the state, a mixing step that XORs each bit in the state with the
   * parity of two columns.
   *
   * @param state The current state of the permutation.
   */
  void theta(final T state);

  /**
   * Performs the rhoPi step on the state, a mixing step involving bit rotations and lane
   * rearrangements.
   *
   * @param state The current state of the permutation.
   */
  void rhoPi(final T state);

  /**
   * Performs the chi step on the state, a non-linear step that processes bits in each row of the
   * state.
   *
   * @param state The current state of the permutation.
   */
  void chi(final T state);

  /**
   * Performs the iota step on the state, introducing a round constant to avoid symmetry.
   *
   * @param state The current state of the permutation.
   * @param round The current round index, used for determining the round constant.
   */
  void iota(final T state, final int round);
}
