package com.babkovic.hash;

import com.babkovic.babkovic.hash.SpongeHash;
import com.babkovic.babkovic.hash.SpongePermutation;
import com.babkovic.keccak_200.PermutationImpl;
import com.babkovic.keccak_200.SpongeHashKeccak200Impl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

class SpongeHashImplTest {

  private final SpongePermutation spongePermutationImpl = new PermutationImpl();
  private final SpongeHash spongeHashKeccak200 = new SpongeHashKeccak200Impl(spongePermutationImpl);

  @Test
  void shouldReturnOriginalMessageWhenApplyPaddingWithLargeMessage(final TestInfo testInfo) {
    // given
    final byte[] message = new byte[1024];

    // when
    final byte[] retMessage = spongeHashKeccak200.applyPadding(message);

    // then
    for (int i = 0; i < retMessage.length; i++) {
      Assertions.assertEquals(retMessage[i], message[i]);
    }
  }

  @Test
  void shouldReturnPaddedMessageWhenApplyPaddingWithSmallMessage(final TestInfo testInfo) {
    // given
    final byte[] message = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};

    // when
    final byte[] retMessage = spongeHashKeccak200.applyPadding(message);

    // then
    for (int i = 0; i < message.length; i++) {
      Assertions.assertEquals(retMessage[i], message[i]);
    }
    for (int i = message.length; i < retMessage.length; i++) {
      Assertions.assertEquals(retMessage[i], 0);
    }
  }
}
