package com.babkovic.keccak1600output256;

import static com.babkovic.common.Constants.BITS_IN_BYTE;
import static com.babkovic.common.Constants.BITS_IN_LONG;
import static com.babkovic.common.Utils.nearestGreaterMultiple;
import static com.babkovic.keccak1600output256.Constants.OUTPUT_LENGTH_BITS;
import static com.babkovic.keccak1600output256.Constants.r;

import com.babkovic.api.SpongeHash;
import com.babkovic.api.SpongePermutation;
import com.babkovic.exception.SpongeException;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

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
    final long[] state = initState();
    final long[] messageBlock = new long[r / BITS_IN_LONG];

    message = applyPadding(message);

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

    final long[] state = initState();
    ; // 25
    long[] messageBlock = new long[r / BITS_IN_LONG]; // 17
    final DataInputStream message = new DataInputStream(messageStream);

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

  @Override
  public long[] initState() {
    return new long[] {
      1512438630783188661L,
      8198716176144688777L,
      1637884160694766545L,
      1763068439375808596L,
      3323231908170204617L,
      631703399715668548L,
      1530395573530499759L,
      1483770803502517068L,
      6746053807561825751L,
      8289798442804673757L,
      6229518946956962360L,
      265492940621606881L,
      6133650358006886469L,
      1640423390081412490L,
      1218238834902968216L,
      1886528811272012332L,
      644790174577108009L,
      1673022507320370160L,
      4996237436508233008L,
      1786917405949476368L,
      4331592739472745193L,
      1624126196739263612L,
      3883847296014053403L,
      7589786689989931013L,
      7492938352285470026L
    };
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
