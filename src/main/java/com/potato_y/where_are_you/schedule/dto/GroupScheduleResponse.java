package com.potato_y.where_are_you.schedule.dto;

import com.potato_y.where_are_you.schedule.domain.GroupSchedule;
import java.time.LocalDateTime;

public record GroupScheduleResponse(
    Long scheduleId,
    Long groupId,
    Long createUserId,
    String title,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Boolean isAlarmEnabled,
    int alarmBeforeHours,
    String location,
    double locationLatitude,
    double locationLongitude
) {

  public GroupScheduleResponse(GroupSchedule groupSchedule) {
    this(
        groupSchedule.getId(),
        groupSchedule.getGroup().getId(),
        groupSchedule.getUser().getId(),
        groupSchedule.getTitle(),
        groupSchedule.getStartTime(),
        groupSchedule.getEndTime(),
        groupSchedule.isAlarmEnabled(),
        groupSchedule.getAlarmBeforeHours(),
        groupSchedule.getLocation(),
        groupSchedule.getLocationLatitude(),
        groupSchedule.getLocationLongitude()
    );
  }
}
