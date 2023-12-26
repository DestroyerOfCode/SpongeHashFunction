package com.babkovic.keccak1600output256;

import static com.babkovic.common.Constants.KECCAK_LANE;
import static com.babkovic.common.Constants.KECCAK_SIDE;
import static com.babkovic.common.Utils.MOD_5;
import static com.babkovic.common.Utils.rol64;
import static com.babkovic.keccak1600output256.Constants.KECCAK_1600_PI_LANE;
import static com.babkovic.keccak1600output256.Constants.KECCAK_1600_ROTATION_CONSTANTS;
import static com.babkovic.keccak1600output256.Constants.KECCAK_1600_ROUND_CONSTANTS;
import static com.babkovic.keccak1600output256.Constants.ROUNDS;

import com.babkovic.api.SpongePermutation;

public class PermutationImpl implements SpongePermutation<long[]> {

  /**
   * Performs the permutation phase of the Keccak algorithm. It repeatedly applies the
   * transformation rounds on the state.
   *
   * @param state The state array that is transformed through the permutation rounds.
   */
  @Override
  public void permute(final long[] state) {
    for (int i = 0; i < ROUNDS; i++) {
      theta(state);
      rhoPi(state);
      chi(state);
      iota(state, i);
    }
  }

  /**
   * The theta step of the Keccak permutation phase. It XORs each bit in a lane with the parity of
   * two other lanes in its column.
   *
   * @param state The state array on which the theta step is performed.
   */
  @Override
  public void theta(final long[] state) {
    final long[] bc = new long[KECCAK_LANE];

    for (int i = 0; i < KECCAK_LANE; i++) {
      bc[i] = state[i] ^ state[5 + i] ^ state[10 + i] ^ state[15 + i] ^ state[20 + i];
    }

    for (int i = 0; i < KECCAK_LANE; i++) {
      final long temp = bc[MOD_5[i + 4]] ^ rol64(bc[MOD_5[i + 1]], 1);
      for (int j = 0; j < KECCAK_SIDE; j += 5) {
        state[i + j] ^= temp;
      }
    }
  }

  /**
   * The rho and pi steps of the Keccak permutation combined. It rotates the bits of each lane, then
   * permutes the lanes.
   *
   * @param state The state array on which the rho and pi steps are performed.
   */
  @Override
  public void rhoPi(final long[] state) {
    long temp = state[1];
    long c;

    for (int i = 0; i < KECCAK_SIDE - 1 /* 24 */; i++) {
      c = state[KECCAK_1600_PI_LANE[i]];
      state[KECCAK_1600_PI_LANE[i]] = rol64(temp, KECCAK_1600_ROTATION_CONSTANTS[i]);
      temp = c;
    }
  }

  /**
   * The chi step of the Keccak permutation phase. It combines bits from each column of a lane.
   *
   * @param state The state array on which the chi step is performed.
   */
  @Override
  public void chi(final long[] state) {
    final long[] bc = new long[KECCAK_LANE];

    for (int i = 0; i < KECCAK_SIDE; i += 5) {
      System.arraycopy(state, i, bc, 0, KECCAK_LANE);

      for (int j = 0; j < KECCAK_LANE; j++) {
        state[i + j] = bc[j] ^ ((~bc[MOD_5[j + 1]]) & bc[MOD_5[j + 2]]);
      }
    }
  }

  /**
   * The iota step of the Keccak permutation phase. It modifies the state based on the round
   * constant.
   *
   * @param state The state array on which the iota step is performed.
   * @param round The round number, which determines the round constant used.
   */
  @Override
  public void iota(final long[] state, final int round) {
    state[0] ^= KECCAK_1600_ROUND_CONSTANTS[round];
  }
}
