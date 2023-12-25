package com.babkovic;

import static com.babkovic.common.Constants.BITS_IN_BYTE;
import static com.babkovic.common.Constants.BYTES_IN_LONG;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.function.Executable;

public class TestUtils {

  public static Executable verifyArraysAreEqual(final long[] arr1, final long[] arr2) {
    return () ->
        assertAll(
            () -> assertEquals(arr1.length, arr2.length),
            () -> {
              for (int i = 0; i < arr1.length; i++) {
                assertEquals(arr2[i], arr1[i]);
              }
            });
  }

  public static Executable verifyArraysAreEqual(final byte[] arr1, final byte[] arr2) {
    return () ->
        assertAll(
            () -> assertEquals(arr1.length, arr2.length),
            () -> {
              for (int i = 0; i < arr1.length; i++) {
                assertEquals(arr2[i], arr1[i]);
              }
            });
  }

  public static Executable hashAndAssertOutputSize(
      final long[] message, final int outputLengthBytes) {
    return () -> assertEquals(outputLengthBytes, message.length);
  }

  public static Executable hashAndAssertOutputSize(
      final byte[] message, final int outputLengthBytes) {
    return () -> assertEquals(outputLengthBytes, message.length);
  }

  public static int calculateNumberOfAbsorbIterations(final int fileSize, final int r) {
    return (int) Math.ceil((double) (fileSize * BITS_IN_BYTE) / r);
  }

  public static byte[] toByteArray(final InputStream inputStream, final int available)
      throws IOException {
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int nRead;
    final byte[] data = new byte[available]; // Buffer size

    while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }

    buffer.flush();
    return buffer.toByteArray();
  }

  public static long[] byteArrayToLongArray(final byte[] bytes) {

    final long[] outBytes = new long[(int) Math.ceil((double) bytes.length / BYTES_IN_LONG)];

    final int padding = outBytes.length * BYTES_IN_LONG - bytes.length;
    final ByteBuffer buffer =
        ByteBuffer.allocate(bytes.length + padding).put(bytes).put(new byte[padding]);
    buffer.flip();
    buffer.asLongBuffer().get(outBytes);
    return outBytes;
  }
}
