package com.potato_y.where_are_you.firebase.domain;

import com.potato_y.where_are_you.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@Table(name = "fcm_tokens")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FcmToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", unique = true, nullable = false)
  private User user;

  @NotNull
  @Column(name = "token")
  private String token;

  @CreatedDate
  @NotNull
  @Column(name = "create_at", updatable = false)
  private LocalDateTime createAt;

  @Builder
  public FcmToken(User user, String token) {
    this.user = user;
    this.token = token;
  }

  public FcmToken updateToken(String token) {
    this.token = token;

    return this;
  }
}
