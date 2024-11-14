package com.potato_y.where_are_you.schedule;

import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.schedule.domain.GroupSchedule;
import com.potato_y.where_are_you.schedule.domain.Participation;
import com.potato_y.where_are_you.user.domain.User;
import java.time.LocalDateTime;

public class GroupScheduleUtils {

  /**
   * 일반적인 정상 케이스 1 <br> 일정 시작 시각: 25분 뒤 <br> 일정 종료 시각: 1시간 뒤 <br> 알람 활성화: true <br> 알람 시간: 1시간 <br>
   */
  public static GroupSchedule createScheduleCase1(Group group, User user) {
    return GroupSchedule.builder()
        .group(group)
        .user(user)
        .title("test group")
        .startTime(LocalDateTime.now().plusMinutes(25))
        .endTime(LocalDateTime.now().plusHours(1))
        .isAlarmEnabled(true)
        .alarmBeforeHours(1)
        .location("테스트 장소")
        .locationLatitude(123.456)
        .locationLongitude(234.567)
        .build();
  }

  /**
   * 공유 시간이 많이 남은 케이스 <br> 일정 시작 시각: 25일 뒤 <br> 일정 종료 시각: 26일 뒤 <br> 알람 활성화: true <br> 알람 시간: 1시간
   * <br>
   */
  public static GroupSchedule createScheduleCase2(Group group, User user) {
    return GroupSchedule.builder()
        .group(group)
        .user(user)
        .title("test group")
        .startTime(LocalDateTime.now().plusDays(25))
        .endTime(LocalDateTime.now().plusDays(26))
        .isAlarmEnabled(true)
        .alarmBeforeHours(1)
        .location("테스트 장소")
        .locationLatitude(123.456)
        .locationLongitude(234.567)
        .build();
  }

  public static Participation createParticipation(GroupSchedule schedule, User user,
      boolean isParticipating) {
    return Participation.builder()
        .schedule(schedule)
        .user(user)
        .isParticipating(isParticipating)
        .build();
  }
}
