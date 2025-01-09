package com.potato_y.timely.schedule.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmScheduleRepository extends JpaRepository<AlarmSchedule, Long> {

  List<AlarmSchedule> findByDateTime(LocalDateTime dateTime);

  Optional<AlarmSchedule> findBySchedule(GroupSchedule schedule);
}
