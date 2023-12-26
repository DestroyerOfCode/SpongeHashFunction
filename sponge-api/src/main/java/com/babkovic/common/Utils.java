package com.babkovic.common;

public class Utils {
  /**
   * Performs a left bit rotation on a byte.
   *
   * @param a The byte to be rotated.
   * @param offset The number of bits to rotate.
   * @return The rotated byte.
   */
  public static byte rol8(byte a, int offset) {
    return (byte) (((a << (offset & 7)) ^ (a >> (8 - (offset & 7)))) & 0xFF);
  }

  /**
   * Performs a left bit rotation on a long.
   *
   * @param a The long to be rotated.
   * @param offset The number of bits to rotate.
   * @return The rotated long.
   */
  public static long rol64(long a, int offset) {
    return (offset != 0) ? ((a << offset) ^ (a >>> (64 - offset))) : a;
  }

  /**
   * Finds the smallest multiple of a number that is greater than or equal to a specified array
   * size. Useful for padding or aligning data to a specific boundary.
   *
   * @param arraySize The size of the array or data segment.
   * @param number The base number to find a multiple of.
   * @return The nearest greater multiple of the given number.
   * @throws IllegalArgumentException If the provided number is less than or equal to 0.
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

  public static final byte[] MOD_5 = {0, 1, 2, 3, 4, 0, 1, 2, 3, 4};
}
