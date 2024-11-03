package com.potato_y.where_are_you.group.domain;

import com.potato_y.where_are_you.user.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {

  List<Group> findByHostUser(User hostUser);
}
