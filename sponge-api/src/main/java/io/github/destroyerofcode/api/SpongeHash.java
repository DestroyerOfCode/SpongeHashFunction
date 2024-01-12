package io.github.destroyerofcode.api;

/**
 * Interface defining the SpongeHash operations. It extends the Hash interface and includes
 * additional methods specific to sponge construction.
 */
public interface SpongeHash<T> extends Hash<T> {

  /**
   * If the original message is shorter than n bits (200 for keccak_200 etc...), this method creates
   * a new array with correct size length
   *
   * @return the original message with additional bits added to its end (only 0s)
   */
  T applyPadding(final T message);

  /**
   * Initializes the state of the hash function.
   *
   * @return The initialized state as a long array.
   */
  T initState();

  /**
   * Absorbs the given message into the state.
   *
   * @param state The current state of the hash function.
   * @param message The message to be absorbed.
   */
  void absorb(final T state, final T message);

  /**
   * Squeezes the hash value out of the given state.
   *
   * @param message The state from which the hash is to be squeezed.
   * @return The squeezed hash value.
   */
  T squeeze(final T message);

  T squeeze(final T message, final int outputOffsetPosition);
}
