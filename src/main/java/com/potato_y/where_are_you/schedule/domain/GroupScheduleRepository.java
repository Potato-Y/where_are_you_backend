package com.potato_y.where_are_you.schedule.domain;

import com.potato_y.where_are_you.group.domain.Group;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupScheduleRepository extends JpaRepository<GroupSchedule, Long> {

  List<GroupSchedule> findByGroup(Group group);
}
