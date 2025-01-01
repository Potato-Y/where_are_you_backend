package com.potato_y.where_are_you.firebase.domain;

import lombok.Getter;

@Getter
public enum FcmChannelId {
  SCHEDULE_CREATE("SCHEDULE_CREATE"), SCHEDULE_BEFORE_ALARM("SCHEDULE_BEFORE_ALARM");

  private final String value;

  FcmChannelId(String value) {
    this.value = value;
  }
}
