package com.potato_y.timely.schedule.domain;

import com.potato_y.timely.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

  Optional<Participation> findByUserAndSchedule(User user, GroupSchedule schedule);

  List<Participation> findBySchedule(GroupSchedule groupSchedule);
}
