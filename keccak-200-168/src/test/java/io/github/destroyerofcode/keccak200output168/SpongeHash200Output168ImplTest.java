package io.github.destroyerofcode.keccak200output168;

import static io.github.destroyerofcode.TestUtils.calculateNumberOfAbsorbIterations;
import static io.github.destroyerofcode.TestUtils.hashAndAssertOutputSize;
import static io.github.destroyerofcode.TestUtils.toByteArray;
import static io.github.destroyerofcode.TestUtils.verifyArraysAreEqual;
import static io.github.destroyerofcode.keccak200output168.Constants.BYTES_IN_r;
import static io.github.destroyerofcode.keccak200output168.Constants.OUTPUT_LENGTH_BYTES;
import static io.github.destroyerofcode.keccak200output168.Constants.ROUNDS;
import static io.github.destroyerofcode.keccak200output168.Constants.r;
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

import io.github.destroyerofcode.api.SpongeHash;
import io.github.destroyerofcode.api.SpongePermutation;
import io.github.destroyerofcode.exception.SpongeException;
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

public class SpongeHash200Output168ImplTest {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(SpongeHash200Output168ImplTest.class);
  private SpongePermutation<byte[]> spongePermutationImpl;
  private SpongeHash<byte[]> spongeHashKeccak200;

  @BeforeEach
  void setUp(final TestInfo testInfo) {
    LOGGER.info(() -> String.format("Starting test: %s", testInfo.getDisplayName()));
    spongePermutationImpl = spy(new PermutationImpl());
    spongeHashKeccak200 = spy(new SpongeHashKeccak200Output168Impl(spongePermutationImpl));
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
    @DisplayName("Ensure padding is added when message size is not a multiple of 'r'")
    void testPaddingAddedForNonMultipleOfBlockSize() {
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
    @DisplayName("Verify padding preserves original message when size is already a multiple of 'r'")
    void testPaddingPreservesMessageForBlockSizeMultiple() {
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
    @DisplayName("Confirm padding behaves for larger messages")
    void testPaddingCorrectForMultipleOfr() {
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
  @DisplayName("Hash Functionality Tests")
  class HashFunctionalityTest {
    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Test
    @DisplayName("Hashing should handle small message arrays without errors")
    void testHashingSmallMessageArrayWithoutErrors() throws IOException {
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
    @DisplayName("Hashing should handle messages with size as multiple of r without errors")
    void testHashingMessageWithSizeMultipleOf168WithoutErrors() throws IOException {
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
    @DisplayName("Hashing should handle messages with size not a multiple of r without errors")
    void testHashingMessageWithSizeNotMultipleOfrWithoutErrors() throws IOException {
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
    @DisplayName("Hashing should process very large message arrays without errors")
    void testHashingVeryLargeMessageArrayWithoutErrors() throws IOException {
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
    @DisplayName("Hashing a small message stream should be consistent with array hashing")
    void testConsistencyBetweenStreamAndArrayHashingForSmallMessage() throws IOException {
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
    @DisplayName("Hashing a stream with size as multiple of 168 should be consistent")
    void testConsistencyForStreamWithSizeMultipleOf168() throws IOException {
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
    @DisplayName("Hashing a stream with size not a multiple of 168 should be consistent")
    void testConsistencyForStreamWithSizeNotMultipleOf168() throws IOException {
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
    @DisplayName("Hashing a long message should not produce errors")
    void testHashingLongMessageWithoutErrors() throws IOException {
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
    @DisplayName("Hashing a video file stream should complete without throwing exceptions")
    void testHashingStreamVideoFileWithoutExceptions() throws IOException {
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
    @DisplayName("Hashing a short text string using both stream and array should match")
    void testHashingShortTextStringMatchesForStreamAndArray() throws IOException {
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
    @DisplayName("Hashing a long text string using both stream and array should match")
    void testHashingLongTextStringMatchesForStreamAndArray() throws IOException {
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
    @DisplayName("Hashing should correctly throw exception on InputStream error")
    void testHashingHandlesInputStreamExceptionsCorrectly() throws IOException {
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
