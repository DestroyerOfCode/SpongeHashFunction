package com.babkovic.keccak1600output256;

import static com.babkovic.common.Utils.nearestGreaterMultiple;
import static com.babkovic.keccak1600output256.Constants.BITS_IN_BYTE;
import static com.babkovic.keccak1600output256.Constants.BITS_IN_LONG;
import static com.babkovic.keccak1600output256.Constants.BYTES_IN_LONG;
import static com.babkovic.keccak1600output256.Constants.OUTPUT_LENGTH_BITS;
import static com.babkovic.keccak1600output256.Constants.ROUNDS;
import static com.babkovic.keccak1600output256.Constants.r;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.babkovic.api.SpongeHash;
import com.babkovic.api.SpongePermutation;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.function.Executable;

class SpongeHash1600Output256ImplTest {

  private SpongePermutation<long[]> spongePermutationImpl;
  private SpongeHash<long[]> spongeHashKeccak1600;

  @BeforeEach
  void setUp() {
    spongePermutationImpl = spy(new PermutationImpl());
    spongeHashKeccak1600 = spy(new SpongeHashKeccak1600Impl(spongePermutationImpl));
  }

  @AfterEach
  void cleanUp() {
    spongePermutationImpl = null;
    spongeHashKeccak1600 = null;
  }

  private static void verifyArraysAreEqual(final long[] arr1, final long[] arr2) {
    assertEquals(arr1.length, arr2.length);
    for (int i = 0; i < arr1.length; i++) {
      assertEquals(arr2[i], arr1[i]);
    }
  }

  private static int calculateNumberOfAbsorbIterations(final int fileSize) {
    return (int) Math.ceil((double) (fileSize * BITS_IN_BYTE) / r);
  }

  private static byte[] toByteArray(final InputStream inputStream, final int available)
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

  private static long[] byteArrayToLongArray(final byte[] bytes) {

    final long[] outBytes = new long[(int) Math.ceil((double) bytes.length / BYTES_IN_LONG)];

    final int padding = outBytes.length * BYTES_IN_LONG - bytes.length;
    final ByteBuffer buffer =
        ByteBuffer.allocate(bytes.length + padding).put(bytes).put(new byte[padding]);
    buffer.flip();
    buffer.asLongBuffer().get(outBytes);
    return outBytes;
  }

  private static Executable hashAndAssertSize(final long[] message) {
    return () -> assertEquals(OUTPUT_LENGTH_BITS / BITS_IN_LONG, message.length);
  }

  @Test
  void shouldReturnOriginalMessage_WhenApplyPaddingWithMultipleOfr(final TestInfo testInfo) {
    // given
    final long[] message = new long[r * 9];
    message[0] = 1L; // just to check if all bytes are not set to null

    // when
    final long[] retMessage = spongeHashKeccak1600.applyPadding(message);

    // then
    verifyArraysAreEqual(retMessage, retMessage);

    assertEquals(r * 9, message.length);
  }

  @Test
  void shouldReturnOriginalMessage_WhenApplyPaddingWithoutMultipleOfr(final TestInfo testInfo) {
    // given
    final long[] message = new long[1024];
    final int paddedMessageLength = nearestGreaterMultiple(message.length, r / BITS_IN_LONG);
    message[0] = 1;

    // when
    final long[] retMessage = spongeHashKeccak1600.applyPadding(message);

    // then
    assertEquals(1024, message.length);
    assertEquals(1037, retMessage.length);

    for (int i = 0; i < message.length; i++) {
      assertEquals(message[i], retMessage[i]);
    }
    for (int i = message.length; i < retMessage.length; i++) {
      assertEquals(0, retMessage[i]);
    }

    assertEquals(paddedMessageLength, retMessage.length);
  }

  @Test
  void shouldReturnPaddedMessage_WhenApplyPaddingWithSmallMessage(final TestInfo testInfo) {
    // given
    final long[] message = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
    final long[] retMessage = spongeHashKeccak1600.applyPadding(message);

    // when and then
    assertDoesNotThrow(hashAndAssertSize(spongeHashKeccak1600.hash(retMessage)));

    for (int i = 0; i < message.length; i++) {
      assertEquals(retMessage[i], message[i]);
    }

    for (int i = message.length; i < retMessage.length; i++) {
      assertEquals(retMessage[i], 0);
    }
  }

