package com.potato_y.where_are_you.schedule.dto;

import com.potato_y.where_are_you.schedule.domain.GroupSchedule;

public record GetGroupScheduleListResponse(
    GroupScheduleResponse groupSchedule,
    boolean isParticipating
) {

  public GetGroupScheduleListResponse(GroupSchedule groupSchedule, boolean isParticipating) {
    this(
        new GroupScheduleResponse(groupSchedule),
        isParticipating
    );
  }
}
