package com.babkovic.keccak200output168;

import static com.babkovic.keccak200output168.Constants.BITS_IN_BYTE;
import static com.babkovic.keccak200output168.Constants.r;

import com.babkovic.api.SpongeHash;
import com.babkovic.api.SpongePermutation;
import com.babkovic.exception.SpongeException;
import java.io.IOException;
import java.io.InputStream;

public class SpongeHashKeccak200Impl implements SpongeHash<byte[]> {

  private final SpongePermutation<byte[]> spongePermutation;

  public SpongeHashKeccak200Impl(final SpongePermutation<byte[]> spongePermutation) {
    this.spongePermutation = spongePermutation;
  }

  @Override
  public byte[] hash(byte[] message) {
    /* b is size in bits, 8 is size of byte on every architecture.
    So if b=200, it allocates 25 bytes */
    final byte[] state = initState();
    final byte[] messageBlock = new byte[r / BITS_IN_BYTE];

    message = applyPadding(message);

    for (int i = 0; i < message.length; i += r / BITS_IN_BYTE) {
      // message block is the 168 bits (21 bytes)
      // from the original message copy 168 bits to the message block
      System.arraycopy(message, i, messageBlock, 0, r / BITS_IN_BYTE);
      absorb(state, messageBlock);
    }

    return squeeze(state);
  }

  @Override
  public byte[] hash(final InputStream message, final int messageSize) {
    /* b is size in bits, 8 is size of byte on every architecture.
    So if b=200, it allocates 25 bytes */
    final byte[] state = initState();
    final byte[] messageBlock = new byte[r / BITS_IN_BYTE];

    try {
      // message block is the 168 bits (21 bytes)
      // from the original message copy 168 bits to the message block
      for (int i = 0; messageSize > i; i += r / BITS_IN_BYTE) {
        message.readNBytes(messageBlock, 0, r / BITS_IN_BYTE);
        absorb(state, messageBlock);
      }
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
      // we need to add as many bytes as we need for the closes multiple of 168 bits (21 bytes
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
  public byte[] initState() {
    // random 25 bytes
    return new byte[] {
      113, -77, 65, -26, -43, -17, 83, -4, -15, -24, -116, -16, 120, -82, -89, -57, -39, 93, 59, 10,
      -92, 16, -119, -91, 1
    };
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
    final byte[] retArr = new byte[r / BITS_IN_BYTE];
    // use the first r bits to squeeze out the output
    System.arraycopy(message, 0, retArr, outputOffsetPosition, retArr.length);

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