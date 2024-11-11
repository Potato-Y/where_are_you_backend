package com.potato_y.where_are_you.schedule;

import static com.potato_y.where_are_you.group.GroupValidator.validateGroupId;

import com.potato_y.where_are_you.authentication.CurrentUserProvider;
import com.potato_y.where_are_you.error.exception.ForbiddenException;
import com.potato_y.where_are_you.error.exception.NotFoundException;
import com.potato_y.where_are_you.group.GroupService;
import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.schedule.domain.GroupSchedule;
import com.potato_y.where_are_you.schedule.domain.GroupScheduleRepository;
import com.potato_y.where_are_you.schedule.domain.Participation;
import com.potato_y.where_are_you.schedule.domain.ParticipationRepository;
import com.potato_y.where_are_you.schedule.dto.CreateGroupScheduleRequest;
import com.potato_y.where_are_you.schedule.dto.GroupScheduleResponse;
import com.potato_y.where_are_you.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupScheduleService {

  private final GroupService groupService;
  private final CurrentUserProvider currentUserProvider;
  private final GroupScheduleRepository scheduleRepository;
  private final ParticipationRepository participationRepository;

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

  @Transactional(readOnly = true)
  public List<GroupScheduleResponse> getSchedules(Long groupId) {
    User user = currentUserProvider.getCurrentUser();
    if (!groupService.checkGroupMember(groupId, user)) {
      throw new ForbiddenException("사용자가 그룹원이 아닙니다");
    }

    Group group = groupService.findByGroup(groupId);
    List<GroupSchedule> schedules = scheduleRepository.findByGroup(group);

    return schedules.stream().map(GroupScheduleResponse::new).toList();
  }

  @Transactional
  public void registerParticipation(Long groupId, Long scheduleId) {
    User user = currentUserProvider.getCurrentUser();
    GroupSchedule schedule = getGroupSchedule(scheduleId);

    validateGroupId(schedule.getGroup(), groupId);
    if (!groupService.checkGroupMember(groupId, user)) {
      throw new ForbiddenException("사용자가 그룹원이 아닙니다");
    }

    Participation participation = participationRepository.findByUserAndSchedule(user, schedule)
        .orElseGet(() -> createParticipation(user, schedule));

    if (!participation.isParticipating()) {
      participation.updateIsParticipating(true);
    }
  }

  private Participation createParticipation(User user, GroupSchedule schedule) {
    return participationRepository.save(Participation.builder()
        .user(user)
        .schedule(schedule)
        .isParticipating(true)
        .build());
  }

  private GroupSchedule getGroupSchedule(Long id) {
    return scheduleRepository.findById(id).orElseThrow(() -> new NotFoundException("일정이 없습니다."));
  }

  @Transactional
  public void cancelParticipation(Long groupId, Long scheduleId) {
    User user = currentUserProvider.getCurrentUser();
    GroupSchedule schedule = getGroupSchedule(scheduleId);

    validateGroupId(schedule.getGroup(), groupId);
    if (!groupService.checkGroupMember(groupId, user)) {
      throw new ForbiddenException("사용자가 그룹원이 아닙니다");
    }

    Participation participation = participationRepository.findByUserAndSchedule(user, schedule)
        .orElseThrow(() -> new NotFoundException("참여 정보를 불러올 수 없습니다"));

    participation.updateIsParticipating(false);
  }
}
