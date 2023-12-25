package com.babkovic.keccak1600output256;

import static com.babkovic.TestUtils.byteArrayToLongArray;
import static com.babkovic.TestUtils.calculateNumberOfAbsorbIterations;
import static com.babkovic.TestUtils.hashAndAssertOutputSize;
import static com.babkovic.TestUtils.toByteArray;
import static com.babkovic.TestUtils.verifyArraysAreEqual;
import static com.babkovic.common.Constants.BITS_IN_LONG;
import static com.babkovic.common.Constants.BYTES_IN_LONG;
import static com.babkovic.common.Utils.nearestGreaterMultiple;
import static com.babkovic.keccak1600output256.Constants.OUTPUT_LENGTH_BITS;
import static com.babkovic.keccak1600output256.Constants.ROUNDS;
import static com.babkovic.keccak1600output256.Constants.r;
import static org.junit.jupiter.api.Assertions.assertAll;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

class SpongeHash1600Output256ImplTest {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(SpongeHash1600Output256ImplTest.class);

  private SpongePermutation<long[]> spongePermutationImpl;
  private SpongeHash<long[]> spongeHashKeccak1600;

  @BeforeEach
  void setUp(final TestInfo testInfo) {
    LOGGER.info(() -> String.format("Starting test: %s", testInfo.getDisplayName()));
    spongePermutationImpl = spy(new PermutationImpl());
    spongeHashKeccak1600 = spy(new SpongeHashKeccak1600Impl(spongePermutationImpl));
  }

  @AfterEach
  void cleanUp(final TestInfo testInfo) {
    LOGGER.info(() -> String.format("Ending test: %s", testInfo.getDisplayName()));
    spongePermutationImpl = null;
    spongeHashKeccak1600 = null;
  }

  @Test
  @DisplayName("Padding with multiple of 'r': should return the original message unchanged")
  void shouldReturnOriginalMessage_WhenApplyPaddingWithMultipleOfr() {
    // given
    final long[] message = new long[r * 9];
    message[0] = 1L; // just to check if all bytes are not set to null

    // when
    final long[] retMessage = spongeHashKeccak1600.applyPadding(message);

    // then
    assertAll(
        () -> verifyArraysAreEqual(retMessage, retMessage),
        () -> assertEquals(r * 9, message.length));
  }

  @Test
  @DisplayName("Padding without multiple of 'r': should return the original message length")
  void shouldReturnOriginalMessage_WhenApplyPaddingWithoutMultipleOfr() {
    // given
    final long[] message = new long[1024];
    final int paddedMessageLength = nearestGreaterMultiple(message.length, r / BITS_IN_LONG);
    message[0] = 1;

    // when
    final long[] retMessage = spongeHashKeccak1600.applyPadding(message);

    // then
    assertAll(
        () -> assertEquals(1024, message.length),
        () -> assertEquals(paddedMessageLength, retMessage.length),
        () -> {
          for (int i = 0; i < message.length; i++) {
            assertEquals(message[i], retMessage[i]);
          }
        },
        () -> {
          for (int i = message.length; i < retMessage.length; i++) {
            assertEquals(0, retMessage[i]);
          }
        });

    assertEquals(paddedMessageLength, retMessage.length);
  }

  @Test
  @DisplayName("Padding with a small message: should return a correctly padded message")
  void shouldReturnPaddedMessage_WhenApplyPaddingWithSmallMessage() {
    // given
    final long[] message = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
    final long[] retMessage = spongeHashKeccak1600.applyPadding(message);

    // when
    final long[] hashedMessage = spongeHashKeccak1600.hash(retMessage);

    // then
    assertAll(
        () -> assertDoesNotThrow(hashAndAssertOutputSize(hashedMessage, OUTPUT_LENGTH_BITS)),
        () -> {
          for (int i = 0; i < message.length; i++) {
            assertEquals(retMessage[i], message[i]);
          }
        },
        () -> {
          for (int i = message.length; i < retMessage.length; i++) {
            assertEquals(retMessage[i], 0);
          }
        });
  }

