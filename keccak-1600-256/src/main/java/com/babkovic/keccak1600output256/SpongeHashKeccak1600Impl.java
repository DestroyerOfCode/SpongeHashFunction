package com.babkovic.keccak1600output256;

import static com.babkovic.keccak1600output256.Constants.BITS_IN_BYTE;
import static com.babkovic.keccak1600output256.Constants.BITS_IN_LONG;
import static com.babkovic.keccak1600output256.Constants.OUTPUT_LENGTH_BITS;
import static com.babkovic.keccak1600output256.Constants.STATE_LONG_LENGTH;
import static com.babkovic.keccak1600output256.Constants.b;
import static com.babkovic.keccak1600output256.Constants.r;

import com.babkovic.api.SpongeHash;
import com.babkovic.api.SpongePermutation;
import com.babkovic.exception.SpongeException;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class SpongeHashKeccak1600Impl implements SpongeHash<long[]> {

  private final SpongePermutation<long[]> spongePermutation;

  public SpongeHashKeccak1600Impl(final SpongePermutation<long[]> spongePermutation) {
    this.spongePermutation = spongePermutation;
  }

  @Override
  public long[] hash(long[] message) {
    /* b is size in bits, 64 is size of Long on every architecture.
    So if b=1600, it allocates 25 Longs
    and if r=1088, it allocated 18 Longs
    */
    final long[] state = new long[b / BITS_IN_LONG];
    final long[] messageBlock = new long[r / BITS_IN_LONG];

    message = applyPadding(message);
    initState(state);

    try {
      for (int i = 0; i < message.length; i += r / BITS_IN_LONG) {
        // message block is the 1088 bits (17 Longs)
        // from the original message copy 1088 bits to the message block
        System.arraycopy(message, i, messageBlock, 0, r / BITS_IN_LONG);
        absorb(state, messageBlock);
      }

      return squeeze(state);
    } catch (Exception e) {
      throw new SpongeException("An error has occurred when hashing: ", e);
    }
  }

  @Override
  public long[] hash(final InputStream messageStream, final int messageSizeBytes) {
    /* b is size in bits, 64 is size of Long on every architecture.
    So if b=1600, it allocates 25 Longs
    and if r=1088, it allocated 18 Longs
    */

    final long[] state = new long[b / BITS_IN_LONG]; // 25
    long[] messageBlock = new long[r / BITS_IN_LONG]; // 17
    final DataInputStream message = new DataInputStream(messageStream);

    initState(state);
    try {
      // message block is the 1088 bits (17 bytes)
      // from the original message copy 1088 bits to the message block
      for (int i = 0; messageSizeBytes > i; i += r / BITS_IN_BYTE) {

        final int bytesToRead = Math.min(r / BITS_IN_BYTE, messageSizeBytes);
        byte[] bytesRead = new byte[bytesToRead];
        int read = message.read(bytesRead);

        if (read < r / BITS_IN_BYTE) {
          bytesRead = applyPadding(bytesRead);
        }

        final ByteBuffer buffer = ByteBuffer.wrap(bytesRead);
        for (int j = 0; j < r / BITS_IN_LONG; j++) {
          messageBlock[j] = buffer.getLong();
        }

        absorb(state, messageBlock);
      }

      // returns first
      return squeeze(state);
    } catch (IOException e) {
      throw new SpongeException("An error has occurred when hashing:", e);
    }
  }

  @Override
  public long[] applyPadding(final long[] message) {
    int originalLength = message.length;
    int paddedLength = nearestGreaterMultiple(originalLength, r / BITS_IN_LONG); // 17

    final long[] paddedMessage = new long[paddedLength];
    System.arraycopy(message, 0, paddedMessage, 0, originalLength);

    return paddedMessage;
  }

  public byte[] applyPadding(final byte[] message) {
    int originalLength = message.length;
    int paddedLength = nearestGreaterMultiple(originalLength, r / BITS_IN_BYTE); // 136

    final byte[] paddedMessage = new byte[paddedLength];
    System.arraycopy(message, 0, paddedMessage, 0, originalLength);

    return paddedMessage;
  }

  /**
   * Calculates the nearest multiple of a number that is greater than the array size.
   *
   * @param arraySize The size of the array.
   * @param number The number for which the nearest multiple is to be found.
   * @return The nearest multiple of the number that is greater than the array size.
   */
  private static int nearestGreaterMultiple(int arraySize, int number) {
    if (number <= 0) {
      throw new IllegalArgumentException("Number must be greater than 0.");
    }

    int multiple = (arraySize / number) * number;
    if (multiple < arraySize) {
      multiple += number;
    }
    return multiple;
  }

  @Override
  public void initState(final long[] state) {
    if (STATE_LONG_LENGTH != state.length) {
      throw new RuntimeException(
          String.format(
              "Incorrect size of state. Should be %d Bytes (%d Longs) (%d bits).",
              STATE_LONG_LENGTH * Long.BYTES, STATE_LONG_LENGTH, b));
    }

    // in later stages apply different initial values for improved security. this is just pro forma
    Arrays.fill(state, 1431655765L);
  }

  @Override
  public void absorb(final long[] state, final long[] message) {
    mixStateAndMessage(state, message);
    spongePermutation.permute(state);
  }

  @Override
  public long[] squeeze(final long[] message) {
    return squeeze(message, 0);
  }

  @Override
  public long[] squeeze(final long[] message, final int outputOffsetPosition) {
    final long[] retArr = new long[OUTPUT_LENGTH_BITS / BITS_IN_LONG];
    // use the first r bits to squeeze out the output
    System.arraycopy(message, 0, retArr, outputOffsetPosition, retArr.length);

    return retArr;
  }

  /**
   * mixing the message block with the current state. this methods xors first 1088 bits of the state
   * with first 1088 bits of the message. 1088 bits because that is the length of r of the message.
   */
  private static void mixStateAndMessage(final long[] state, final long[] message) {
    for (int i = 0; i < message.length; i++) {
      state[i] = message[i] ^ state[i];
    }
  }
}
