package com.potato_y.timely.schedule.domain;

import com.potato_y.timely.user.domain.User;
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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@Table(name = "participations")
@NoArgsConstructor
public class Participation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_schedule_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private GroupSchedule schedule;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;

  @NotNull
  @Column(name = "is_participating")
  private boolean isParticipating;

  @Builder
  public Participation(GroupSchedule schedule, User user, Boolean isParticipating) {
    this.schedule = schedule;
    this.user = user;
    this.isParticipating = isParticipating;
  }

  public Participation updateIsParticipating(boolean isParticipating) {
    this.isParticipating = isParticipating;

    return this;
  }
}
