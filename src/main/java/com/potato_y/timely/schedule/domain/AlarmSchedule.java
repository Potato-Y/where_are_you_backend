package com.potato_y.where_are_you.schedule.domain;

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
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@Table(name = "alarm_schedules")
@NoArgsConstructor
public class AlarmSchedule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "schedule_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private GroupSchedule schedule;

  @NotNull
  @Column(name = "date_time")
  private LocalDateTime dateTime;

  @Builder
  public AlarmSchedule(GroupSchedule schedule, LocalDateTime dateTime) {
    this.schedule = schedule;
    this.dateTime = dateTime;
  }

  public AlarmSchedule updateDateTime(LocalDateTime dateTime) {
    this.dateTime = dateTime;

    return this;
  }
}
