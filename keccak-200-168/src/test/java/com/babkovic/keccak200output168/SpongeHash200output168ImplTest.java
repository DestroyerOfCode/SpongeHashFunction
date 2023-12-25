package com.babkovic.keccak200output168;

import static com.babkovic.TestUtils.calculateNumberOfAbsorbIterations;
import static com.babkovic.TestUtils.verifyArraysAreEqual;
import static com.babkovic.keccak200output168.Constants.BITS_IN_BYTE;
import static com.babkovic.keccak200output168.Constants.OUTPUT_LENGTH_BITS;
import static com.babkovic.keccak200output168.Constants.ROUNDS;
import static com.babkovic.keccak200output168.Constants.r;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.babkovic.TestUtils;
import com.babkovic.api.SpongeHash;
import com.babkovic.api.SpongePermutation;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

public class SpongeHash200output168ImplTest {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(SpongeHash200output168ImplTest.class);
  private SpongePermutation<byte[]> spongePermutationImpl;
  private SpongeHash<byte[]> spongeHashKeccak200;

  @BeforeEach
  void setUp(final TestInfo testInfo) {
    LOGGER.info(() -> String.format("Starting test: %s", testInfo.getDisplayName()));
    spongePermutationImpl = spy(new PermutationImpl());
    spongeHashKeccak200 = spy(new SpongeHashKeccak200Impl(spongePermutationImpl));
  }

  @AfterEach
  void cleanUp(final TestInfo testInfo) {
    LOGGER.info(() -> String.format("Ending test: %s", testInfo.getDisplayName()));
    spongePermutationImpl = null;
    spongeHashKeccak200 = null;
  }

