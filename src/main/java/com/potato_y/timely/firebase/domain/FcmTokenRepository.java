package com.potato_y.timely.firebase.domain;

import com.potato_y.timely.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

  Optional<FcmToken> findByUser(User user);
}
