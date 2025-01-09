package com.potato_y.timely.config.jwt;

import lombok.Getter;

@Getter
public enum TokenType {
  ACCESS("ACCESS"), REFRESH("REFRESH");

  private final String type;

  TokenType(String type) {
    this.type = type;
  }
}
