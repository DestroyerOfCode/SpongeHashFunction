package io.github.destroyerofcode.keccak1600output256;

import static io.github.destroyerofcode.TestUtils.byteArrayToLongArray;
import static io.github.destroyerofcode.TestUtils.calculateNumberOfAbsorbIterations;
import static io.github.destroyerofcode.TestUtils.longArrayToByteArray;
import static io.github.destroyerofcode.TestUtils.toByteArray;
import static io.github.destroyerofcode.TestUtils.verifyArraysAreEqual;
import static io.github.destroyerofcode.common.Constants.BITS_IN_LONG;
import static io.github.destroyerofcode.common.Constants.BYTES_IN_LONG;
import static io.github.destroyerofcode.common.Utils.nearestGreaterMultiple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.destroyerofcode.TestUtils;
import io.github.destroyerofcode.api.SpongeHash;
import io.github.destroyerofcode.api.SpongePermutation;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

public class SpongeHash1600Output256ImplTest {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(SpongeHash1600Output256ImplTest.class);

  private SpongePermutation<long[]> spongePermutationImpl;
  private SpongeHash<long[]> spongeHashKeccak1600;

  @BeforeEach
  void setUp(final TestInfo testInfo) {
    LOGGER.info(() -> String.format("Starting test: %s", testInfo.getDisplayName()));
    spongePermutationImpl = spy(new PermutationImpl());
    spongeHashKeccak1600 = spy(new SpongeHashKeccak1600Output256Impl(spongePermutationImpl));
  }

  @AfterEach
  void cleanUp(final TestInfo testInfo) {
    LOGGER.info(() -> String.format("Ending test: %s", testInfo.getDisplayName()));
    spongePermutationImpl = null;
    spongeHashKeccak1600 = null;
  }

  @Nested
  @DisplayName("Padding Behavior Tests")
  class PaddingBehaviorTests {
    @Test
    @DisplayName("Should retain original message when padding multiple of rate 'r'")
    void testOriginalMessageRetainedWhenPaddingMultipleOfRate() {
      // given
      final long[] message = new long[Constants.r * 9];
      message[0] = 1L; // just to check if all bytes are not set to null

      // when
      final long[] retMessage = spongeHashKeccak1600.applyPadding(message);

      // then
      assertAll(
          () -> assertEquals(1L, retMessage[0]),
          () -> Assertions.assertEquals(Constants.r * 9, message.length));
    }

    @Test
    @DisplayName("Should add padding when padding not multiple of rate 'r'")
    void testAddPaddingWhenPaddingNotMultipleOfRate() {
      // given
      final long[] message = new long[1024];
      final int paddedMessageLength =
          nearestGreaterMultiple(message.length, Constants.r / BITS_IN_LONG);
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
    @DisplayName("Should correctly pad a message shorter than rate 'r'")
    void testCorrectPaddingForShortMessage() {
      // given
      final long[] message = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};

      // when
      final long[] retMessage = spongeHashKeccak1600.applyPadding(message);

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
  }

