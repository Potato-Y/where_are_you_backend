package com.potato_y.where_are_you.schedule.domain;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmScheduleRepository extends JpaRepository<AlarmSchedule, Long> {

  List<AlarmSchedule> findByDateTime(LocalDateTime dateTime);
}