  @Tag("arrayVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithSmallMessage(final TestInfo testInfo) {
    // given
    final int absorbIterationsCount = 1; // how many times will absorb phase iterate through message
    // 16 elements, just 1 before the max. 16 = r/bits_in_long - 1
    final long[] message = {
      0x7FFFFFFFFFFFFFFFL,
      0x11F44BFB1BBCD580L,
      0x244C24BC89905E29L,
      0x149A283E8FD56F98L,
      0x417A2406B1A3F772L,
      0x4E16AB587AF4A38L,
      0x14742890F662CDDAL,
      0x7654321F44D73A92L,
      0x7FFFFFFFFFFFFFFFL,
      0x2073B44182435C41L,
      0x7FFFFFFFFFFFFFFFL,
      0x7FFFFFFFFFFFFFFFL,
      0xA3E6A99E37D2A1C4L,
      0x7FFFFFFFFFFFFFFFL,
      0x7FFFFFFFFFFFFFFFL,
      0x7A74B517635724BDL
    };

    // when & then
    assertDoesNotThrow(hashAndAssertSize(spongeHashKeccak1600.hash(message)));
    verify(spongeHashKeccak1600, times(absorbIterationsCount)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount);
  }

  @Tag("arrayVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithMessageLengthOfMultipleOf1088(
      final TestInfo testInfo) {
    // given
    final int absorbIterationsCount = 1;
    // 17 elements, because 17 Longs fit to 1088 bits
    final long[] message = {
      0x7FFFFFFFFFFFFFFFL,
      0x11F44BFB1BBCD580L,
      0x244C24BC89905E29L,
      0x149A283E8FD56F98L,
      0x417A2406B1A3F772L,
      0x4E16AB587AF4A38L,
      0x14742890F662CDDAL,
      0x7654321F44D73A92L,
      0x7FFFFFFFFFFFFFFFL,
      0x2073B44182435C41L,
      0x7FFFFFFFFFFFFFFFL,
      0x7FFFFFFFFFFFFFFFL,
      0xA3E6A99E37D2A1C4L,
      0x7FFFFFFFFFFFFFFFL,
      0x7FFFFFFFFFFFFFFFL,
      0x7A74B517635724BDL,
      0x30E2B50BB8D9C599L
    };

    // when
    final long[] hashedMessage = spongeHashKeccak1600.hash(message);

    // then
    assertDoesNotThrow(hashAndAssertSize(hashedMessage));
    verify(spongeHashKeccak1600, times(absorbIterationsCount)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(1);
  }

  @Tag("arrayVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithMessageLengthOfNotMultipleOf1088(
      final TestInfo testInfo) {
    // given
    final int absorbIterationsCount = 2;
    // 18
    final long[] message = {
      0x7FFFFFFFFFFFFFFFL,
      0x11F44BFB1BBCD580L,
      0x244C24BC89905E29L,
      0x149A283E8FD56F98L,
      0x417A2406B1A3F772L,
      0x4E16AB587AF4A38L,
      0x14742890F662CDDAL,
      0x7654321F44D73A92L,
      0x7FFFFFFFFFFFFFFFL,
      0x2073B44182435C41L,
      0x7FFFFFFFFFFFFFFFL,
      0x7FFFFFFFFFFFFFFFL,
      0xA3E6A99E37D2A1C4L,
      0x7FFFFFFFFFFFFFFFL,
      0x7FFFFFFFFFFFFFFFL,
      0x7A74B517635724BDL,
      0x30E2B50BB8D9C599L,
      0x30E2B50BB8D9C532L
    };

    // when
    final long[] hashedMessage = spongeHashKeccak1600.hash(message);

    // then
    assertDoesNotThrow(hashAndAssertSize(hashedMessage));
    verify(spongeHashKeccak1600, times(absorbIterationsCount)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount);
  }

  @Tag("arrayVersion")
  @Test
  void shouldNotThrowException_WhenCallingMessageHashWith40Longs(final TestInfo testInfo) {
    // given
    final int absorbIterationsCount = 3; // how many times will absorb phase iterate through message
    // 40
    final long[] message = {
      0x7FFFFFFFFFFFFFFFL,
      0x11F44BFB1BBCD580L,
      0x244C24BC89905E29L,
      0x149A283E8FD56F98L,
      0x417A2406B1A3F772L,
      0x4E16AB587AF4A38L,
      0x14742890F662CDDAL,
      0x7654321F44D73A92L,
      0x7FFFFFFFFFFFFFFFL,
      0x2073B44182435C41L,
      0x7FFFFFFFFFFFFFFFL,
      0x7FFFFFFFFFFFFFFFL,
      0xA3E6A99E37D2A1C4L,
      0x7FFFFFFFFFFFFFFFL,
      0x7FFFFFFFFFFFFFFFL,
      0x7A74B517635724BDL,
      0x30E2B50BB8D9C599L,
      0x7FFFFFFFFFFFFFFFL,
      0x11F44BFB1BBCD580L,
      0x244C24BC89905E29L,
      0x149A283E8FD56F98L,
      0x417A2406B1A3F772L,
      0x4E16AB587AF4A38L,
      0x14742890F662CDDAL,
      0x7654321F44D73A92L,
      0x7FFFFFFFFFFFFFFFL,
      0x2073B44182435C41L,
      0x7FFFFFFFFFFFFFFFL,
      0x7FFFFFFFFFFFFFFFL,
      0xA3E6A99E37D2A1C4L,
      0x7FFFFFFFFFFFFFFFL,
      0x7FFFFFFFFFFFFFFFL,
      0x7A74B517635724BDL,
      0x30E2B50BB8D9C599L,
      0x7FFFFFFFFFFFFFFFL,
      0x7FFFFFFFFFFFFFFFL,
      0x7FFFFFFFFFFFFFFFL,
      0x7A74B517635724BDL,
      0x30E2B50BB8D9C599L,
      0x30E2B50BB8D9C532L
    };

    // when
    final long[] hashedMessage = spongeHashKeccak1600.hash(message);

    // then
    assertDoesNotThrow(hashAndAssertSize(hashedMessage));
    verify(spongeHashKeccak1600, times(absorbIterationsCount)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount);
  }

  @Tag("arrayVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithVeryLargeMessage(final TestInfo testInfo) {
    // given
    final int arraySize = 102_393;
    final int absorbIterationsCount =
        (int)
            Math.ceil(
                (double) arraySize
                    / ((double) r
                        / BITS_IN_LONG)); // how many times will absorb phase iterate through
    final long[] message = new long[arraySize];

    // when
    final long[] hashedMessage = spongeHashKeccak1600.hash(message);

    // then
    assertDoesNotThrow(hashAndAssertSize(hashedMessage));
    verify(spongeHashKeccak1600, times(absorbIterationsCount)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount);
  }

  @Tag("streamVersion")
  @Test
  void shouldNotThrowException_WhenHashingWithSmallNumberNotAMultipleOf8Bytes(
      final TestInfo testInfo) {
    // given
    final byte[] message = {33, -127, 10, 33, -127, 10, 33}; // 7
    final InputStream is = new ByteArrayInputStream(message);
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length);

    // when
    final long[] resStream = spongeHashKeccak1600.hash(is, message.length);
    final long[] resArray = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

    // then
    verifyArraysAreEqual(resStream, resArray);
    assertDoesNotThrow(hashAndAssertSize(resStream));

    verify(spongeHashKeccak1600, times(absorbIterationsCount * 2)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount * 2);
  }

