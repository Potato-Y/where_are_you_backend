package com.potato_y.where_are_you.user.dto;

import com.potato_y.where_are_you.user.domain.UserLate;

public record UserLateResponse(
    UserResponse user,
    LateDataResponse lateData
) {

  public UserLateResponse(UserLate userLate) {
    this(
        new UserResponse(userLate.getUser()),
        new LateDataResponse(userLate)
    );
  }

  public record LateDataResponse(
      Long participation,
      Long late
  ) {

    public LateDataResponse(UserLate userLate) {
      this(
          userLate.getParticipationCount(),
          userLate.getLateCount()
      );
    }
  }
}
