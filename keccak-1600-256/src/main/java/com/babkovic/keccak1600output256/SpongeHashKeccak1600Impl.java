package com.babkovic.keccak1600output256;

import static com.babkovic.keccak1600output256.Constants.BITS_IN_BYTE;
import static com.babkovic.keccak1600output256.Constants.OUTPUT_LENGTH_BITS;
import static com.babkovic.keccak1600output256.Constants.STATE_BYTE_LENGTH;
import static com.babkovic.keccak1600output256.Constants.b;
import static com.babkovic.keccak1600output256.Constants.r;

import com.babkovic.api.SpongeHash;
import com.babkovic.api.SpongePermutation;
import com.babkovic.exception.SpongeException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class SpongeHashKeccak1600Impl implements SpongeHash {

  private final SpongePermutation spongePermutation;

  public SpongeHashKeccak1600Impl(final SpongePermutation spongePermutation) {
    this.spongePermutation = spongePermutation;
  }

  @Override
  public byte[] hash(byte[] message) {
    /* b is size in bits, 8 is size of byte on every architecture.
    So if b=200, it allocates 25 bytes */
    final byte[] state = new byte[b / BITS_IN_BYTE];
    final byte[] messageBlock = new byte[r / BITS_IN_BYTE];

    message = applyPadding(message);
    initState(state);

    try {
      for (int i = 0; i < message.length; i += r / BITS_IN_BYTE) {
        // message block is the 1152 bits (21 bytes)
        // from the original message copy 1152 bits to the message block
        System.arraycopy(message, i, messageBlock, 0, r / BITS_IN_BYTE);
        absorb(state, messageBlock);
      }

      return squeeze(state);
    } catch (Exception e) {
      throw new SpongeException("An error has occurred when hashing:", e);
    }
  }

  @Override
  public byte[] hash(final InputStream message, final int messageSize) {
    /* b is size in bits, 8 is size of byte on every architecture.
    So if b=1600, it allocates 200 bytes */
    final byte[] state = new byte[b / BITS_IN_BYTE];
    final byte[] messageBlock = new byte[r / BITS_IN_BYTE];
    initState(state);

    try {
      // message block is the 1152 bits (21 bytes)
      // from the original message copy 1152 bits to the message block
      for (int i = 0; messageSize > i; i += r / BITS_IN_BYTE) {
        message.readNBytes(messageBlock, 0, r / BITS_IN_BYTE);
        absorb(state, messageBlock);
      }

      // returns first
      return squeeze(state);
    } catch (IOException e) {
      throw new SpongeException("An error has occurred when hashing:", e);
    }
  }

  @Override
  public byte[] applyPadding(final byte[] message) {
    if (message.length < r / BITS_IN_BYTE) {
      final byte[] paddedMessage = new byte[r / BITS_IN_BYTE];
      System.arraycopy(message, 0, paddedMessage, 0, message.length);
      return paddedMessage;
    }

    int messageLengthOffsetInBytes = (message.length) % (r / BITS_IN_BYTE);
    if (messageLengthOffsetInBytes != 0) {
      // we need to add as many bytes as we need for the closes multiple of 1088 bits (136 bytes
      // resp.)
      // we get that by message.length + (r / BITS_IN_BYTE - messageLengthOffsetInBytes)
      final byte[] paddedMessage =
          new byte[message.length + (r / BITS_IN_BYTE - messageLengthOffsetInBytes)];
      System.arraycopy(message, 0, paddedMessage, 0, message.length);
      return paddedMessage;
    }
    return message;
  }

  @Override
  public void initState(final byte[] state) {
    if (STATE_BYTE_LENGTH != state.length) {
      throw new RuntimeException(
          String.format("Incorrect size of state. Should be %d.", STATE_BYTE_LENGTH));
    }

    // in later stages apply different initial values for improved security. this is just pro forma
    Arrays.fill(state, (byte) 0b01010101);
  }

  @Override
  public void absorb(final byte[] state, final byte[] message) {
    mixStateAndMessage(state, message);
    spongePermutation.permute(state);
  }

  @Override
  public byte[] squeeze(final byte[] message) {
    return squeeze(message, 0);
  }

  @Override
  public byte[] squeeze(final byte[] message, final int outputOffsetPosition) {
    final byte[] retArr = new byte[OUTPUT_LENGTH_BITS / BITS_IN_BYTE];
    // use the first r bits to squeeze out the output
    System.arraycopy(message, 0, retArr, outputOffsetPosition, retArr.length);

    return retArr;
  }

  /**
   * mixing the message block with the current state. this methods xors first 1152 bits of the state
   * with first 1152 bits of the message. 1152 bits because that is the length of r of the message.
   */
  private static void mixStateAndMessage(byte[] state, byte[] message) {
    for (int i = 0; i < message.length; i++) {
      state[i] = (byte) (message[i] ^ state[i]);
    }
  }
}
