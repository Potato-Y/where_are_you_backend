package com.potato_y.where_are_you.firebase.domain;

import lombok.Getter;

@Getter
public enum FcmChannelId {
  CREATE_SCHEDULE("CREATE_SCHEDULE");

  private final String value;

  FcmChannelId(String value) {
    this.value = value;
  }
}