  @Test
  @DisplayName("Apply padding without multiple of 'r': should correctly pad the message")
  void shouldReturnOriginalMessage_WhenApplyPaddingWithoutMultipleOfr() {
    // given
    byte[] message = new byte[r / BITS_IN_BYTE + 1];
    message[0] = 1;

    // when
    final byte[] retMessage = spongeHashKeccak200.applyPadding(message);

    // then
    assertAll(
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

  @Test
  @DisplayName("Apply padding with a small message: should return a correctly padded message")
  void shouldReturnPaddedMessage_WhenApplyPaddingWithSmallMessage() {
    // given
    final byte[] message = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};

    // when
    final byte[] retMessage = spongeHashKeccak200.applyPadding(message);

    // then
    assertAll(
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
  @DisplayName("Hashing a small message array: should complete without exceptions")
  void shouldNotThrowException_WhenCallingHashWithSmallMessage() {
    // given
    final byte[] message = {
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127
    };
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length, r);

    // when
    final byte[] hashedMessage = spongeHashKeccak200.hash(message);

    // then
    assertAll(
        () ->
            assertDoesNotThrow(
                TestUtils.hashAndAssertOutputSize(hashedMessage, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak200, times(absorbIterationsCount)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount));
  }

  @Test
  @DisplayName("Apply padding with multiple of 'r': should return the original message unchanged")
  void shouldReturnOriginalMessage_WhenApplyPaddingWithMultipleOfr() {
    // given
    final byte[] message = new byte[r / BITS_IN_BYTE * 9];
    message[0] = 1; // just to check if all bytes are not set to null
    // when
    final byte[] retMessage = spongeHashKeccak200.applyPadding(message);

    // then
    assertAll(
        () -> {
          for (int i = 0; i < retMessage.length; i++) {
            assertEquals(retMessage[i], message[i]);
          }
        });
  }

  @Tag("arrayVersion")
  @Test
  @DisplayName(
      "Hashing an array message of length multiple of 168: should complete without exceptions")
  void shouldNotThrowException_WhenCallingHashWithMessageLengthOfMultipleOf168() {
    // given
    final byte[] message = {
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      127
    };
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length, r);

    // when
    final byte[] hashedMessage = spongeHashKeccak200.hash(message);

    // then
    assertAll(
        () ->
            assertDoesNotThrow(
                TestUtils.hashAndAssertOutputSize(hashedMessage, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak200, times(absorbIterationsCount)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount));
  }

  @Tag("arrayVersion")
  @Test
  @DisplayName(
      "Hashing an array message of length not multiple of 168: should complete without exceptions")
  void shouldNotThrowException_WhenCallingHashWithMessageLengthOfNotMultipleOf168() {
    // given
    final byte[] message = {
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      127, 3
    };
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length, r);

    // when
    final byte[] hashedMessage = spongeHashKeccak200.hash(message);

    // then
    assertAll(
        () ->
            assertDoesNotThrow(
                TestUtils.hashAndAssertOutputSize(hashedMessage, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak200, times(absorbIterationsCount)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount));
  }

  @Tag("arrayVersion")
  @Test
  @DisplayName("Hashing a very large message array: should complete without exceptions")
  void shouldNotThrowException_WhenCallingHashWithVeryLargeMessage() {
    // given
    final byte[] message = new byte[102_393];
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length, r);

    // when
    final byte[] hashedMessage = spongeHashKeccak200.hash(message);

    // then
    assertAll(
        () ->
            assertDoesNotThrow(
                TestUtils.hashAndAssertOutputSize(hashedMessage, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak200, times(absorbIterationsCount)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount));
  }

  @Tag("streamVersion")
  @Tag("arrayVersion")
  @Test
  @DisplayName("Hashing a small message stream: should yield consistent results with array version")
  void shouldNotThrowException_WhenCallingHashWithStreamSmallMessage() {
    // given
    final byte[] message = {33, -127, 10, 33, -127, 10, 33, -127};
    final InputStream is = new ByteArrayInputStream(message);
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length, r);

    // when
    final byte[] hashedStreamMessage = spongeHashKeccak200.hash(is, message.length);
    final byte[] hashedArrayMessage = spongeHashKeccak200.hash(message);

    // then
    assertAll(
        () -> assertEquals(hashedArrayMessage.length, hashedStreamMessage.length),
        () -> {
          for (int i = 0; i < hashedStreamMessage.length; i++) {
            assertEquals(hashedArrayMessage[i], hashedStreamMessage[i]);
          }
        },
        () ->
            assertDoesNotThrow(
                TestUtils.hashAndAssertOutputSize(hashedStreamMessage, OUTPUT_LENGTH_BITS)),
        () ->
            assertDoesNotThrow(
                TestUtils.hashAndAssertOutputSize(hashedArrayMessage, OUTPUT_LENGTH_BITS)),
        () ->
            assertDoesNotThrow(
                TestUtils.hashAndAssertOutputSize(hashedStreamMessage, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak200, times(absorbIterationsCount * 2)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount * 2));
  }

  @Tag("streamVersion")
  @Test
  @DisplayName(
      "Hashing a stream message of length multiple of 168: should yield consistent results")
  void shouldNotThrowException_WhenCallingHashWithStreamMessageLengthOfMultipleOf168() {
    // given
    final byte[] message = {
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      127
    };
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length, r);
    final InputStream is = new ByteArrayInputStream(message);

    // when
    final byte[] hashedStreamMessage = spongeHashKeccak200.hash(is, message.length);
    final byte[] hashedArrayMessage = spongeHashKeccak200.hash(message);

    // then
    assertAll(
        () -> assertEquals(hashedArrayMessage.length, hashedStreamMessage.length),
        () -> {
          for (int i = 0; i < hashedStreamMessage.length; i++) {
            assertEquals(hashedArrayMessage[i], hashedStreamMessage[i]);
          }
        },
        () ->
            assertDoesNotThrow(
                TestUtils.hashAndAssertOutputSize(hashedStreamMessage, OUTPUT_LENGTH_BITS)),
        () ->
            assertDoesNotThrow(
                TestUtils.hashAndAssertOutputSize(hashedArrayMessage, OUTPUT_LENGTH_BITS)),
        () ->
            assertDoesNotThrow(
                TestUtils.hashAndAssertOutputSize(hashedStreamMessage, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak200, times(absorbIterationsCount * 2)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount * 2));
  }

  @Tag("streamVersion")
  @Test
  @DisplayName(
      "Hashing a stream message of length not multiple of 168: should yield consistent results")
  void shouldNotThrowException_WhenCallingHashWithStreamMessageLengthOfNotMultipleOf168() {
    // given
    final byte[] message = {
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      127, 3
    };
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length, r);
    final InputStream is = new ByteArrayInputStream(message);

    // when
    final byte[] hashedMessage = spongeHashKeccak200.hash(is, message.length);

    // then
    assertAll(
        () ->
            assertDoesNotThrow(
                TestUtils.hashAndAssertOutputSize(hashedMessage, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak200, times(absorbIterationsCount)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount));
  }

  @Tag("streamVersion")
  @Test
  @DisplayName("Hashing a long message stream: should complete without exceptions")
  void shouldNotThrowException_WhenCallingHashWithStreamLongMessage() {
    // given
    final int n = 4992; // how many times will absorb phase iterate through message
    byte[] message = new byte[1_048_12];
    final InputStream is = new ByteArrayInputStream(message);

    // when
    final byte[] hashedMessage = spongeHashKeccak200.hash(is, message.length);

    // then
    assertAll(
        () ->
            assertDoesNotThrow(
                TestUtils.hashAndAssertOutputSize(hashedMessage, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak200, times(n)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(n));
  }

  @Tag("streamVersion")
  @Tag("performanceHeavy")
  @Test
  @DisplayName("Hashing a video file stream: should complete without exceptions")
  void shouldNotThrowException_WhenCallingHashWithStreamImageMessage() throws IOException {
    // given
    try (final InputStream is = new FileInputStream("src/test/resources/video.mp4")) {
      final int fileSize = is.available();
      final int absorbIterationsCount = calculateNumberOfAbsorbIterations(fileSize, r);

      // when
      final byte[] hashedMessage = spongeHashKeccak200.hash(is, fileSize);

      // then
      assertAll(
          () ->
              assertDoesNotThrow(
                  TestUtils.hashAndAssertOutputSize(hashedMessage, OUTPUT_LENGTH_BITS)),
          () -> verify(spongeHashKeccak200, times(absorbIterationsCount)).absorb(any(), any()),
          () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount));
    }
  }

  @Test
  @Tag("stringVersion")
  @DisplayName("Hashing string with Stream and Array: Should not throw any exceptions")
  void hashingSmallStringWithStreamShouldNotThrowAnyExceptions() {
    // given
    final String stringToHash = "Hello";
    final int absorbIterationsCount =
        calculateNumberOfAbsorbIterations(stringToHash.getBytes(StandardCharsets.UTF_8).length, r);

    // when
    final byte[] resStream =
        spongeHashKeccak200.hash(
            new ByteArrayInputStream(stringToHash.getBytes(StandardCharsets.UTF_8)),
            stringToHash.getBytes().length);
    final byte[] resArray = spongeHashKeccak200.hash(stringToHash.getBytes(StandardCharsets.UTF_8));

    // then
    assertAll(
        () -> verifyArraysAreEqual(resStream, resArray),
        () -> assertDoesNotThrow(TestUtils.hashAndAssertOutputSize(resStream, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak200, times(absorbIterationsCount * 2)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount * 2));
  }

  @Test
  @Tag("stringVersion")
  @DisplayName("Hashing string with Stream and Array: Should not throw any exceptions")
  void hashingLargeStringWithStreamShouldNotThrowAnyExceptions() {
    // given
    final String stringToHash =
        "HelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHello"
            + "HelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHello"
            + "HelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHello";
    final byte[] stringByteArray = stringToHash.getBytes(StandardCharsets.UTF_8);
    final int absorbIterationsCount = calculateNumberOfAbsorbIterations(stringByteArray.length, r);

    // when
    final byte[] resStream =
        spongeHashKeccak200.hash(new ByteArrayInputStream(stringByteArray), stringByteArray.length);
    final byte[] resArray = spongeHashKeccak200.hash(stringByteArray);

    // then
    assertAll(
        () -> verifyArraysAreEqual(resStream, resArray),
        () -> assertDoesNotThrow(TestUtils.hashAndAssertOutputSize(resStream, OUTPUT_LENGTH_BITS)),
        () -> verify(spongeHashKeccak200, times(absorbIterationsCount * 2)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount * 2));
  }

  private void verifyPermFuncsGetCalledNTimesRoundTimes(final int n) {
    verify(spongePermutationImpl, times(n * ROUNDS)).theta(any());
    verify(spongePermutationImpl, times(n * ROUNDS)).rhoPi(any());
    verify(spongePermutationImpl, times(n * ROUNDS)).chi(any());
    verify(spongePermutationImpl, times(n * ROUNDS)).iota(any(), anyInt());
  }
}