  @Tag("arrayVersion")
  @Test
  @DisplayName("Hashing a small message: should not throw any exceptions")
  void shouldNotThrowException_WhenCallingHashWithSmallMessage() {
    // given
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
    final int absorbIterationsCount =
        calculateNumberOfAbsorbIterations(message.length * BYTES_IN_LONG, r);

    // when
    final long[] hashedMessage = spongeHashKeccak1600.hash(message);

    // then
    assertAll(
        () -> assertDoesNotThrow(hashAndAssertOutputSize(hashedMessage, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak1600, times(absorbIterationsCount)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount));
  }

  @Tag("arrayVersion")
  @Test
  @DisplayName("Hashing with message length as multiple of 1088: should not throw exceptions")
  void shouldNotThrowException_WhenCallingHashWithMessageLengthOfMultipleOf1088() {
    // given
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
    final int absorbIterationsCount =
        calculateNumberOfAbsorbIterations(message.length * BYTES_IN_LONG, r);

    // when
    final long[] hashedMessage = spongeHashKeccak1600.hash(message);

    // then
    assertAll(
        () -> assertDoesNotThrow(hashAndAssertOutputSize(hashedMessage, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak1600, times(absorbIterationsCount)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount));
  }

  @Tag("arrayVersion")
  @Test
  @DisplayName("Hashing with message length not a multiple of 1088: should not throw exceptions")
  void shouldNotThrowException_WhenCallingHashWithMessageLengthOfNotMultipleOf1088() {
    // given
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
    final int absorbIterationsCount =
        calculateNumberOfAbsorbIterations(message.length * BYTES_IN_LONG, r);

    // when
    final long[] hashedMessage = spongeHashKeccak1600.hash(message);

    // then
    assertAll(
        () -> assertDoesNotThrow(hashAndAssertOutputSize(hashedMessage, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak1600, times(absorbIterationsCount)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount));
  }

  @Tag("arrayVersion")
  @Test
  @DisplayName("Hashing a message with 40 longs: should not throw any exceptions")
  void shouldNotThrowException_WhenCallingMessageHashWith40Longs() {
    // given
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
    final int absorbIterationsCount =
        calculateNumberOfAbsorbIterations(message.length * BYTES_IN_LONG, r);

    // when
    final long[] hashedMessage = spongeHashKeccak1600.hash(message);

    // then
    assertAll(
        () -> assertDoesNotThrow(hashAndAssertOutputSize(hashedMessage, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak1600, times(absorbIterationsCount)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount));
  }

  @Tag("arrayVersion")
  @Test
  @DisplayName("Hashing a very large message: should not throw any exceptions")
  void shouldNotThrowException_WhenCallingHashWithVeryLargeMessage() {
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
    assertAll(
        () -> assertDoesNotThrow(hashAndAssertOutputSize(hashedMessage, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak1600, times(absorbIterationsCount)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount));
  }

  @Tag("streamVersion")
  @Test
  @DisplayName("Hashing a small byte stream not a multiple of 8 bytes: should not throw exceptions")
  void shouldNotThrowException_WhenHashingWithSmallNumberNotAMultipleOf8Bytes() {
    // given
    final byte[] message = {33, -127, 10, 33, -127, 10, 33}; // 7
    final InputStream is = new ByteArrayInputStream(message);
    final int absorbIterationsCount =
        calculateNumberOfAbsorbIterations(message.length * BYTES_IN_LONG, r);

    // when
    final long[] resStream = spongeHashKeccak1600.hash(is, message.length);
    final long[] resArray = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

    // then
    assertAll(
        () -> verifyArraysAreEqual(resStream, resArray),
        () -> assertDoesNotThrow(hashAndAssertOutputSize(resStream, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak1600, times(absorbIterationsCount * 2)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount * 2));
  }

  @Tag("streamVersion")
  @Tag("arrayVersion")
  @Test
  @DisplayName(
      "Hashing a small byte stream with a multiple of 8 bytes: should not throw exceptions")
  void shouldNotThrowException_WhenHashingSmallNumberWithAMultipleOf8Bytes() {
    // given
    final byte[] message = {33, -127, 10, 33, -127, 10, 33, 11}; // 8
    final InputStream is = new ByteArrayInputStream(message);
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length, r);

    // when
    final long[] resStream = spongeHashKeccak1600.hash(is, message.length);
    final long[] resArray = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

    // then
    assertAll(
        () -> verifyArraysAreEqual(resStream, resArray),
        () -> assertDoesNotThrow(hashAndAssertOutputSize(resStream, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak1600, times(absorbIterationsCount * 2)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount * 2));
  }

  @Tag("streamVersion")
  @Test
  @DisplayName(
      "Hashing another small byte stream with a multiple of 8 bytes: should not throw exceptions")
  void shouldNotThrowException_WhenHashingSmallNumberWithAMultipleOf8Bytes2() {
    // given
    final byte[] message = {33, -127, 10, 33, -127, 10, 33, 13}; // 8
    final InputStream is = new ByteArrayInputStream(message);
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length, r);

    // when
    final long[] resStream = spongeHashKeccak1600.hash(is, message.length);
    final long[] resArray = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

    // then
    assertAll(
        () -> verifyArraysAreEqual(resStream, resArray),
        () -> assertDoesNotThrow(hashAndAssertOutputSize(resStream, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak1600, times(absorbIterationsCount * 2)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount * 2));
  }

  @Tag("streamVersion")
  @Tag("arrayVersion")
  @Test
  @DisplayName(
      "Hashing with stream message length of multiple of 1088: should not throw exceptions")
  void shouldNotThrowException_WhenCallingHashWithStreamMessageLengthOfMultipleOf1088() {
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
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length, r);
    final InputStream is = new ByteArrayInputStream(message);

    // when
    final long[] resStream = spongeHashKeccak1600.hash(is, message.length);
    final long[] resArray = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

    // then
    assertAll(
        () -> verifyArraysAreEqual(resStream, resArray),
        () -> assertDoesNotThrow(hashAndAssertOutputSize(resStream, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak1600, times(absorbIterationsCount * 2)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount * 2));
  }

  @Tag("streamVersion")
  @Tag("arrayVersion")
  @Test
  @DisplayName(
      "Hashing with stream message length not a multiple of 1088: should not throw exceptions")
  void shouldNotThrowException_WhenCallingHashWithStreamMessageLengthOfNotMultipleOf1088() {
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
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length, r);

    // when
    final long[] resStream = spongeHashKeccak1600.hash(is, message.length);
    final long[] resArray = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

    // then
    assertAll(
        () -> verifyArraysAreEqual(resStream, resArray),
        () -> assertDoesNotThrow(hashAndAssertOutputSize(resStream, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak1600, times(absorbIterationsCount * 2)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount * 2));
  }

  @Tag("streamVersion")
  @Tag("arrayVersion")
  @Test
  @DisplayName(
      "Hashing another stream message length not a multiple of 1088: should not throw exceptions")
  void shouldNotThrowException_WhenCallingHashWithStreamMessageLengthOfNotMultipleOf10882() {
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
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length, r);

    // when
    final long[] resStream = spongeHashKeccak1600.hash(is, message.length);
    final long[] resArray = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

    // then
    assertAll(
        () -> verifyArraysAreEqual(resStream, resArray),
        () -> assertDoesNotThrow(hashAndAssertOutputSize(resStream, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak1600, times(absorbIterationsCount * 2)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount * 2));
  }

  @Tag("streamVersion")
  @Test
  @DisplayName("Hashing a long stream message: should not throw any exceptions")
  void shouldNotThrowException_WhenCallingHashWithStreamLongMessage() {
    // given
    final int arraySize = 104_812;
    final byte[] message = new byte[arraySize];
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length, r);
    final InputStream is = new ByteArrayInputStream(message);

    // when
    final long[] hashedMessage = spongeHashKeccak1600.hash(is, message.length);

    // then
    assertAll(
        () -> assertDoesNotThrow(hashAndAssertOutputSize(hashedMessage, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak1600, times(absorbIterationsCount)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount));
  }

  @Tag("streamVersion")
  @Tag("performanceHeavy")
  @Test
  @DisplayName("Hashing a stream of video message: should not throw any exceptions")
  void shouldNotThrowException_WhenCallingHashWithStreamVideoMessage() throws IOException {
    // given
    final String filePath = "src/test/resources/video.mp4";
    try (final InputStream is = new FileInputStream(filePath);
        final InputStream is1 = new FileInputStream(filePath)) {
      final Path path = Paths.get(filePath);
      final int fileSize = (int) Files.size(path);
      final int absorbIterationsCount = calculateNumberOfAbsorbIterations(fileSize, r);
      final byte[] byteArrayStream = toByteArray(is1, is1.available());
      final long[] longArrayStream = byteArrayToLongArray(byteArrayStream);

      // when
      final long[] resStream = spongeHashKeccak1600.hash(is, fileSize);
      final long[] resArray = spongeHashKeccak1600.hash(longArrayStream);

      // then
      assertAll(
          () -> verifyArraysAreEqual(resStream, resArray),
          () -> verify(spongeHashKeccak1600, times(absorbIterationsCount * 2)).absorb(any(), any()),
          () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount * 2));
    }
  }

  @Test
  @DisplayName("Hashing a small string with Stream and Array: Should not throw any exceptions")
  void hashingSmallStringWithStreamShouldNotThrowAnyExceptions() {
    // given
    final String stringToHash = "Hello ";
    final int absorbIterationsCount =
        calculateNumberOfAbsorbIterations(stringToHash.getBytes(StandardCharsets.UTF_8).length, r);

    // when
    final long[] resStream =
        spongeHashKeccak1600.hash(
            new ByteArrayInputStream(stringToHash.getBytes(StandardCharsets.UTF_8)),
            stringToHash.getBytes().length);
    final long[] resArray =
        spongeHashKeccak1600.hash(
            byteArrayToLongArray(stringToHash.getBytes(StandardCharsets.UTF_8)));

    // then
    assertAll(
        () -> verifyArraysAreEqual(resStream, resArray),
        () -> assertDoesNotThrow(hashAndAssertOutputSize(resStream, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak1600, times(absorbIterationsCount * 2)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount * 2));
  }

  @Test
  @DisplayName("Hashing a large string with Stream and Array: Should not throw any exceptions")
  void hashingLargeStringWithStreamShouldNotThrowAnyExceptions() {
    // given
    final String stringToHash =
        "HelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHello"
            + "HelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHello"
            + "HelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHello";

    final byte[] stringByteArray = stringToHash.getBytes(StandardCharsets.UTF_8);
    final long[] stringLongArray = byteArrayToLongArray(stringByteArray);
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(stringByteArray.length, r);

    // when
    final long[] resStream =
        spongeHashKeccak1600.hash(
            new ByteArrayInputStream(stringByteArray), stringByteArray.length);
    final long[] resArray = spongeHashKeccak1600.hash(stringLongArray);

    // then
    assertAll(
        () -> verifyArraysAreEqual(resStream, resArray),
        () -> assertDoesNotThrow(hashAndAssertOutputSize(resStream, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak1600, times(absorbIterationsCount * 2)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount * 2));
  }

  private void verifyPermFuncsGetCalledNTimesRoundTimes(final int n) {
    verify(spongePermutationImpl, times(n * ROUNDS)).theta(any());
    verify(spongePermutationImpl, times(n * ROUNDS)).rhoPi(any());
    verify(spongePermutationImpl, times(n * ROUNDS)).chi(any());
    verify(spongePermutationImpl, times(n * ROUNDS)).iota(any(), anyInt());
  }
}