  @Nested
  @DisplayName("Hashing Functionality Tests")
  class HashingFunctionalityTests {

    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Test
    @DisplayName("Should hash small message arrays without throwing exceptions")
    void testHashingSmallMessage() throws IOException {
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
          calculateNumberOfAbsorbIterations(message.length * BYTES_IN_LONG, Constants.r) * 2;
      try (final InputStream is = new ByteArrayInputStream(longArrayToByteArray(message))) {

        // when
        final long[] hashedArrayMessage = spongeHashKeccak1600.hash(message);
        final long[] hashedStreamMessage =
            spongeHashKeccak1600.hash(is, message.length * BYTES_IN_LONG);

        // then
        assertHashing(hashedStreamMessage, hashedArrayMessage, absorbIterationsCount);
      }
    }

    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Test
    @DisplayName("Should hash message arrays with length multiple of 1088 without exceptions")
    void testHashingMessageArraysWithMultipleOf1088Length() throws IOException {
      // given
      // 17 elements, because exactly 17 longs fit to 1088 bits
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
          calculateNumberOfAbsorbIterations(message.length * BYTES_IN_LONG, Constants.r) * 2;
      try (final InputStream is = new ByteArrayInputStream(longArrayToByteArray(message))) {

        // when
        final long[] hashedArrayMessage = spongeHashKeccak1600.hash(message);
        final long[] hashedStreamMessage =
            spongeHashKeccak1600.hash(is, message.length * BYTES_IN_LONG);

        // then
        assertHashing(hashedStreamMessage, hashedArrayMessage, absorbIterationsCount);
      }
    }

    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Test
    @DisplayName("Should hash message with length not multiple of 1088 without exceptions")
    void testHashingMessageWithNonMultipleOf1088Length() throws IOException {
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
          calculateNumberOfAbsorbIterations(message.length * BYTES_IN_LONG, Constants.r) * 2;
      try (final InputStream is = new ByteArrayInputStream(longArrayToByteArray(message))) {

        // when
        final long[] hashedStreamMessage =
            spongeHashKeccak1600.hash(is, message.length * BYTES_IN_LONG);
        final long[] hashedArrayMessage = spongeHashKeccak1600.hash(message);

        // then
        assertHashing(hashedStreamMessage, hashedArrayMessage, absorbIterationsCount);
      }
    }

    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Test
    @DisplayName("Should hash message arrays with 40 longs without throwing exceptions")
    void testHashingLongMessageArraysWith40Longs() throws IOException {
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
          calculateNumberOfAbsorbIterations(message.length * BYTES_IN_LONG, Constants.r) * 2;
      try (final InputStream is = new ByteArrayInputStream(longArrayToByteArray(message))) {

        // when
        final long[] hashedStreamMessage =
            spongeHashKeccak1600.hash(is, message.length * BYTES_IN_LONG);
        final long[] hashedArrayMessage = spongeHashKeccak1600.hash(message);

        // then
        assertHashing(hashedStreamMessage, hashedArrayMessage, absorbIterationsCount);
      }
    }

    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Test
    @DisplayName("Should hash very large message arrays without throwing exceptions")
    void testHashingVeryLargeMessageArrays() throws IOException {
      // given
      final int arraySize = 10_239;
      final int absorbIterationsCount =
          calculateNumberOfAbsorbIterations(arraySize * BYTES_IN_LONG, Constants.r) * 2;
      final long[] message = new long[arraySize];
      try (final InputStream is = new ByteArrayInputStream(longArrayToByteArray(message))) {

        // when
        final long[] hashedStreamMessage = spongeHashKeccak1600.hash(message);
        final long[] hashedArrayMessage =
            spongeHashKeccak1600.hash(is, message.length * BYTES_IN_LONG);

        // then
        assertHashing(hashedStreamMessage, hashedArrayMessage, absorbIterationsCount);
      }
    }

    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Test
    @DisplayName("Should hash small non-8-byte-aligned streams without throwing exceptions")
    void testHashingSmallNon8ByteAlignedStreams() throws IOException {
      // given
      // 7
      final byte[] message = {33, -127, 10, 33, -127, 10, 33};
      try (final InputStream is = new ByteArrayInputStream(message)) {

        final int absorbIterationsCount =
            calculateNumberOfAbsorbIterations(message.length * BYTES_IN_LONG, Constants.r) * 2;

        // when
        final long[] hashedStreamMessage = spongeHashKeccak1600.hash(is, message.length);
        final long[] hashedArrayMessage = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

        // then
        assertHashing(hashedStreamMessage, hashedArrayMessage, absorbIterationsCount);
      }
    }

    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Test
    @DisplayName("Should hash small 8-byte-aligned streams without throwing exceptions - 1")
    void testHashingSmall8ByteAlignedStreams_1() throws IOException {
      // given
      // 8
      final byte[] message = {33, -127, 10, 33, -127, 10, 33, 11};
      try (final InputStream is = new ByteArrayInputStream(message)) {

        final int absorbIterationsCount =
            calculateNumberOfAbsorbIterations(message.length, Constants.r) * 2;

        // when
        final long[] hashedStreamMessage = spongeHashKeccak1600.hash(is, message.length);
        final long[] hashedArrayMessage = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

        // then
        assertHashing(hashedStreamMessage, hashedArrayMessage, absorbIterationsCount);
      }
    }

    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Test
    @DisplayName("Should hash small 8-byte-aligned streams without throwing exceptions - 2")
    void testHashingSmall8ByteAlignedStreams_2() throws IOException {
      // given
      final byte[] message = {33, -127, 10, 33, -127, 10, 33, 13}; // 8
      try (final InputStream is = new ByteArrayInputStream(message)) {

        final int absorbIterationsCount =
            calculateNumberOfAbsorbIterations(message.length, Constants.r) * 2;

        // when
        final long[] hashedStreamMessage = spongeHashKeccak1600.hash(is, message.length);
        final long[] hashedArrayMessage = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

        // then
        assertHashing(hashedStreamMessage, hashedArrayMessage, absorbIterationsCount);
      }
    }

    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Test
    @DisplayName("Should hash streams with length multiple of 1088 without exceptions")
    void testHashingStreamsWithMultipleOf1088Length() throws IOException {
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
      final int absorbIterationsCount =
          calculateNumberOfAbsorbIterations(message.length, Constants.r) * 2;
      try (final InputStream is = new ByteArrayInputStream(message)) {

        // when
        final long[] hashedStreamMessage = spongeHashKeccak1600.hash(is, message.length);
        final long[] hashedArrayMessage = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

        // then
        assertHashing(hashedStreamMessage, hashedArrayMessage, absorbIterationsCount);
      }
    }

    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Test
    @DisplayName("Should hash streams with length not multiple of 1088 without exceptions - 1")
    void testHashingStreamsWithNonMultipleOf1088Length_1() throws IOException {
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
      try (final InputStream is = new ByteArrayInputStream(message)) {
        final int absorbIterationsCount =
            calculateNumberOfAbsorbIterations(message.length, Constants.r) * 2;

        // when
        final long[] hashedStreamMessage = spongeHashKeccak1600.hash(is, message.length);
        final long[] hashedArrayMessage = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

        // then
        assertHashing(hashedStreamMessage, hashedArrayMessage, absorbIterationsCount);
      }
    }

    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Test
    @DisplayName("Should hash streams with length not multiple of 1088 without exceptions - 2")
    void testHashingStreamsWithNonMultipleOf1088Length_2() throws IOException {
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
      try (final InputStream is = new ByteArrayInputStream(message)) {
        final int absorbIterationsCount =
            calculateNumberOfAbsorbIterations(message.length, Constants.r) * 2;

        // when
        final long[] hashedStreamMessage = spongeHashKeccak1600.hash(is, message.length);
        final long[] hashedArrayMessage = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

        // then
        assertHashing(hashedStreamMessage, hashedArrayMessage, absorbIterationsCount);
      }
    }

    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Test
    @DisplayName("Should hash long streams without throwing exceptions")
    void testHashingLongStreams() throws IOException {
      // given
      final int arraySize = 10_412;
      final byte[] message = new byte[arraySize];
      final int absorbIterationsCount =
          calculateNumberOfAbsorbIterations(message.length, Constants.r) * 2;

      try (final InputStream is = new ByteArrayInputStream(message)) {

        // when
        final long[] hashedStreamMessage = spongeHashKeccak1600.hash(is, message.length);
        final long[] hashedArrayMessage = spongeHashKeccak1600.hash(byteArrayToLongArray(message));

        // then
        assertHashing(hashedStreamMessage, hashedArrayMessage, absorbIterationsCount);
      }
    }

