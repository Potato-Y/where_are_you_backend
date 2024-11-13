package com.potato_y.where_are_you.firebase.domain;

import lombok.Getter;

@Getter
public enum FcmChannelId {
  CREATE_SCHEDULE("CREATE_SCHEDULE"), ALARM_SCHEDULE("APPOINTMENT_ALARM");

  private final String value;

  FcmChannelId(String value) {
    this.value = value;
  }
}
