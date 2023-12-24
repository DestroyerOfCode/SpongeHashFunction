package com.babkovic;

import com.babkovic.api.SpongeHash;
import com.babkovic.api.SpongePermutation;
import com.babkovic.exception.SpongeException;
import com.babkovic.keccak1600output256.PermutationImpl;
import com.babkovic.keccak1600output256.SpongeHashKeccak1600Impl;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Main {
  public static void main(String[] args) {
    final SpongePermutation<long[]> permutation = new PermutationImpl();
    final SpongeHash<long[]> impl = new SpongeHashKeccak1600Impl(permutation);
    try (InputStream file =
        new FileInputStream("src/main/resources/motherboard-internal-4k-k7.jpg")) {
      final long[] hash = impl.hash(file, file.available());

      System.out.println("Hash bytes: " + Arrays.toString(hash));
      System.out.println("Hash size: " + hash.length + " bytes");

    } catch (IOException e) {
      throw new SpongeException("Something went wrong hashing", e);
    }
  }
}
