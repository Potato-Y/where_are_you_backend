package com.potato_y.where_are_you.location.domain;

import com.potato_y.where_are_you.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {

  Optional<UserLocation> findByUser(User user);
}
