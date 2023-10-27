package com.babkovic.hash;

import static com.babkovic.keccak200sync.Constants.ROUNDS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.babkovic.api.SpongeHash;
import com.babkovic.api.SpongePermutation;
import com.babkovic.keccak200sync.PermutationImpl;
import com.babkovic.keccak200sync.SpongeHashKeccak200Impl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

class SpongeHashImplTest {

  private final SpongePermutation spongePermutationImpl = spy(new PermutationImpl());
  private final SpongeHash spongeHashKeccak200 =
      spy(new SpongeHashKeccak200Impl(spongePermutationImpl));

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

  @Test
  void shouldNotThrowException_WhenCallingHashWithMessageLengthOfNotMultipleOf168(
      final TestInfo testInfo) {
    // given
    final int n = 2; // how many times will absorb phase iterate through message
    byte[] message = {
      33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127, 10, 33, -127,
      127, 1
    };

    // when & then
    assertDoesNotThrow(() -> spongeHashKeccak200.hash(message));
    verify(spongeHashKeccak200, times(n)).absorb(any(), any());
    verifyPermFuncsGetCalledNTimesRoundTimes(n);
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
