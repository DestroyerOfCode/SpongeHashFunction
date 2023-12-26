package com.babkovic.keccak200output168;

import static com.babkovic.TestUtils.calculateNumberOfAbsorbIterations;
import static com.babkovic.TestUtils.hashAndAssertOutputSize;
import static com.babkovic.TestUtils.toByteArray;
import static com.babkovic.TestUtils.verifyArraysAreEqual;
import static com.babkovic.keccak200output168.Constants.BYTES_IN_r;
import static com.babkovic.keccak200output168.Constants.OUTPUT_LENGTH_BYTES;
import static com.babkovic.keccak200output168.Constants.ROUNDS;
import static com.babkovic.keccak200output168.Constants.r;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.babkovic.api.SpongeHash;
import com.babkovic.api.SpongePermutation;
import com.babkovic.exception.SpongeException;
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
import org.junit.jupiter.api.Nested;
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

  @Nested
  @DisplayName("Methods testing the applyPadding method")
  class TestPadding {
    @Test
    @DisplayName("Apply padding without multiple of 'r': should correctly pad the message")
    void shouldReturnOriginalMessage_WhenApplyPaddingWithoutMultipleOfr() {
      // given
      byte[] message = new byte[BYTES_IN_r + 1];
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
      // 15 bytes
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

    @Test
    @DisplayName("Apply padding with multiple of 'r': should return the original message unchanged")
    void shouldReturnOriginalMessage_WhenApplyPaddingWithMultipleOfr() {
      // given
      final byte[] message = new byte[BYTES_IN_r * 9];
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
  }

  @Nested
  class Hash {
    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Test
    @DisplayName("Hashing a small message array: should complete without exceptions")
    void shouldNotThrowException_WhenCallingHashWithSmallMessage() throws IOException {
      // given
      // 20 bytes
      final byte[] message = {
        33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127
      };
      final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length, r) * 2;
      try (final InputStream is = new ByteArrayInputStream(message)) {

        // when
        final byte[] hashedArrayMessage = spongeHashKeccak200.hash(message);
        final byte[] hashedStreamMessage = spongeHashKeccak200.hash(is, message.length);

        // then
        verifyHash(hashedStreamMessage, hashedArrayMessage, absorbIterationsCount);
      }
    }

    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Test
    @DisplayName(
        "Hashing an array message of length multiple of 168: should complete without exceptions")
    void shouldNotThrowException_WhenCallingHashWithMessageLengthOfMultipleOf168()
        throws IOException {
      // given
      // 21 bytes
      final byte[] message = {
        33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
        -127, 127
      };
      final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length, r) * 2;
      try (final InputStream is = new ByteArrayInputStream(message)) {

        // when
        final byte[] hashedStreamMessage = spongeHashKeccak200.hash(is, message.length);
        final byte[] hashedArrayMessage = spongeHashKeccak200.hash(message);

        // then
        verifyHash(hashedArrayMessage, hashedStreamMessage, absorbIterationsCount);
      }
    }

    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Test
    @DisplayName(
        "Hashing an array message of length not multiple of 168: should complete without exceptions")
    void shouldNotThrowException_WhenCallingHashWithMessageLengthOfNotMultipleOf168()
        throws IOException {
      // given
      // 22 bytes
      final byte[] message = {
        33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
        -127, 127, 3
      };
      final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length, r) * 2;
      try (final InputStream is = new ByteArrayInputStream(message)) {

        // when
        final byte[] hashedStreamMessage = spongeHashKeccak200.hash(is, message.length);
        final byte[] hashedArrayMessage = spongeHashKeccak200.hash(message);

        // then
        verifyHash(hashedArrayMessage, hashedStreamMessage, absorbIterationsCount);
      }
    }

    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Test
    @DisplayName("Hashing a very large message array: should complete without exceptions")
    void shouldNotThrowException_WhenCallingHashWithVeryLargeMessage() throws IOException {
      // given
      final byte[] message = new byte[10_393];
      final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length, r) * 2;
      try (final InputStream is = new ByteArrayInputStream(message)) {

        // when
        final byte[] hashedStreamMessage = spongeHashKeccak200.hash(is, message.length);
        final byte[] hashedArrayMessage = spongeHashKeccak200.hash(message);

        // then
        verifyHash(hashedArrayMessage, hashedStreamMessage, absorbIterationsCount);
      }
    }

    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Test
    @DisplayName(
        "Hashing a small message stream: should yield consistent results with array version")
    void shouldNotThrowException_WhenCallingHashWithStreamSmallMessage() throws IOException {
      // given
      // 8 bytes
      final byte[] message = {33, -127, 10, 33, -127, 10, 33, -127};
      try (final InputStream is = new ByteArrayInputStream(message)) {
        final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length, r) * 2;

        // when
        final byte[] hashedStreamMessage = spongeHashKeccak200.hash(is, message.length);
        final byte[] hashedArrayMessage = spongeHashKeccak200.hash(message);

        // then
        verifyHash(hashedArrayMessage, hashedStreamMessage, absorbIterationsCount);
      }
    }

    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Test
    @DisplayName(
        "Hashing a stream message of length multiple of 168: should yield consistent results")
    void shouldNotThrowException_WhenCallingHashWithStreamMessageLengthOfMultipleOf168()
        throws IOException {
      // given
      // 21 bytes
      final byte[] message = {
        33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
        -127, 127
      };
      final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length, r) * 2;
      try (final InputStream is = new ByteArrayInputStream(message)) {

        // when
        final byte[] hashedStreamMessage = spongeHashKeccak200.hash(is, message.length);
        final byte[] hashedArrayMessage = spongeHashKeccak200.hash(message);

        // then
        verifyHash(hashedArrayMessage, hashedStreamMessage, absorbIterationsCount);
      }
    }

    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Test
    @DisplayName(
        "Hashing a stream message of length not multiple of 168: should yield consistent results")
    void shouldNotThrowException_WhenCallingHashWithStreamMessageLengthOfNotMultipleOf168()
        throws IOException {
      // given
      // 22 bytes
      final byte[] message = {
        33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33,
        -127, 127, 3
      };
      final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length, r) * 2;
      try (final InputStream is = new ByteArrayInputStream(message)) {

        // when
        final byte[] hashedStreamMessage = spongeHashKeccak200.hash(is, message.length);
        final byte[] hashedArrayMessage = spongeHashKeccak200.hash(message);

        // then
        verifyHash(hashedStreamMessage, hashedArrayMessage, absorbIterationsCount);
      }
    }

    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Test
    @DisplayName("Hashing a long message stream: should complete without exceptions")
    void shouldNotThrowException_WhenCallingHashWithStreamLongMessage() throws IOException {
      // given
      final byte[] message = new byte[10_481];
      final int absorbIterationsCount = calculateNumberOfAbsorbIterations(message.length, r) * 2;
      try (final InputStream is = new ByteArrayInputStream(message)) {

        // when
        final byte[] hashedStreamMessage = spongeHashKeccak200.hash(is, message.length);
        final byte[] hashedArrayMessage = spongeHashKeccak200.hash(message);

        // then
        verifyHash(hashedStreamMessage, hashedArrayMessage, absorbIterationsCount);
      }
    }

    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Tag("fileVersion")
    @Tag("performanceHeavy")
    @Test
    @DisplayName("Hashing a video file stream: should complete without exceptions")
    void shouldNotThrowException_WhenCallingHashWithStreamImageMessage() throws IOException {
      // given
      final String filePath = "src/test/resources/video.mp4";
      try (final InputStream is = new FileInputStream(filePath);
          final InputStream is1 = new FileInputStream(filePath)) {
        final Path path = Paths.get(filePath);
        final int fileSize = (int) Files.size(path);
        final byte[] byteArrayStream = toByteArray(is1, is1.available());
        final int absorbIterationsCount = calculateNumberOfAbsorbIterations(fileSize, r) * 2;

        // when
        final byte[] hashedStreamMessage = spongeHashKeccak200.hash(is, fileSize);
        final byte[] hashedArrayMessage = spongeHashKeccak200.hash(byteArrayStream);

        // then
        verifyHash(hashedStreamMessage, hashedArrayMessage, absorbIterationsCount);
      }
    }

    @Test
    @Tag("stringVersion")
    @Tag("streamVersion")
    @Tag("arrayVersion")
    @DisplayName("Hashing string with Stream and Array: Should not throw any exceptions")
    void hashingSmallStringWithStreamShouldNotThrowAnyExceptions() throws IOException {
      // given
      final String stringToHash = "Hello";
      final byte[] stringByteArray = stringToHash.getBytes(StandardCharsets.UTF_8);
      final int absorbIterationsCount =
          calculateNumberOfAbsorbIterations(
              stringToHash.getBytes(StandardCharsets.UTF_8).length, r);
      try (final InputStream is = new ByteArrayInputStream(stringByteArray)) {

        // when
        final byte[] hashedStreamMessage = spongeHashKeccak200.hash(is, stringByteArray.length);
        final byte[] hashedArrayMessage = spongeHashKeccak200.hash(stringByteArray);

        // then
        verifyHash(hashedStreamMessage, hashedArrayMessage, absorbIterationsCount * 2);
      }
    }

    @Test
    @Tag("stringVersion")
    @Tag("streamVersion")
    @Tag("arrayVersion")
    @DisplayName("Hashing string with Stream and Array: Should not throw any exceptions")
    void hashingLargeStringWithStreamShouldNotThrowAnyExceptions() throws IOException {
      // given
      final String stringToHash =
          "HelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHello"
              + "HelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHello"
              + "HelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHello";
      final byte[] stringByteArray = stringToHash.getBytes(StandardCharsets.UTF_8);
      final int absorbIterationsCount =
          calculateNumberOfAbsorbIterations(stringByteArray.length, r) * 2;
      try (final InputStream is = new ByteArrayInputStream(stringByteArray)) {

        // when
        final byte[] hashedStreamMessage = spongeHashKeccak200.hash(is, stringToHash.length());
        final byte[] hashedArrayMessage = spongeHashKeccak200.hash(stringByteArray);

        // then
        verifyHash(hashedStreamMessage, hashedArrayMessage, absorbIterationsCount);
      }
    }

    @Test
    @DisplayName("Test Hash Stream Throws Exception")
    void testHashStreamThrowsException() throws IOException {
      // given
      final InputStream mockInputStream = mock(InputStream.class);
      when(mockInputStream.readNBytes(any(), anyInt(), anyInt())).thenThrow(IOException.class);

      // when & then
      final SpongeException spongeException =
          assertThrows(SpongeException.class, () -> spongeHashKeccak200.hash(mockInputStream, 1));
      assertEquals("An error has occurred when hashing: ", spongeException.getMessage());
    }

    private void verifyHash(
        final byte[] hashedStreamMessage,
        final byte[] hashedArrayMessage,
        final int absorbIterationsCount) {
      assertAll(
          verifyArraysAreEqual(hashedStreamMessage, hashedArrayMessage),
          hashAndAssertOutputSize(hashedStreamMessage, OUTPUT_LENGTH_BYTES),
          hashAndAssertOutputSize(hashedArrayMessage, OUTPUT_LENGTH_BYTES),
          () -> verify(spongeHashKeccak200, times(absorbIterationsCount)).absorb(any(), any()),
          () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount));
    }
  }

  private void verifyPermFuncsGetCalledNTimesRoundTimes(final int n) {
    verify(spongePermutationImpl, times(n * ROUNDS)).theta(any());
    verify(spongePermutationImpl, times(n * ROUNDS)).rhoPi(any());
    verify(spongePermutationImpl, times(n * ROUNDS)).chi(any());
    verify(spongePermutationImpl, times(n * ROUNDS)).iota(any(), anyInt());
  }
}
