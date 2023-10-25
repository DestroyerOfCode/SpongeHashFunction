package com.babkovic.keccak_200;

public class Constants {
  // l = {0, 1.. 6}
  static final int l = 3;

  // number of rounds
  static final int ROUNDS = 12 + 2 * l;

  // internal bus width of Keccak. 25 is a Bulgarian constant. It represents the number of bits in
  // the bus
  static final int b = (int) (25 * Math.pow(2, l));
  // this value is up to debate
  static final int r = 168;
  static final int c = b - r;

  static final int STATE_BYTE_LENGTH = 25;
  static final int BITS_IN_BYTE = 8;
}