  @Tag("streamVersion")
  @Test
  void shouldNotThrowException_WhenHashingSmallNumberWithAMultipleOf8Bytes(
      final TestInfo testInfo) {
    // given
    final byte[] message = {33, -127, 10, 33, -127, 10, 33, 11}; // 8
    final InputStream is = new ByteArrayInputStream(message);
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length);

    // when
    final long[] resStream = spongeHashKeccak1600.hash(is, message.length);
    final long[] resArray = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

    // then
    verifyArraysAreEqual(resStream, resArray);
    assertDoesNotThrow(hashAndAssertSize(resStream));
    verify(spongeHashKeccak1600, times(2 * absorbIterationsCount)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(2 * absorbIterationsCount);
  }

  @Tag("streamVersion")
  @Test
  void shouldNotThrowException_WhenHashingSmallNumberWithAMultipleOf8Bytes2(
      final TestInfo testInfo) {
    // given
    final byte[] message = {33, -127, 10, 33, -127, 10, 33, 13}; // 8
    final InputStream is = new ByteArrayInputStream(message);
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length);

    // when
    final long[] resStream = spongeHashKeccak1600.hash(is, message.length);
    final long[] resArray = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

    // then
    verifyArraysAreEqual(resStream, resArray);
    assertDoesNotThrow(hashAndAssertSize(resStream));
    verify(spongeHashKeccak1600, times(2 * absorbIterationsCount)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(2 * absorbIterationsCount);
  }

