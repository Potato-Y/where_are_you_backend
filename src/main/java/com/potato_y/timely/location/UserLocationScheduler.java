package com.potato_y.where_are_you.location;

import static com.potato_y.where_are_you.common.utils.DateTimeUtils.clearSecondAndNano;

import com.potato_y.where_are_you.schedule.domain.GroupSchedule;
import com.potato_y.where_are_you.schedule.domain.GroupScheduleRepository;
import com.potato_y.where_are_you.schedule.domain.Participation;
import com.potato_y.where_are_you.schedule.domain.ParticipationRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserLocationScheduler {

  private final LocationShareService locationShareService;
  private final GroupScheduleRepository groupScheduleRepository;
  private final ParticipationRepository participationRepository;

  @Scheduled(fixedDelay = 60000)
  @Transactional
  public void deleteUserLocation() {
    LocalDateTime dateTime = clearSecondAndNano(LocalDateTime.now());

    List<GroupSchedule> schedules = groupScheduleRepository.findByStartTime(
        dateTime); // 해당 시간에 시작하는 스케줄 찾기
    List<Participation> participations = new ArrayList<>(); // 참여자 목록 가져오기

    schedules.forEach(it -> participations.addAll(participationRepository.findBySchedule(it)));

    participations.forEach(it -> locationShareService.resetUserLocation(it.getUser()));
  }
}
