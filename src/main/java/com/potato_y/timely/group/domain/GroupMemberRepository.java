package com.potato_y.timely.group.domain;

import com.potato_y.timely.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

  List<GroupMember> findByGroup(Group group);

  Optional<GroupMember> findByGroupAndUser(Group group, User user);

  List<GroupMember> findByUser(User user);
}