  @Tag("streamVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithStreamMessageLengthOfMultipleOf1088(
      final TestInfo testInfo) {
    // given
    // 136 bytes = 17 longs = 1 message block size
    final byte[] message = {
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127
    };
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length);
    final InputStream is = new ByteArrayInputStream(message);

    // when
    final long[] resStream = spongeHashKeccak1600.hash(is, message.length);
    final long[] resArray = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

    // then
    verifyArraysAreEqual(resStream, resArray);

    assertDoesNotThrow(hashAndAssertSize(resStream));
    verify(spongeHashKeccak1600, times(absorbIterationsCount * 2)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount * 2);
  }

  @Tag("streamVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithStreamMessageLengthOfNotMultipleOf1088(
      final TestInfo testInfo) {
    // given
    // 137
    final byte[] message = {
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10
    };
    final InputStream is = new ByteArrayInputStream(message);
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length);

    // when
    final long[] resStream = spongeHashKeccak1600.hash(is, message.length);
    final long[] resArray = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

    // then
    verifyArraysAreEqual(resStream, resArray);
    assertDoesNotThrow(hashAndAssertSize(resStream));
    verify(spongeHashKeccak1600, times(2 * absorbIterationsCount)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(2 * absorbIterationsCount);
  }

  @Tag("streamVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithStreamMessageLengthOfNotMultipleOf10882(
      final TestInfo testInfo) {
    // given
    // 137
    final byte[] message = {
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
      33, -127, 10, 33, -127, 10, 33, -127, 11
    };
    final InputStream is = new ByteArrayInputStream(message);
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length);

    // when
    final long[] resStream = spongeHashKeccak1600.hash(is, message.length);
    final long[] resArray = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

    // then
    verifyArraysAreEqual(resStream, resArray);
    assertDoesNotThrow(hashAndAssertSize(resStream));
    verify(spongeHashKeccak1600, times(2 * absorbIterationsCount)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(2 * absorbIterationsCount);
  }

  @Tag("streamVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithStreamLongMessage(final TestInfo testInfo) {
    // given
    final int arraySize = 104_812;
    final byte[] message = new byte[arraySize];
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length);
    final InputStream is = new ByteArrayInputStream(message);

    // when
    final long[] hashedMessage = spongeHashKeccak1600.hash(is, message.length);

    // then
    assertDoesNotThrow(hashAndAssertSize(hashedMessage));
    verify(spongeHashKeccak1600, times(absorbIterationsCount)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount);
  }

  @Tag("streamVersion")
  @Tag("performanceHeavy")
  @Test
  void shouldNotThrowException_WhenCallingHashWithStreamVideoMessage(final TestInfo testInfo)
      throws IOException {
    // given
    final String filePath = "src/test/resources/video.mp4";
    try (final InputStream is = new FileInputStream(filePath);
        final InputStream is1 = new FileInputStream(filePath)) {
      final Path path = Paths.get(filePath);
      final int fileSize = (int) Files.size(path);
      final int absorbIterationsCount = calculateNumberOfAbsorbIterations(fileSize);
      final byte[] byteArrayStream = toByteArray(is1, is1.available());
      final long[] longArrayStream = byteArrayToLongArray(byteArrayStream);

      // when
      final long[] resStream = spongeHashKeccak1600.hash(is, fileSize);
      final long[] resArray = spongeHashKeccak1600.hash(longArrayStream);

      // then
      verifyArraysAreEqual(resStream, resArray);
      verify(spongeHashKeccak1600, times(absorbIterationsCount * 2)).absorb(any(), any());
      verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount * 2);
    }
  }
  private void verifyPermFuncsGetCalledNTimesRoundTimes(final int n) {
    verify(spongePermutationImpl, times(n * ROUNDS)).theta(any());
    verify(spongePermutationImpl, times(n * ROUNDS)).rhoPi(any());
    verify(spongePermutationImpl, times(n * ROUNDS)).chi(any());
    verify(spongePermutationImpl, times(n * ROUNDS)).iota(any(), anyInt());
  }
}
