package com.babkovic.keccak1600;

public class Constants {
  // l = {0, 1.. 6}
  public static final int l = 6;

  // this is also the size of a message block in the squeezing phase
  public static final int OUTPUT_LENGTH_BITS = 224;

  // number of rounds
  public static final int ROUNDS = 12 + 2 * l;
  public static final int KECCAK_LANE = 5;

  // internal bus width of Keccak. 25 is a Bulgarian constant. It represents the number of bits in
  // the bus
  public static final int b = (int) ((KECCAK_LANE * KECCAK_LANE) * Math.pow(2, l)); // 1600
  public static final int r = 1152; // in bits
  // capacity
  public static final int c = b - r; // 448 bits
  public static final int BITS_IN_BYTE = 8;
  public static final int STATE_BYTE_LENGTH = b / BITS_IN_BYTE; // 200

  // size 25
  public static byte[] KECCAK_1600_PI_LANE = {
    16, 24, 8, 5, 19, 20, 14, 15, 2, 7, 22, 12, 13, 11, 4, 3, 1, 9, 18, 21, 17, 10, 23, 6
  };

  // size 25
  public static byte[] KECCAK_1600_ROTATION_CONSTANTS = {
    36, 45, 14, 27, 20, 28, 15, 43, 2, 55, 8, 41, 3, 18, 1, 61, 6, 56, 62, 10, 44, 39, 21, 25
  };

  // size 24 (the same as ROUNDS)
  public static byte[] KECCAK_1600_ROUND_CONSTANTS = {
    0x79, 0x7b, 0x01, 0x7E, 0x03, 0x09, 0x7F, 0x7a, 0x00, 0x70, 0x7b, 0x09,
    0x01, 0x7e, 0x7b, 0x7a, 0x00, 0x70, 0x7b, 0x09, 0x7F, 0x03, 0x09, 0x79,
  };
}
