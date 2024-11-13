package com.potato_y.where_are_you.schedule;

import com.potato_y.where_are_you.common.constants.Number;
import com.potato_y.where_are_you.schedule.domain.AlarmSchedule;
import com.potato_y.where_are_you.schedule.domain.AlarmScheduleRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlarmSchedulerConfiguration {

  private final GroupScheduleService groupScheduleService;
  private final AlarmScheduleRepository alarmScheduleRepository;

  @Scheduled(fixedDelay = 60000)
  @Transactional(readOnly = true)
  public void run() {
    LocalDateTime dateTime = LocalDateTime.now().withSecond(Number.ZERO.getValue())
        .withNano(Number.ZERO.getValue());
    List<AlarmSchedule> schedules = alarmScheduleRepository.findByDateTime(dateTime);

    schedules.forEach(it -> groupScheduleService.pushScheduleAlarm(it.getSchedule()));
  }
}
