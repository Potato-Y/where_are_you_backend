package com.potato_y.where_are_you.schedule;

import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.schedule.domain.GroupSchedule;
import com.potato_y.where_are_you.user.domain.User;
import java.time.LocalDateTime;

public class GroupScheduleFactory {

  private Group group;
  private User user;
  private String title = "test group";
  private LocalDateTime startTime = LocalDateTime.now().plusMinutes(25).withSecond(0).withNano(0);
  private LocalDateTime endTime = LocalDateTime.now().plusHours(1).withSecond(0).withNano(0);
  private boolean isAlarmEnabled = true;
  private int alarmBeforeHours = 1;
  private String location = "테스트 장소";
  private double locationLatitude = 123.456;
  private double locationLongitude = 234.567;

  public GroupScheduleFactory group(Group group) {
    this.group = group;
    return this;
  }

  public GroupScheduleFactory user(User user) {
    this.user = user;
    return this;
  }

  public GroupScheduleFactory title(String title) {
    this.title = title;
    return this;
  }

  public GroupScheduleFactory startTime(LocalDateTime startTime) {
    this.startTime = startTime;
    return this;
  }

  public GroupScheduleFactory endTime(LocalDateTime endTime) {
    this.endTime = endTime;
    return this;
  }

  public GroupScheduleFactory isAlarmEnabled(boolean isAlarmEnabled) {
    this.isAlarmEnabled = isAlarmEnabled;
    return this;
  }

  public GroupScheduleFactory alarmBeforeHours(int alarmBeforeHours) {
    this.alarmBeforeHours = alarmBeforeHours;
    return this;
  }

  public GroupScheduleFactory location(String location) {
    this.location = location;
    return this;
  }

  public GroupScheduleFactory locationLatitude(double locationLatitude) {
    this.locationLatitude = locationLatitude;
    return this;
  }

  public GroupScheduleFactory locationLongitude(double locationLongitude) {
    this.locationLongitude = locationLongitude;
    return this;
  }

  public static GroupScheduleFactory builder() {
    return new GroupScheduleFactory();
  }

  public GroupSchedule build() {
    return GroupSchedule.builder()
        .group(group)
        .user(user)
        .title(title)
        .startTime(startTime)
        .endTime(endTime)
        .isAlarmEnabled(isAlarmEnabled)
        .alarmBeforeHours(alarmBeforeHours)
        .location(location)
        .locationLatitude(locationLatitude)
        .locationLongitude(locationLongitude)
        .build();
  }
}
