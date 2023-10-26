package com.babkovic.keccak200sync;

import static com.babkovic.common.Utils.MOD_5;
import static com.babkovic.common.Utils.rol8;

import com.babkovic.api.SpongePermutation;

public class PermutationImpl implements SpongePermutation {
    @Override
    public byte[] permute(byte[] state) {

        theta(state);
        rho(state);
        pi(state);
        chi(state);
        iota(state);

        return state;
    }

    @Override
    public byte[] theta(byte[] state) {
        final byte[] c = new byte[5];

        for(int i = 0; i < 5; i++) {
            c[i] = (byte) (state[i] ^ state[5 + i] ^ state[10 + i] ^ state[15 + i] ^ state[20 + i]);
        }

        for(int i = 0; i < 5; i++) {
            final byte temp = (byte) (c[MOD_5[i+4]] ^ rol8(c[MOD_5[i+1]], 1));
            for(int j = 0; j < 25; j += 5) {
                state[i + j] ^= temp;
            }
        }
        return state;
    }

    @Override
    public byte[] rho(byte[] state) {
        byte temp = state[1];

        return state;
    }

    @Override
    public byte[] pi(byte[] state) {
        return state;
    }

    @Override
    public byte[] chi(byte[] state) {
        return state;
    }

    @Override
    public byte[] iota(byte[] state) {
        return state;
    }
}
