package com.babkovic.keccak_200;

import static com.babkovic.keccak_200.Constants.*;

import com.babkovic.hash.SpongeHash;
import com.babkovic.hash.SpongePermutation;
import java.util.Arrays;

public class SpongeHashKeccak200Impl implements SpongeHash {

  private final SpongePermutation spongePermutation;

  public SpongeHashKeccak200Impl(final SpongePermutation spongePermutation) {
    this.spongePermutation = spongePermutation;
  }

  @Override
  public byte[] hash(byte[] message) {
    /* b is size in bits, 8 is size of byte on every architecture.
    So if b=200, it allocates 25 bytes */
    final byte[] state = new byte[b / BITS_IN_BYTE];

    message = applyPadding(message);
    initState(state);

    for (int i = 0; i < message.length; i += r / BITS_IN_BYTE) {
      // message block is the 168 bits (21 bytes)
      final byte[] messageBlock = new byte[r / BITS_IN_BYTE];

      // from the original message copy 168 bits to the message block
      System.arraycopy(message, i, messageBlock, 0, messageBlock.length);
      absorb(state, messageBlock);
    }

    return squeeze(state);
  }

  @Override
  public byte[] applyPadding(final byte[] message) {
    if (message.length < r / BITS_IN_BYTE) {
      final byte[] paddedMessage = new byte[r / BITS_IN_BYTE];
      System.arraycopy(message, 0, paddedMessage, 0, message.length);
      return paddedMessage;
    }
    return message;
  }


  @Override
  public byte[] initState(final byte[] state) {
    if (STATE_BYTE_LENGTH != state.length) {
      throw new RuntimeException("Incorrect size of state. Should be 25.");
    }

    // in later stages apply different initial values for improved security. this is just pro forma
    Arrays.fill(state, (byte) 0b01010101);

    return state;
  }

  @Override
  public byte[] absorb(final byte[] state, final byte[] message) {
    mixStateAndMessage(state, message);
    spongePermutation.permute(state);

    return state;
  }

  @Override
  public byte[] squeeze(final byte[] message) {
    final byte[] retArr = new byte[r / BITS_IN_BYTE];
    // use the first r bits to squeeze out the output
    System.arraycopy(message, 0, retArr, 0, retArr.length);

    return retArr;
  }

  /**
   * mixing the message block with the current state. this methods xors first 168 bits of the state
   * with first 168 bits of the message. 168 bits because that is the length of r of the message.
   */
  private static void mixStateAndMessage(byte[] state, byte[] message) {
    for (int i = 0; i < message.length; i++) {
      state[i] = (byte) (message[i] ^ state[i]);
    }
  }
}
