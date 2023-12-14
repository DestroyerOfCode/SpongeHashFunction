package com.babkovic.keccak1600;

import static com.babkovic.common.Utils.MOD_5;
import static com.babkovic.common.Utils.rol8;
import static com.babkovic.keccak1600.Constants.KECCAK_1600_PI_LANE;
import static com.babkovic.keccak1600.Constants.KECCAK_1600_ROTATION_CONSTANTS;
import static com.babkovic.keccak1600.Constants.KECCAK_1600_ROUND_CONSTANTS;
import static com.babkovic.keccak1600.Constants.KECCAK_LANE;
import static com.babkovic.keccak1600.Constants.ROUNDS;

import com.babkovic.api.SpongePermutation;

public class PermutationImpl implements SpongePermutation {

  @Override
  public void permute(final byte[] state) {
    for (int i = 0; i < ROUNDS; i++) {
      theta(state);
      rhoPi(state);
      chi(state);
      iota(state, i);
    }
  }

  @Override
  public void theta(final byte[] state) {
    final byte[] c = new byte[KECCAK_LANE];

    for (int i = 0; i < KECCAK_LANE; i++) {
      c[i] = (byte) (state[i] ^ state[5 + i] ^ state[10 + i] ^ state[15 + i] ^ state[20 + i]);
    }

    for (int i = 0; i < KECCAK_LANE; i++) {
      final byte temp = (byte) (c[MOD_5[i + 4]] ^ rol8(c[MOD_5[i + 1]], 1));
      for (int j = 0; j < 25; j += 5) {
        state[i + j] ^= temp;
      }
    }
  }

  @Override
  public void rhoPi(final byte[] state) {
    byte temp = state[1];
    byte c;

    for (int i = 0; i < 24; i++) {
      c = state[KECCAK_1600_PI_LANE[i]];
      state[KECCAK_1600_PI_LANE[i]] = rol8(temp, KECCAK_1600_ROTATION_CONSTANTS[i]);
      temp = c;
    }
  }

  @Override
  public void chi(final byte[] state) {
    final byte[] c = new byte[KECCAK_LANE];

    for (int i = 0; i < 25; i += 5) {
      System.arraycopy(state, i, c, 0, KECCAK_LANE);

      for (int j = 0; j < KECCAK_LANE; j++) {
        state[i + j] = (byte) (c[j] ^ ((~c[MOD_5[j + 1]]) & c[MOD_5[j + 2]]));
      }
    }
  }

  @Override
  public void iota(final byte[] state, final int round) {
    state[0] ^= KECCAK_1600_ROUND_CONSTANTS[round];
  }
}
