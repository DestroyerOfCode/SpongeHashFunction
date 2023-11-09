package com.babkovic;

import com.babkovic.api.SpongeHash;
import com.babkovic.api.SpongePermutation;
import com.babkovic.exception.SpongeException;
import com.babkovic.keccak200sync.PermutationImpl;
import com.babkovic.keccak200sync.SpongeHashKeccak200Impl;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HexFormat;

public class Main {
  public static void main(String[] args) {
    SpongePermutation permutation = new PermutationImpl();
    SpongeHash impl = new SpongeHashKeccak200Impl(permutation);
    try (InputStream file =
        new FileInputStream("src/main/resources/motherboard-internal-4k-k7.jpg")) {
      final byte[] hash = impl.hash(file, file.available());

      System.out.println("Hash string: " + HexFormat.of().formatHex(hash));
      System.out.println("Hash bytes: " + Arrays.toString(hash));
      System.out.println("Hash size: " + hash.length + " bytes");

    } catch (IOException e) {
      throw new SpongeException("Something went wrong hashing", e);
    }
  }
}
