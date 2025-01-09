package com.potato_y.timely.schedule.dto;

import com.potato_y.timely.schedule.domain.GroupSchedule;

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
