package com.potato_y.where_are_you.firebase.domain;

import com.potato_y.where_are_you.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

  Optional<FcmToken> findByUser(User user);
}
