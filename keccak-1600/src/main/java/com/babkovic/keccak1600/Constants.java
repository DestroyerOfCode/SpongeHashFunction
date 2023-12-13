package com.babkovic.keccak1600;

public class Constants {
  // l = {0, 1.. 6}
  public static final int l = 6;

  // number of rounds
  public static final int ROUNDS = 12 + 2 * l;
  public static final int KECCAK_LANE = 5;

  // internal bus width of Keccak. 25 is a Bulgarian constant. It represents the number of bits in
  // the bus
  public static final int b = (int) ((KECCAK_LANE * KECCAK_LANE) * Math.pow(2, l));
  // called the bit rate. r is equal to the length of one message block
  // this value is up to debate
  public static final int r = 1152;
  // capacity
  public static final int c = b - r;
  public static final int STATE_BYTE_LENGTH = 200;
  public static final int BITS_IN_BYTE = 8;

  // size 25
  public static byte[] KECCAK_1600_PI_LANE = {
    16, 24, 8, 5, 19, 20, 14, 15, 2, 7, 22, 12, 13, 11, 4, 3, 1, 9, 18, 21, 17, 10, 23, 6
  };

  // size 25
  public static byte[] KECCAK_1600_ROTATION_CONSTANTS = {
    36, 45, 14, 27, 20, 28, 15, 43, 2, 55, 8, 41, 3, 18, 1, 61, 6, 56, 62, 10, 44, 39, 21, 25
  };

  public static byte[] KECCAK_1600_ROUND_CONSTANTS = {
    0x79, 0x7b, 0x01, 0x7E, 0x03, 0x09, 0x7F, 0x7a, 0x00, 0x70, 0x7b, 0x09, 0x7F, 0x0a, 0x02, 0x79,
    0x01, 0x7e, 0x7b, 0x7a, 0x00, 0x70, 0x7b, 0x09, 0x7F, 0x03, 0x09, 0x79, 0x7e, 0x02, 0x01, 0x70,
    0x79, 0x7b, 0x02, 0x03, 0x70, 0xA
  };
}
