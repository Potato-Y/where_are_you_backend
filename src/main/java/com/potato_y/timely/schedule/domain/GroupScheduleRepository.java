package com.potato_y.timely.schedule.domain;

import com.potato_y.timely.group.domain.Group;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupScheduleRepository extends JpaRepository<GroupSchedule, Long> {

  List<GroupSchedule> findByGroup(Group group);

  List<GroupSchedule> findByStartTime(LocalDateTime startTime);
}
