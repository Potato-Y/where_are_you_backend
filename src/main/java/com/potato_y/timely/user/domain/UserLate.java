package com.potato_y.timely.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@Table(name = "users_late")
@NoArgsConstructor
public class UserLate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;

  @NotNull
  @ColumnDefault("0")
  @Column(name = "participation_count")
  private Long participationCount = 0L;

  @NotNull
  @ColumnDefault("0")
  @Column(name = "late_count")
  private Long lateCount = 0L;

  @Builder
  public UserLate(User user) {
    this.user = user;
  }

  public UserLate upCount(boolean isLate) {
    this.participationCount++;
    if (isLate) {
      this.lateCount++;
    }

    return this;
  }
}
