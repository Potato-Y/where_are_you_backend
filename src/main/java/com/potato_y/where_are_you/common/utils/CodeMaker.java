package com.potato_y.where_are_you.common.utils;

import java.util.Random;

public class CodeMaker {

  private static final String FULL_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

  public static String createCode(int length) {
    Random random = new Random();

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      int index = random.nextInt(FULL_CHARACTERS.length());
      sb.append(FULL_CHARACTERS.charAt(index));
    }

    return sb.toString();
  }
}
