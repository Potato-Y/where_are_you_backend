package com.potato_y.where_are_you.common.utils;

import java.util.UUID;

public class CodeMaker {

  public static String createCode() {
    return UUID.randomUUID().toString();
  }
}
