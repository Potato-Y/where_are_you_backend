package com.potato_y.where_are_you.schedule;

import com.potato_y.where_are_you.authentication.CurrentUserProvider;
import com.potato_y.where_are_you.error.exception.ForbiddenException;
import com.potato_y.where_are_you.group.GroupService;
import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.schedule.domain.GroupSchedule;
import com.potato_y.where_are_you.schedule.domain.GroupScheduleRepository;
import com.potato_y.where_are_you.schedule.dto.CreateGroupScheduleRequest;
import com.potato_y.where_are_you.schedule.dto.GroupScheduleResponse;
import com.potato_y.where_are_you.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupScheduleService {

  private final GroupService groupService;
  private final CurrentUserProvider currentUserProvider;
  private final GroupScheduleRepository scheduleRepository;

  @Transactional
  public GroupScheduleResponse createSchedule(Long groupId, CreateGroupScheduleRequest dto) {
    User user = currentUserProvider.getCurrentUser();
    Group group = groupService.findByGroup(groupId);

    // 사용자가 그룹 멤버인지 확인
    if (!groupService.checkGroupMember(groupId, user)) {
      throw new ForbiddenException("사용자가 그룹원이 아닙니다");
    }

    GroupSchedule groupSchedule = scheduleRepository.save(GroupSchedule.builder()
        .group(group)
        .user(user)
        .title(dto.title())
        .startTime(dto.startTime())
        .endTime(dto.endTime())
        .isAlarmEnabled(dto.isAlarmEnabled())
        .alarmBeforeHours(dto.alarmBeforeHours())
        .location(dto.location())
        .locationLatitude(dto.locationLatitude())
        .locationLongitude(dto.locationLongitude())
        .build());

    return new GroupScheduleResponse(groupSchedule);
  }
}
