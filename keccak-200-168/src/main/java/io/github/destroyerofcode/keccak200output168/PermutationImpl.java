package io.github.destroyerofcode.keccak200output168;

import static io.github.destroyerofcode.common.Constants.KECCAK_LANE;
import static io.github.destroyerofcode.common.Constants.KECCAK_SIDE;
import static io.github.destroyerofcode.common.Utils.MOD_5;
import static io.github.destroyerofcode.common.Utils.rol8;
import static io.github.destroyerofcode.keccak200output168.Constants.KECCAK_200_PI_LANE;
import static io.github.destroyerofcode.keccak200output168.Constants.KECCAK_200_ROTATION_CONSTANTS;
import static io.github.destroyerofcode.keccak200output168.Constants.KECCAK_200_ROUND_CONSTANTS;
import static io.github.destroyerofcode.keccak200output168.Constants.ROUNDS;

import io.github.destroyerofcode.api.SpongePermutation;

/**
 * Implements the SpongePermutation interface specifically for Keccak-200, applying the permutation
 * steps theta, rhoPi, chi, and iota to the given state.
 */
public class PermutationImpl implements SpongePermutation<byte[]> {

  /**
   * Applies the full permutation cycle to the state. The cycle includes the steps theta, rhoPi,
   * chi, and iota, repeated for a specified number of rounds.
   *
   * @param state The state array to be permuted.
   */
  @Override
  public void permute(final byte[] state) {
    for (int i = 0; i < ROUNDS; i++) {
      theta(state);
      rhoPi(state);
      chi(state);
      iota(state, i);
    }
  }

  /**
   * The theta step of the Keccak permutation. It XORs each bit of the state with the parity of two
   * columns in the state array.
   *
   * @param state The current state of the Keccak sponge.
   */
  @Override
  public void theta(final byte[] state) {
    final byte[] c = new byte[KECCAK_LANE];

    for (int i = 0; i < KECCAK_LANE; i++) {
      c[i] = (byte) (state[i] ^ state[5 + i] ^ state[10 + i] ^ state[15 + i] ^ state[20 + i]);
    }

    for (int i = 0; i < KECCAK_LANE; i++) {
      final byte temp = (byte) (c[MOD_5[i + 4]] ^ rol8(c[MOD_5[i + 1]], 1));
      for (int j = 0; j < KECCAK_SIDE; j += 5) {
        state[i + j] ^= temp;
      }
    }
  }

  /**
   * The rhoPi step of the Keccak permutation. It rotates and rearranges the lanes of the state.
   *
   * @param state The current state of the Keccak sponge.
   */
  @Override
  public void rhoPi(final byte[] state) {
    byte temp = state[1];
    byte c;

    for (int i = 0; i < KECCAK_SIDE - 1; i++) {
      c = state[KECCAK_200_PI_LANE[i]];
      state[KECCAK_200_PI_LANE[i]] = rol8(temp, KECCAK_200_ROTATION_CONSTANTS[i]);
      temp = c;
    }
  }

  /**
   * The chi step of the Keccak permutation. It combines bits from each row of the state using a
   * non-linear function.
   *
   * @param state The current state of the Keccak sponge.
   */
  @Override
  public void chi(final byte[] state) {
    final byte[] c = new byte[KECCAK_LANE];

    for (int i = 0; i < KECCAK_SIDE; i += KECCAK_LANE) {
      System.arraycopy(state, i, c, 0, KECCAK_LANE);

      for (int j = 0; j < KECCAK_LANE; j++) {
        state[i + j] = (byte) (c[j] ^ ((~c[MOD_5[j + 1]]) & c[MOD_5[j + 2]]));
      }
    }
  }

  /**
   * The iota step of the Keccak permutation. It modifies the state with a round constant.
   *
   * @param state The current state of the Keccak sponge.
   * @param round The current round number, used to determine the round constant.
   */
  @Override
  public void iota(final byte[] state, final int round) {
    state[0] ^= KECCAK_200_ROUND_CONSTANTS[round];
  }
}