    @Tag("streamVersion")
    @Tag("arrayVersion")
    @Tag("performanceHeavy")
    @Test
    @DisplayName("Should hash video file streams without throwing exceptions")
    void testHashingVideoFileStreams() throws IOException {
      // given
      final String filePath = "src/test/resources/video.mp4";
      try (final InputStream is = new FileInputStream(filePath);
          final InputStream is1 = new FileInputStream(filePath)) {
        final Path path = Paths.get(filePath);
        final int fileSize = (int) Files.size(path);
        final byte[] byteArrayStream = toByteArray(is1, is1.available());
        final long[] longArrayStream = byteArrayToLongArray(byteArrayStream);
        final int absorbIterationsCount =
            calculateNumberOfAbsorbIterations(fileSize, Constants.r) * 2;

        // when
        final long[] hashedStreamMessage = spongeHashKeccak1600.hash(is, fileSize);
        final long[] hashedArrayMessage = spongeHashKeccak1600.hash(longArrayStream);

        // then
        assertHashing(hashedStreamMessage, hashedArrayMessage, absorbIterationsCount);
      }
    }

    @Nested
    @DisplayName("String Hashing Tests")
    class StringHashingTests {
      @Test
      @Tag("stringVersion")
      @DisplayName("Hashing a small string with Stream and Array: Should not throw any exceptions")
      void hashingSmallStringWithStreamShouldNotThrowAnyExceptions() {
        // given
        final String stringToHash = "Hello ";
        final int absorbIterationsCount =
            calculateNumberOfAbsorbIterations(
                    stringToHash.getBytes(StandardCharsets.UTF_8).length, Constants.r)
                * 2;

        // when
        final long[] hashedStreamMessage =
            spongeHashKeccak1600.hash(
                new ByteArrayInputStream(stringToHash.getBytes(StandardCharsets.UTF_8)),
                stringToHash.getBytes().length);
        final long[] hashedArrayMessage =
            spongeHashKeccak1600.hash(
                byteArrayToLongArray(stringToHash.getBytes(StandardCharsets.UTF_8)));

        // then
        assertHashing(hashedStreamMessage, hashedArrayMessage, absorbIterationsCount);
      }

