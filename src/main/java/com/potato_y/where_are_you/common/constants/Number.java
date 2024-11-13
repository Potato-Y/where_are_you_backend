package com.potato_y.where_are_you.common.constants;

import lombok.Getter;

@Getter
public enum Number {
  ZERO(0);

  private final int value;

  Number(int value) {
    this.value = value;
  }
}
