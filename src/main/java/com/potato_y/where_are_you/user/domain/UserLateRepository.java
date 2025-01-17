package com.potato_y.where_are_you.user.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLateRepository extends JpaRepository<UserLate, Long> {

  Optional<UserLate> findByUser(User user);
}
