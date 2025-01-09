package com.potato_y.timely.schedule;

import static com.potato_y.timely.common.utils.DateTimeUtils.clearSecondAndNano;

import com.potato_y.timely.schedule.domain.AlarmSchedule;
import com.potato_y.timely.schedule.domain.AlarmScheduleRepository;
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
public class GroupScheduleScheduler {

  private final GroupScheduleService groupScheduleService;
  private final AlarmScheduleRepository alarmScheduleRepository;

  @Scheduled(fixedDelay = 60000)
  @Transactional(readOnly = true)
  public void pushAlarm() {
    LocalDateTime dateTime = clearSecondAndNano(LocalDateTime.now());
    List<AlarmSchedule> schedules = alarmScheduleRepository.findByDateTime(dateTime);

    schedules.forEach(it -> groupScheduleService.pushScheduleAlarm(it.getSchedule()));
  }
}
