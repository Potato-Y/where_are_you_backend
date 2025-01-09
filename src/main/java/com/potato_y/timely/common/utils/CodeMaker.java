package com.potato_y.timely.common.utils;

import java.util.UUID;

public class CodeMaker {

  public static String createCode() {
    return UUID.randomUUID().toString();
  }
}
