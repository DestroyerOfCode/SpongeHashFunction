package com.babkovic.api;

public interface SpongePermutation extends Permutation {
    byte[] theta(final byte[] state);
    byte[] rho(final byte[] state);
    byte[] pi(final byte[] state);
    byte[] chi(final byte[] state);
    byte[] iota(final byte[] state);
}

