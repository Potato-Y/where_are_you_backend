package com.potato_y.where_are_you.group.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupInviteCodeRepository extends JpaRepository<GroupInviteCode, Long> {

  Optional<GroupInviteCode> findByCode(String code);
}
