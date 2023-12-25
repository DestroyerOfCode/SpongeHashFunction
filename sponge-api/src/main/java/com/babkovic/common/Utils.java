package com.babkovic.common;

public class Utils {
  /**
   * @param a the byte to be rotated
   * @param offset the number of bits to rotate it
   * @return the rotated byte.
   */
  public static byte rol8(byte a, int offset) {
    return (byte) (((a << (offset & 7)) ^ (a >> (8 - (offset & 7)))) & 0xFF);
  }

  public static long rol64(long a, int offset) {
    return (offset != 0) ? ((a << offset) ^ (a >>> (64 - offset))) : a;
  }

  public static byte[] MOD_5 = {0, 1, 2, 3, 4, 0, 1, 2, 3, 4};

  /**
   * Calculates the nearest multiple of a number that is greater than the array size.
   *
   * @param arraySize The size of the array.
   * @param number The number for which the nearest multiple is to be found.
   * @return The nearest multiple of the number that is greater than the array size.
   */
  public static int nearestGreaterMultiple(int arraySize, int number) {
    if (number <= 0) {
      throw new IllegalArgumentException("Number must be greater than 0.");
    }

    int multiple = (arraySize / number) * number;
    if (multiple < arraySize) {
      multiple += number;
    }
    return multiple;
  }
}
