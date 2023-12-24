package com.babkovic.keccak1600output256;

import static com.babkovic.keccak1600output256.Constants.BITS_IN_BYTE;
import static com.babkovic.keccak1600output256.Constants.BITS_IN_LONG;
import static com.babkovic.keccak1600output256.Constants.BYTES_IN_LONG;
import static com.babkovic.keccak1600output256.Constants.OUTPUT_LENGTH_BITS;
import static com.babkovic.keccak1600output256.Constants.ROUNDS;
import static com.babkovic.keccak1600output256.Constants.STATE_LONG_LENGTH;
import static com.babkovic.keccak1600output256.Constants.r;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

  @Test
  void shouldReturnOriginalMessage_WhenApplyPaddingWithMultipleOfr(final TestInfo testInfo) {
    // given
    final long[] message = new long[r * 9];
    message[0] = 1L; // just to check if all bytes are not set to null

    // when
    final long[] retMessage = spongeHashKeccak1600.applyPadding(message);

    // then
    for (int i = 0; i < retMessage.length; i++) {
      assertEquals(retMessage[i], message[i]);
    }

    assertEquals(r * 9, message.length);
  }

  @Test
  void shouldReturnOriginalMessage_WhenApplyPaddingWithoutMultipleOfr(final TestInfo testInfo) {
    // given
    final long[] message = new long[1024];
    final int paddedMessageLength = message.length + 13;
    message[0] = 1;
    final long[] retMessage = spongeHashKeccak1600.applyPadding(message);

    // when and then
    assertDoesNotThrow(hashAndAssertSize(spongeHashKeccak1600.hash(retMessage)));

    for (int i = 0; i < message.length; i++) {
      assertEquals(retMessage[i], message[i]);
    }

    assertEquals(1024, message.length);

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

  @Test
  void shouldThrowException_WhenStateLengthIsNot25(final TestInfo testInfo) {
    // given & when
    final RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () -> spongeHashKeccak1600.initState(new long[1]),
            String.format(
                "The test %s failed on asserting an exception", testInfo.getDisplayName()));

    // then
    assertEquals(
        "Incorrect size of state. Should be 200 Bytes (25 Longs) (1600 bits).", ex.getMessage());
  }

  @Test
  void shouldInitStateWithValue_WhenStateLengthIs200Bytes(final TestInfo testInfo) {
    // given
    final long[] state = new long[STATE_LONG_LENGTH];

    // when
    spongeHashKeccak1600.initState(state);

    // then
    for (long b : state) {
      assertEquals(b, 1431655765L);
    }
  }

  @Tag("arrayVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithSmallMessage(final TestInfo testInfo) {
    // given
    final int n = 1; // how many times will absorb phase iterate through message
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
    verify(spongeHashKeccak1600, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(n);
  }

  @Tag("arrayVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithMessageLengthOfMultipleOf1088(
      final TestInfo testInfo) {
    // given
    final int n = 1; // how many times will absorb phase iterate through message
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

    // when & then
    assertDoesNotThrow(hashAndAssertSize(spongeHashKeccak1600.hash(message)));
    verify(spongeHashKeccak1600, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(1);
  }

  @Tag("arrayVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithMessageLengthOfNotMultipleOf1088(
      final TestInfo testInfo) {
    // given
    final int n = 2; // how many times will absorb phase iterate through message
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

    // when & then
    assertDoesNotThrow(hashAndAssertSize(spongeHashKeccak1600.hash(message)));
    verify(spongeHashKeccak1600, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(n);
  }

  @Tag("arrayVersion")
  @Test
  void shouldNotThrowException_WhenCallingMessageHashWith40Longs(final TestInfo testInfo) {
    // given
    final int n = 3; // how many times will absorb phase iterate through message
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

    // when & then
    assertDoesNotThrow(hashAndAssertSize(spongeHashKeccak1600.hash(message)));
    verify(spongeHashKeccak1600, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(n);
  }

  @Tag("arrayVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithVeryLargeMessage(final TestInfo testInfo) {
    // given
    final int arraySize = 102_393;
    final int n =
        (int)
            Math.ceil(
                (double) arraySize
                    / ((double) r
                        / BITS_IN_LONG)); // how many times will absorb phase iterate through
    // message

    final long[] message = new long[arraySize];

    // when & then
    assertDoesNotThrow(hashAndAssertSize(spongeHashKeccak1600.hash(message)));
    verify(spongeHashKeccak1600, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(n);
  }

  @Tag("streamVersion")
  @Test
  void shouldNotThrowException_WhenHashingWithSmallNumberNotAMultipleOf8Bytes(
      final TestInfo testInfo) {
    // given
    final byte[] message = {33, -127, 10, 33, -127, 10, 33}; // 7
    final InputStream is = new ByteArrayInputStream(message);
    final int n =
        (int)
            Math.ceil(
                (double) (message.length * BITS_IN_BYTE)
                    / r); // how many times will absorb phase iterate through message

    // when & then
    final long[] resStream = spongeHashKeccak1600.hash(is, message.length);
    final long[] resArray = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

    for (int i = 0; i < resStream.length; i++) {
      assertEquals(resArray[i], resStream[i]);
    }
    assertDoesNotThrow(hashAndAssertSize(resStream));

    verify(spongeHashKeccak1600, times(n * 2)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(n * 2);
  }

  @Tag("streamVersion")
  @Test
  void shouldNotThrowException_WhenHashingSmallNumberWithAMultipleOf8Bytes(
      final TestInfo testInfo) {
    // given
    final byte[] message = {33, -127, 10, 33, -127, 10, 33, 11}; // 8
    final InputStream is = new ByteArrayInputStream(message);
    final int n =
        (int)
            Math.ceil(
                (double) (message.length * BITS_IN_BYTE)
                    / r); // how many times will absorb phase iterate through message

    // when
    final long[] resStream = spongeHashKeccak1600.hash(is, message.length);
    final long[] resArray = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

    // then
    for (int i = 0; i < resStream.length; i++) {
      assertEquals(resArray[i], resStream[i]);
    }
    assertDoesNotThrow(hashAndAssertSize(resStream));
    verify(spongeHashKeccak1600, times(2 * n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(2 * n);
  }

  @Tag("streamVersion")
  @Test
  void shouldNotThrowException_WhenHashingSmallNumberWithAMultipleOf8Bytes2(
      final TestInfo testInfo) {
    // given
    final byte[] message = {33, -127, 10, 33, -127, 10, 33, 13}; // 8
    final InputStream is = new ByteArrayInputStream(message);
    final int n =
        (int)
            Math.ceil(
                (double) (message.length * BITS_IN_BYTE)
                    / r); // how many times will absorb phase iterate through message

    // when
    final long[] resStream = spongeHashKeccak1600.hash(is, message.length);
    final long[] resArray = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

    // then
    for (int i = 0; i < resStream.length; i++) {
      assertEquals(resArray[i], resStream[i]);
    }
    assertDoesNotThrow(hashAndAssertSize(resStream));
    verify(spongeHashKeccak1600, times(2 * n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(2 * n);
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
    final int n =
        (int)
            Math.ceil(
                (double) (message.length * BITS_IN_BYTE)
                    / r); // how many times will absorb phase iterate through message
    final InputStream is = new ByteArrayInputStream(message);

    // when
    final long[] resStream = spongeHashKeccak1600.hash(is, message.length);
    final long[] resArray = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

    // then
    for (int i = 0; i < resStream.length; i++) {
      assertEquals(resArray[i], resStream[i]);
    }

    assertDoesNotThrow(hashAndAssertSize(resStream));
    verify(spongeHashKeccak1600, times(n * 2)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(n * 2);
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
    final int n =
        (int)
            Math.ceil(
                (double) (message.length * BITS_IN_BYTE)
                    / r); // how many times will absorb phase iterate through message

    // when
    final long[] resStream = spongeHashKeccak1600.hash(is, message.length);
    final long[] resArray = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

    // then
    for (int i = 0; i < resStream.length; i++) {
      assertEquals(resArray[i], resStream[i]);
    }
    assertDoesNotThrow(hashAndAssertSize(resStream));
    verify(spongeHashKeccak1600, times(2 * n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(2 * n);
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
    final int n =
        (int)
            Math.ceil(
                (double) (message.length * BITS_IN_BYTE)
                    / r); // how many times will absorb phase iterate through message

    // when
    final long[] resStream = spongeHashKeccak1600.hash(is, message.length);
    final long[] resArray = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

    // then
    for (int i = 0; i < resStream.length; i++) {
      assertEquals(resArray[i], resStream[i]);
    }
    assertDoesNotThrow(hashAndAssertSize(resStream));
    verify(spongeHashKeccak1600, times(2 * n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(2 * n);
  }

  @Tag("streamVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithStreamLongMessage(final TestInfo testInfo) {
    // given
    final int arraySize = 104_812;
    final byte[] message = new byte[arraySize];
    final int n =
        (int)
            Math.ceil(
                (double) (message.length * BITS_IN_BYTE)
                    / r); // how many times will absorb phase iterate through message
    final InputStream is = new ByteArrayInputStream(message);

    // when & then
    assertDoesNotThrow(hashAndAssertSize(spongeHashKeccak1600.hash(is, message.length)));
    verify(spongeHashKeccak1600, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(n);
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
      final int n =
          (int)
              Math.ceil(
                  (double) (fileSize * BITS_IN_BYTE)
                      / r); // how many times will absorb phase iterate through message

      // when
      final long[] resStream = spongeHashKeccak1600.hash(is, fileSize);
      final long[] resArray =
          spongeHashKeccak1600.hash(byteArrayToLongArray(toByteArray(is1, is1.available())));

      // then
      for (int i = 0; i < resStream.length; i++) {
        assertEquals(resArray[i], resStream[i]);
      }
      verify(spongeHashKeccak1600, times(n * 2)).absorb(any(), any());
      verifyPermFuncsGetCalledNTimesRoundTimes(n * 2);
    }
  }

  private static byte[] toByteArray(InputStream inputStream, final int available)
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

  private void verifyPermFuncsGetCalledNTimesRoundTimes(final int n) {
    verify(spongePermutationImpl, times(n * ROUNDS)).theta(any());
    verify(spongePermutationImpl, times(n * ROUNDS)).rhoPi(any());
    verify(spongePermutationImpl, times(n * ROUNDS)).chi(any());
    verify(spongePermutationImpl, times(n * ROUNDS)).iota(any(), anyInt());
  }

  private static long[] byteArrayToLongArray(byte[] bytes) {

    final long[] outBytes = new long[(int) Math.ceil((double) bytes.length / BYTES_IN_LONG)];

    int padding = outBytes.length * BYTES_IN_LONG - bytes.length;
    ByteBuffer buffer =
        ByteBuffer.allocate(bytes.length + padding).put(bytes).put(new byte[padding]);
    buffer.flip();
    buffer.asLongBuffer().get(outBytes);
    return outBytes;
  }

  private Executable hashAndAssertSize(final long[] spongeHashKeccak1600) {
    return () -> assertEquals(OUTPUT_LENGTH_BITS / BITS_IN_LONG, spongeHashKeccak1600.length);
  }
}
