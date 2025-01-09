package com.potato_y.timely.schedule;

import com.potato_y.timely.error.exception.ForbiddenException;
import com.potato_y.timely.schedule.domain.GroupSchedule;
import com.potato_y.timely.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class ScheduleValidator {

  public void scheduleOwner(GroupSchedule schedule, User user) {
    if (!schedule.getUser().getId().equals(user.getId())) {
      throw new ForbiddenException("삭제 권한이 없습니다");
    }
  }

}
