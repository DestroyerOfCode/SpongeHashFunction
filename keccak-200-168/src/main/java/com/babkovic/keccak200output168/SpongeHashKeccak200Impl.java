package com.babkovic.keccak200output168;

import static com.babkovic.common.Utils.nearestGreaterMultiple;
import static com.babkovic.keccak200output168.Constants.BYTES_IN_r;

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
    final byte[] messageBlock = new byte[BYTES_IN_r];

    message = applyPadding(message);

    for (int i = 0; i < message.length; i += BYTES_IN_r) {
      // message block is the 168 bits (21 bytes)
      // from the original message copy 168 bits to the message block
      System.arraycopy(message, i, messageBlock, 0, BYTES_IN_r);
      absorb(state, messageBlock);
    }

    return squeeze(state);
  }

  @Override
  public byte[] hash(final InputStream message, final int messageSizeBytes) {
    /* b is size in bits, 8 is size of byte on every architecture.
    So if b=200, it allocates 25 bytes */
    final byte[] state = initState();

    try {
      // message block is the 168 bits (21 bytes)
      // from the original message copy 168 bits to the message block
      for (int i = 0; messageSizeBytes > i; i += BYTES_IN_r) {
        byte[] messageBlock = new byte[BYTES_IN_r];
        int readBytes = message.readNBytes(messageBlock, 0, BYTES_IN_r);

        if (readBytes < BYTES_IN_r) {
          messageBlock = applyPadding(messageBlock);
        }
        absorb(state, messageBlock);
      }
      return squeeze(state);
    } catch (IOException e) {
      throw new SpongeException("An error has occurred when hashing: ", e);
    }
  }

  @Override
  public byte[] applyPadding(final byte[] message) {
    int originalLength = message.length;
    int paddedLength = nearestGreaterMultiple(originalLength, BYTES_IN_r); // 136

    final byte[] paddedMessage = new byte[paddedLength];
    System.arraycopy(message, 0, paddedMessage, 0, originalLength);

    return paddedMessage;
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
    final byte[] retArr = new byte[BYTES_IN_r];
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
