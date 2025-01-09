package com.potato_y.where_are_you.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CreateGroupScheduleRequest(
    @NotBlank @Size(max = 100) String title,
    @NotNull LocalDateTime startTime,
    @NotNull LocalDateTime endTime,
    @NotNull Boolean isAlarmEnabled,
    int alarmBeforeHours,
    String location,
    double locationLatitude,
    double locationLongitude
) {

}
