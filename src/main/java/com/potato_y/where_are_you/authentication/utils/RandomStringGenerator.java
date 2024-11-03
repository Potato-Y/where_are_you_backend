package com.potato_y.where_are_you.authentication.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class RandomStringGenerator {

  private static final SecureRandom secureRandom = new SecureRandom();
  private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

  public static String generateSecureRandomString(int byteLength) {
    byte[] randomBytes = new byte[byteLength];
    secureRandom.nextBytes(randomBytes);

    return base64Encoder.encodeToString(randomBytes);
  }
}
