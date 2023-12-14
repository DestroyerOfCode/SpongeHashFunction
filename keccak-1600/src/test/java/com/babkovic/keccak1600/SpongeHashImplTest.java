package com.babkovic.keccak1600;

import static com.babkovic.keccak1600.Constants.BITS_IN_BYTE;
import static com.babkovic.keccak1600.Constants.ROUNDS;
import static com.babkovic.keccak1600.Constants.STATE_BYTE_LENGTH;
import static com.babkovic.keccak1600.Constants.r;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.function.Executable;

class SpongeHashImplTest {

  private SpongePermutation spongePermutationImpl;
  private SpongeHash spongeHashKeccak1600;

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

  @Tag("arrayVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithSmallMessage(final TestInfo testInfo) {
    // given
    final int n = 1; // how many times will absorb phase iterate through message
    // 143 elements, just 1 before the max. 144 = r/bits_in_byte
    byte[] message = {
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10
    };

    // when & then
    assertDoesNotThrow(hashAndAssertSize(spongeHashKeccak1600.hash(message)));
    verify(spongeHashKeccak1600, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(n);
  }

  @Tag("arrayVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithMessageLengthOfMultipleOf1152(
      final TestInfo testInfo) {
    // given
    final int n = 1; // how many times will absorb phase iterate through message
    byte[] message = {
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      127, 1, 1, 1
    };

    // when & then
    assertDoesNotThrow(hashAndAssertSize(spongeHashKeccak1600.hash(message)));
    verify(spongeHashKeccak1600, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(1);
  }

  @Tag("arrayVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithMessageLengthOfNotMultipleOf1152(
      final TestInfo testInfo) {
    // given
    final int n = 2; // how many times will absorb phase iterate through message
    // 145
    byte[] message = {
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      127, 3, 1, 1, 1
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
                        / BITS_IN_BYTE)); // how many times will absorb phase iterate through
    // message

    byte[] message = new byte[arraySize];

    // when & then
    assertDoesNotThrow(hashAndAssertSize(spongeHashKeccak1600.hash(message)));
    verify(spongeHashKeccak1600, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(n);
  }

  @Tag("streamVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithStreamSmallMessage(final TestInfo testInfo) {
    // given
    final int n = 1; // how many times will absorb phase iterate through message
    final byte[] message = {33, -127, 10, 33, -127, 10, 33, -127};
    final InputStream is = new ByteArrayInputStream(message);

    // when & then
    assertDoesNotThrow(hashAndAssertSize(spongeHashKeccak1600.hash(is, message.length)));
    verify(spongeHashKeccak1600, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(n);
  }

  @Tag("streamVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithStreamMessageLengthOfMultipleOf1152(
      final TestInfo testInfo) {
    // given
    final int n = 1; // how many times will absorb phase iterate through message
    byte[] message = {
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      127
    };
    final InputStream is = new ByteArrayInputStream(message);

    // when & then
    assertDoesNotThrow(hashAndAssertSize(spongeHashKeccak1600.hash(is, message.length)));
    verify(spongeHashKeccak1600, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(n);
  }

  private Executable hashAndAssertSize(final byte[] spongeHashKeccak1600) {
    return () -> assertEquals(28, spongeHashKeccak1600.length);
  }

  @Tag("streamVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithStreamMessageLengthOfNotMultipleOf1152(
      final TestInfo testInfo) {
    // given
    final int n = 2; // how many times will absorb phase iterate through message
    byte[] message = {
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      127, 3, 1, 1, 1
    };
    final InputStream is = new ByteArrayInputStream(message);

    // when & then
    assertDoesNotThrow(hashAndAssertSize(spongeHashKeccak1600.hash(is, message.length)));
    verify(spongeHashKeccak1600, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(n);
  }

  @Tag("streamVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithStreamLongMessage(final TestInfo testInfo) {
    // given
    final int arraySize = 104_812;
    final int n =
        (int)
            Math.ceil(
                (double) arraySize
                    / ((double) r
                        / BITS_IN_BYTE)); // how many times will absorb phase iterate through

    byte[] message = new byte[arraySize];
    final InputStream is = new ByteArrayInputStream(message);

    // when & then
    assertDoesNotThrow(hashAndAssertSize(spongeHashKeccak1600.hash(is, message.length)));
    verify(spongeHashKeccak1600, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(n);
  }

  @Tag("streamVersion")
  @Tag("performanceHeavy")
  @Test
  void shouldNotThrowException_WhenCallingHashWithStreamImageMessage(final TestInfo testInfo)
      throws IOException {
    // given
    try (final InputStream is = new FileInputStream("src/test/resources/video.mp4")) {
      final int fileSize = is.available();
      final int n =
          (int)
              Math.ceil(
                  (double) fileSize
                      / ((double) r
                          / BITS_IN_BYTE)); // how many times will absorb phase iterate through
      // message

      // when & then
      assertDoesNotThrow(hashAndAssertSize(spongeHashKeccak1600.hash(is, fileSize)));
      verify(spongeHashKeccak1600, times(n)).absorb(any(), any());
      verifyPermFuncsGetCalledNTimesRoundTimes(n);
    }
  }

  @Test
  void shouldReturnOriginalMessage_WhenApplyPaddingWithMultipleOfr(final TestInfo testInfo) {
    // given
    final byte[] message = new byte[r * 9];
    message[0] = 1; // just to check if all bytes are not set to null
    // when
    final byte[] retMessage = spongeHashKeccak1600.applyPadding(message);

    // then
    for (int i = 0; i < retMessage.length; i++) {
      assertEquals(retMessage[i], message[i]);
    }
  }

  @Test
  void shouldReturnOriginalMessage_WhenApplyPaddingWithoutMultipleOfr(final TestInfo testInfo) {
    // given
    byte[] message = new byte[1024];
    message[0] = 1;

    // when
    final byte[] retMessage = spongeHashKeccak1600.applyPadding(message);

    // then
    for (int i = 0; i < message.length; i++) {
      assertEquals(retMessage[i], message[i]);
    }

    for (int i = message.length; i < retMessage.length; i++) {
      assertEquals(0, retMessage[i]);
    }
  }

  @Test
  void shouldReturnPaddedMessage_WhenApplyPaddingWithSmallMessage(final TestInfo testInfo) {
    // given
    final byte[] message = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};

    // when
    final byte[] retMessage = spongeHashKeccak1600.applyPadding(message);

    // then
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
    RuntimeException ex =
        Assertions.assertThrows(
            RuntimeException.class,
            () -> spongeHashKeccak1600.initState(new byte[1]),
            String.format(
                "The test %s failed on asserting an exception", testInfo.getDisplayName()));

    // then
    assertEquals("Incorrect size of state. Should be 200.", ex.getMessage());
  }

  @Test
  void shouldInitStateWithValue_WhenStateLengthIs200Bytes(final TestInfo testInfo) {
    // given
    byte[] state = new byte[STATE_BYTE_LENGTH];

    // when
    spongeHashKeccak1600.initState(state);

    // then
    for (byte b : state) {
      assertEquals(b, 0b01010101);
    }
  }

  private void verifyPermFuncsGetCalledNTimesRoundTimes(final int n) {
    verify(spongePermutationImpl, times(n * ROUNDS)).theta(any());
    verify(spongePermutationImpl, times(n * ROUNDS)).rhoPi(any());
    verify(spongePermutationImpl, times(n * ROUNDS)).chi(any());
    verify(spongePermutationImpl, times(n * ROUNDS)).iota(any(), anyInt());
  }
}
