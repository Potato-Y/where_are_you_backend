package com.potato_y.where_are_you.group.domain;

import com.potato_y.where_are_you.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

  List<GroupMember> findByGroup(Group group);

  Optional<GroupMember> findByGroupAndUser(Group group, User user);
}
