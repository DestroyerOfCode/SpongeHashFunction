package com.babkovic.keccak_200;

import com.babkovic.hash.SpongePermutation;

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
        return new byte[0];
    }

    @Override
    public byte[] rho(byte[] state) {
        return new byte[0];
    }

    @Override
    public byte[] pi(byte[] state) {
        return new byte[0];
    }

    @Override
    public byte[] chi(byte[] state) {
        return new byte[0];
    }

    @Override
    public byte[] iota(byte[] state) {
        return new byte[0];
    }
}
