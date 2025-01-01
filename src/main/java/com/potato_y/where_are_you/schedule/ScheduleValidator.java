package com.potato_y.where_are_you.schedule;

import com.potato_y.where_are_you.error.exception.ForbiddenException;
import com.potato_y.where_are_you.schedule.domain.GroupSchedule;
import com.potato_y.where_are_you.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class ScheduleValidator {

  public void scheduleOwner(GroupSchedule schedule, User user) {
    if (!schedule.getUser().getId().equals(user.getId())) {
      throw new ForbiddenException("삭제 권한이 없습니다");
    }
  }

}
