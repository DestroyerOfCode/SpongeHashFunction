package com.babkovic;

import com.babkovic.api.SpongeHash;
import com.babkovic.api.SpongePermutation;
import com.babkovic.keccak200sync.PermutationImpl;
import com.babkovic.keccak200sync.SpongeHashKeccak200Impl;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Main {
  public static void main(String[] args) {
    SpongePermutation permutation = new PermutationImpl();
    SpongeHash impl = new SpongeHashKeccak200Impl(permutation);
    try {
      InputStream file =
          new FileInputStream("/home/marius/Downloads/motherboard-internal-4k-k7.jpg");
      System.out.println("Hash: " + Arrays.toString(impl.hash(file, file.available())));
    } catch (IOException e) {
      System.out.println("thrown exc");
    }
  }
}
