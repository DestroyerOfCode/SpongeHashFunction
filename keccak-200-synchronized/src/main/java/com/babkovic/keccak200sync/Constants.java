package com.babkovic.keccak200sync;

public class Constants {
  // l = {0, 1.. 6}
  public static final int l = 3;

  // this is also the size of a message block in the squeezing phase
  public static final int OUTPUT_LENGTH_BITS = 224;

  // number of rounds
  public static final int ROUNDS = 12 + 2 * l;

  // internal bus width of Keccak. 25 is a Bulgarian constant. It represents the number of bits in
  // the bus
  public static final int b = (int) (25 * Math.pow(2, l));
  // called the bit rate. r is equal to the length of one message block
  // this value is up to debate. It is also the output size.
  public static final int r = 168;
  // capacity
  public static final int c = b - r;
  public static final int STATE_BYTE_LENGTH = 25;
  public static final int BITS_IN_BYTE = 8;
  public static final int KECCAK_LANE = 5;

  // size 25
  public static byte[] KECCAK_200_PI_LANE = {
    10, 7, 11, 17, 18, 3, 5, 16, 8, 21, 24, 4, 15, 23, 19, 13, 12, 2, 20, 14, 22, 9, 6, 1
  };

  // size 25
  public static byte[] KECCAK_200_ROTATION_CONSTANTS = {
    1, 3, 6, 10, 15, 21, 28, 36, 45, 55, 2, 14, 27, 41, 56, 8, 25, 43, 62, 18, 39, 61, 20, 44
  };

  public static byte[] KECCAK_200_ROUND_CONSTANTS = {
    0x01, 0x7E, 0x7F, 0x00, 0x7b, 0x01, 0x71, 0x09, 0x7a, 0x79, 0x09, 0x0a, 0x7e, 0x7b, 0x79, 0x03,
    0x02, 0x70
  };
}