      @Test
      @Tag("stringVersion")
      @DisplayName("Hashing a large string with Stream and Array: Should not throw any exceptions")
      void hashingLargeStringWithStreamShouldNotThrowAnyExceptions() {
        // given
        final String stringToHash =
            "HelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHello"
                + "HelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHello"
                + "HelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHello";

        final byte[] stringByteArray = stringToHash.getBytes(StandardCharsets.UTF_8);
        final long[] stringLongArray = byteArrayToLongArray(stringByteArray);
        final int absorbIterationsCount =
            calculateNumberOfAbsorbIterations(stringByteArray.length, Constants.r) * 2;

        // when
        final long[] hashedStreamMessage =
            spongeHashKeccak1600.hash(
                new ByteArrayInputStream(stringByteArray), stringByteArray.length);
        final long[] hashedArrayMessage = spongeHashKeccak1600.hash(stringLongArray);

        // then
        assertHashing(hashedStreamMessage, hashedArrayMessage, absorbIterationsCount);
      }
    }
  }

  private void assertHashing(
      final long[] hashedStreamMessage,
      final long[] hashedArrayMessage,
      final int absorbIterationsCount) {
    assertAll(
        verifyArraysAreEqual(hashedStreamMessage, hashedArrayMessage),
        TestUtils.hashAndAssertOutputSize(hashedStreamMessage, Constants.OUTPUT_LENGTH_LONGS),
        TestUtils.hashAndAssertOutputSize(hashedArrayMessage, Constants.OUTPUT_LENGTH_LONGS),
        () -> verify(spongeHashKeccak1600, times(absorbIterationsCount)).absorb(any(), any()),
        () -> verifyPermFuncsGetCalledNTimesRoundTimes(absorbIterationsCount));
  }

  private void verifyPermFuncsGetCalledNTimesRoundTimes(final int n) {
    verify(spongePermutationImpl, times(n * Constants.ROUNDS)).theta(any());
    verify(spongePermutationImpl, times(n * Constants.ROUNDS)).rhoPi(any());
    verify(spongePermutationImpl, times(n * Constants.ROUNDS)).chi(any());
    verify(spongePermutationImpl, times(n * Constants.ROUNDS)).iota(any(), anyInt());
  }
}
