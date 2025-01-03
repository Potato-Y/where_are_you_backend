package com.potato_y.where_are_you.schedule;

import static com.potato_y.where_are_you.common.utils.DateTimeUtils.clearSecondAndNano;

import com.potato_y.where_are_you.authentication.CurrentUserProvider;
import com.potato_y.where_are_you.error.exception.ForbiddenException;
import com.potato_y.where_are_you.error.exception.NotFoundException;
import com.potato_y.where_are_you.firebase.FirebaseService;
import com.potato_y.where_are_you.firebase.domain.FcmChannelId;
import com.potato_y.where_are_you.group.GroupService;
import com.potato_y.where_are_you.group.GroupValidator;
import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.group.domain.GroupMember;
import com.potato_y.where_are_you.schedule.domain.AlarmSchedule;
import com.potato_y.where_are_you.schedule.domain.AlarmScheduleRepository;
import com.potato_y.where_are_you.schedule.domain.GroupSchedule;
import com.potato_y.where_are_you.schedule.domain.GroupScheduleRepository;
import com.potato_y.where_are_you.schedule.domain.Participation;
import com.potato_y.where_are_you.schedule.domain.ParticipationRepository;
import com.potato_y.where_are_you.schedule.dto.CreateGroupScheduleRequest;
import com.potato_y.where_are_you.schedule.dto.GetGroupScheduleListResponse;
import com.potato_y.where_are_you.schedule.dto.GroupScheduleResponse;
import com.potato_y.where_are_you.user.domain.User;
import com.potato_y.where_are_you.user.dto.UserResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupScheduleService {

  private final GroupService groupService;
  private final CurrentUserProvider currentUserProvider;
  private final GroupScheduleRepository scheduleRepository;
  private final AlarmScheduleRepository alarmScheduleRepository;
  private final ParticipationRepository participationRepository;
  private final FirebaseService firebaseService;
  private final GroupValidator groupValidator;
  private final ScheduleValidator scheduleValidator;

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
        .startTime(clearSecondAndNano(dto.startTime()))
        .endTime(clearSecondAndNano(dto.endTime()))
        .isAlarmEnabled(dto.isAlarmEnabled())
        .alarmBeforeHours(dto.alarmBeforeHours())
        .location(dto.location())
        .locationLatitude(dto.locationLatitude())
        .locationLongitude(dto.locationLongitude())
        .build());

    if (groupSchedule.isAlarmEnabled()) {
      createAlarmSchedule(groupSchedule);
    }

    pushNewSchedule(groupSchedule);

    return new GroupScheduleResponse(groupSchedule);
  }

  private AlarmSchedule createAlarmSchedule(GroupSchedule schedule) {
    LocalDateTime alarmTime = schedule.getStartTime().minusHours(schedule.getAlarmBeforeHours());

    return alarmScheduleRepository.save(AlarmSchedule.builder()
        .schedule(schedule)
        .dateTime(alarmTime)
        .build());
  }

  @Transactional(readOnly = true)
  public List<GetGroupScheduleListResponse> getGroupSchedules(Long groupId) {
    User user = currentUserProvider.getCurrentUser();
    if (!groupService.checkGroupMember(groupId, user)) {
      throw new ForbiddenException("사용자가 그룹원이 아닙니다");
    }

    Group group = groupService.findByGroup(groupId);
    List<GroupSchedule> schedules = scheduleRepository.findByGroup(group);

    return schedules.stream().map(it -> {
      boolean isParticipating = false;
      Optional<Participation> participation = participationRepository.findByUserAndSchedule(user,
          it);
      if (participation.isPresent() && participation.get().isParticipating()) {
        isParticipating = true;
      }

      return new GetGroupScheduleListResponse(it, isParticipating);
    }).toList();
  }

  @Transactional
  public void registerParticipation(Long groupId, Long scheduleId) {
    User user = currentUserProvider.getCurrentUser();
    GroupSchedule schedule = getGroupSchedule(scheduleId);

    groupValidator.groupId(schedule.getGroup(), groupId);
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

    groupValidator.groupId(schedule.getGroup(), groupId);
    if (!groupService.checkGroupMember(groupId, user)) {
      throw new ForbiddenException("사용자가 그룹원이 아닙니다");
    }

    Participation participation = participationRepository.findByUserAndSchedule(user, schedule)
        .orElseThrow(() -> new NotFoundException("참여 정보를 불러올 수 없습니다"));

    participation.updateIsParticipating(false);
  }

  @Transactional(readOnly = true)
  public List<UserResponse> getParticipationList(Long groupId, Long scheduleId) {
    User user = currentUserProvider.getCurrentUser();
    GroupSchedule schedule = getGroupSchedule(scheduleId);

    groupValidator.groupId(schedule.getGroup(), groupId);
    if (!groupService.checkGroupMember(groupId, user)) {
      throw new ForbiddenException("사용자가 그룹원이 아닙니다");
    }

    List<Participation> participations = participationRepository.findBySchedule(schedule);

    return participations.stream().filter(Participation::isParticipating)
        .map((it) -> new UserResponse(it.getUser())).toList();
  }

  @Transactional(readOnly = true)
  public List<User> getParticipationUsers(GroupSchedule schedule) {
    return participationRepository.findBySchedule(schedule).stream()
        .filter(Participation::isParticipating).map(Participation::getUser).toList();
  }

  @Transactional(readOnly = true)
  public GroupSchedule getSchedule(Long scheduleId) {
    return scheduleRepository.findById(scheduleId)
        .orElseThrow(() -> new NotFoundException("스케줄을 찾을 수 없음"));
  }

  @Transactional(readOnly = true)
  public boolean checkParticipation(User user, GroupSchedule schedule) {
    Optional<Participation> participation = participationRepository
        .findByUserAndSchedule(user, schedule);

    return participation.isPresent() && participation.get().isParticipating();
  }

  @Transactional
  public void deleteGroupSchedule(Long groupId, Long scheduleId) {
    User user = currentUserProvider.getCurrentUser();
    GroupSchedule schedule = getSchedule(scheduleId);

    if (!groupService.checkGroupMember(groupId, user)) {
      throw new ForbiddenException("그룹 멤버가 아닙니다");
    }
    scheduleValidator.scheduleOwner(schedule, user);

    scheduleRepository.delete(schedule);
  }

  @Transactional
  public GroupScheduleResponse modifyGroupSchedule(Long groupId, Long scheduleId,
      CreateGroupScheduleRequest request) {
    User user = currentUserProvider.getCurrentUser();
    GroupSchedule schedule = getSchedule(scheduleId);

    if (!groupService.checkGroupMember(groupId, user)) {
      throw new ForbiddenException("그룹 멤버가 아닙니다");
    }
    scheduleValidator.scheduleOwner(schedule, user);

    schedule
        .updateTitle(request.title())
        .updateStartTime(request.startTime())
        .updateEndTime(request.endTime())
        .updateIsAlarmEnabled(request.isAlarmEnabled())
        .updateAlarmBeforeHours(request.alarmBeforeHours())
        .updateLocation(request.location())
        .updateLocationLatitude(request.locationLatitude())
        .updateLocationLongitude(request.locationLongitude());

    Optional<AlarmSchedule> maybeAlarmSchedule = alarmScheduleRepository.findBySchedule(schedule);
    if (request.isAlarmEnabled()) {
      maybeAlarmSchedule.ifPresentOrElse(
          it -> it.updateDateTime(request.startTime().minusHours(request.alarmBeforeHours())),
          () -> createAlarmSchedule(schedule));
    } else {
      maybeAlarmSchedule.ifPresent(alarmScheduleRepository::delete);
    }

    return new GroupScheduleResponse(schedule);
  }

  @Transactional(readOnly = true)
  void pushScheduleAlarm(GroupSchedule schedule) {
    List<User> groupMembers = groupService.getGroupMembers(schedule.getGroup())
        .stream().map(GroupMember::getUser).toList();

    firebaseService.pushFcmNotificationForSchedule(groupMembers, schedule,
        FcmChannelId.SCHEDULE_BEFORE_ALARM);
  }

  private void pushNewSchedule(GroupSchedule schedule) {
    List<User> groupMembers = groupService.getGroupMembers(schedule.getGroup())
        .stream().map(GroupMember::getUser).toList();

    firebaseService.pushFcmNotificationForSchedule(groupMembers, schedule,
        FcmChannelId.SCHEDULE_CREATE);
  }
}
