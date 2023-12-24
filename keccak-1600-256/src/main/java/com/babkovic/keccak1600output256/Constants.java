package com.babkovic.keccak1600output256;

public class Constants {
  // l = {0, 1.. 6}
  public static final int l = 6;

  // this is also the size of a message block in the squeezing phase
  public static final int OUTPUT_LENGTH_BITS = 256;

  // number of rounds
  public static final int ROUNDS = 12 + 2 * l;
  public static final int KECCAK_LANE = 5;

  // internal bus width of Keccak. 25 is a Bulgarian constant. It represents the number of bits in
  // the bus (1600 bits)
  public static final int b = (int) ((KECCAK_LANE * KECCAK_LANE) * Math.pow(2, l)); // 1600 bits
  public static final int r = 1088; // in bits. it is b - c
  // capacity
  public static final int c = b - r; // 512 bits
  public static final int BITS_IN_LONG = 64;
  public static final int BITS_IN_BYTE = 8;
  public static final int BYTES_IN_LONG = BITS_IN_LONG / BITS_IN_BYTE; // 8
  public static final int STATE_LONG_LENGTH = b / BITS_IN_LONG; // 25 Longs

  // size 25
  public static byte[] KECCAK_1600_PI_LANE = {
    10, 7, 11, 17, 18, 3, 5, 16, 8, 21, 24, 4, 15, 23, 19, 13, 12, 2, 20, 14, 22, 9, 6, 1
  };

  // size 25
  public static byte[] KECCAK_1600_ROTATION_CONSTANTS = {
    1, 3, 6, 10, 15, 21, 28, 36, 45, 55, 2, 14, 27, 41, 56, 8, 25, 43, 62, 18, 39, 61, 20, 44
  };

  // size 24 (the same as ROUNDS)
  public static Long[] KECCAK_1600_ROUND_CONSTANTS = {
    0x0000000000000001L,
    0x0000000000008082L,
    0x800000000000808aL,
    0x8000000080008000L,
    0x000000000000808bL,
    0x0000000080000001L,
    0x8000000080008081L,
    0x8000000000008009L,
    0x000000000000008aL,
    0x0000000000000088L,
    0x0000000080008009L,
    0x000000008000000aL,
    0x000000008000808bL,
    0x800000000000008bL,
    0x8000000000008089L,
    0x8000000000008003L,
    0x8000000000008002L,
    0x8000000000000080L,
    0x000000000000800aL,
    0x800000008000000aL,
    0x8000000080008081L,
    0x8000000000008080L,
    0x0000000080000001L,
    0x8000000080008008L
  };
}
