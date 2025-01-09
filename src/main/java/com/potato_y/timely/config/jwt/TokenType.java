package com.potato_y.where_are_you.config.jwt;

import lombok.Getter;

@Getter
public enum TokenType {
  ACCESS("ACCESS"), REFRESH("REFRESH");

  private final String type;

  TokenType(String type) {
    this.type = type;
  }
}
