package com.babkovic.keccak200sync;

import static com.babkovic.keccak200sync.Constants.BITS_IN_BYTE;
import static com.babkovic.keccak200sync.Constants.ROUNDS;
import static com.babkovic.keccak200sync.Constants.r;
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

class SpongeHashImplTest {

  private SpongePermutation spongePermutationImpl;
  private SpongeHash spongeHashKeccak200;

  @BeforeEach
  void setUp() {
    spongePermutationImpl = spy(new PermutationImpl());
    spongeHashKeccak200 = spy(new SpongeHashKeccak200Impl(spongePermutationImpl));
  }

  @AfterEach
  void cleanUp() {
    spongePermutationImpl = null;
    spongeHashKeccak200 = null;
  }

  @Tag("arrayVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithSmallMessage(final TestInfo testInfo) {
    // given
    final int n = 1; // how many times will absorb phase iterate through message
    byte[] message = {
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127
    };

    // when & then
    assertDoesNotThrow(() -> spongeHashKeccak200.hash(message));
    verify(spongeHashKeccak200, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(n);
  }

  @Tag("arrayVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithMessageLengthOfMultipleOf168(
      final TestInfo testInfo) {
    // given
    final int n = 1; // how many times will absorb phase iterate through message
    byte[] message = {
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      127
    };

    // when & then
    assertDoesNotThrow(() -> spongeHashKeccak200.hash(message));
    verify(spongeHashKeccak200, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(1);
  }

  @Tag("arrayVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithMessageLengthOfNotMultipleOf168(
      final TestInfo testInfo) {
    // given
    final int n = 2; // how many times will absorb phase iterate through message
    byte[] message = {
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      127, 3
    };

    // when & then
    assertDoesNotThrow(() -> spongeHashKeccak200.hash(message));
    verify(spongeHashKeccak200, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(n);
  }

  @Tag("arrayVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithVeryLargeMessage(final TestInfo testInfo) {
    // given
    final int n = 4876; // how many times will absorb phase iterate through message
    byte[] message = new byte[102_393];

    // when & then
    assertDoesNotThrow(() -> spongeHashKeccak200.hash(message));
    verify(spongeHashKeccak200, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(n);
  }

  @Tag("streamVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithStreamSmallMessage(final TestInfo testInfo) {
    // given
    final int n = 1; // how many times will absorb phase iterate through message
    byte[] message = {33, -127, 10, 33, -127, 10, 33, -127};
    final InputStream is = new ByteArrayInputStream(message);

    // when & then
    assertDoesNotThrow(() -> spongeHashKeccak200.hash(is, message.length));
    verify(spongeHashKeccak200, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(n);
  }

  @Tag("streamVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithStreamMessageLengthOfMultipleOf168(
      final TestInfo testInfo) {
    // given
    final int n = 1; // how many times will absorb phase iterate through message
    byte[] message = {
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      127
    };
    final InputStream is = new ByteArrayInputStream(message);

    // when & then
    assertDoesNotThrow(() -> spongeHashKeccak200.hash(is, message.length));
    verify(spongeHashKeccak200, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(n);
  }

  @Tag("streamVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithStreamMessageLengthOfNotMultipleOf168(
      final TestInfo testInfo) {
    // given
    final int n = 2; // how many times will absorb phase iterate through message
    byte[] message = {
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      127, 3
    };
    final InputStream is = new ByteArrayInputStream(message);

    // when & then
    assertDoesNotThrow(() -> spongeHashKeccak200.hash(is, message.length));
    verify(spongeHashKeccak200, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(n);
  }

  @Tag("streamVersion")
  @Test
  void shouldNotThrowException_WhenCallingHashWithStreamLongMessage(final TestInfo testInfo) {
    // given
    final int n = 4992; // how many times will absorb phase iterate through message
    byte[] message = new byte[1_048_12];
    final InputStream is = new ByteArrayInputStream(message);

    // when & then
    assertDoesNotThrow(() -> spongeHashKeccak200.hash(is, message.length));
    verify(spongeHashKeccak200, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(n);
  }

  @Tag("streamVersion")
  @Tag("performanceHeavy")
  @Test
  void shouldNotThrowException_WhenCallingHashWithStreamImageMessage(final TestInfo testInfo)
      throws IOException {
    // given
    try (final InputStream is =
        new FileInputStream("src/test/resources/video.mp4")) {
      final int fileSize = is.available();
      final int n =
          1 + fileSize / (r / BITS_IN_BYTE); // how many times will absorb phase iterate through message

      // when & then
      assertDoesNotThrow(() -> spongeHashKeccak200.hash(is, fileSize));
      verify(spongeHashKeccak200, times(n)).absorb(any(), any());
      verifyPermFuncsGetCalledNTimesRoundTimes(n);
    }
  }

  @Test
  void shouldReturnOriginalMessage_WhenApplyPaddingWithMultipleOfr(final TestInfo testInfo) {
    // given
    final byte[] message = new byte[168 * 9];
    message[0] = 1; // just to check if all bytes are not set to null
    // when
    final byte[] retMessage = spongeHashKeccak200.applyPadding(message);

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
    final byte[] retMessage = spongeHashKeccak200.applyPadding(message);

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
    final byte[] retMessage = spongeHashKeccak200.applyPadding(message);

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
            () -> spongeHashKeccak200.initState(new byte[1]),
            String.format(
                "The test %s failed on asserting an exception", testInfo.getDisplayName()));

    // then
    assertEquals("Incorrect size of state. Should be 25.", ex.getMessage());
  }

  @Test
  void shouldInitStateWithValue_WhenStateLengthIs25(final TestInfo testInfo) {
    // given
    byte[] state = new byte[25];

    // when
    spongeHashKeccak200.initState(state);

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
