package com.potato_y.where_are_you.schedule.domain;

import com.potato_y.where_are_you.group.domain.Group;
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
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@Table(name = "group_schedule")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class GroupSchedule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Group group;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @OnDelete(action = OnDeleteAction.SET_NULL)
  private User user;

  @NotNull
  @Column(name = "title", length = 100)
  private String title;

  @NotNull
  @Column(name = "start_time")
  private LocalDateTime startTime;

  @NotNull
  @Column(name = "end_time")
  private LocalDateTime endTime;

  @NotNull
  @ColumnDefault("false")
  @Column(name = "is_alarm_enabled")
  private Boolean isAlarmEnabled;

  @NotNull
  @ColumnDefault("1")
  @Column(name = "alarm_before_hours")
  private int alarmBeforeHours;

  @Column(name = "location")
  private String location;

  @Column(name = "location_latitude")
  private double locationLatitude;

  @Column(name = "location_longitude")
  private double locationLongitude;

  @CreatedDate
  @NotNull
  @Column(name = "create_at", updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updateAt;

  @Builder
  public GroupSchedule(Group group, User user, String title, LocalDateTime startTime,
      LocalDateTime endTime, Boolean isAlarmEnabled, int alarmBeforeHours, String location,
      double locationLatitude, double locationLongitude) {
    this.group = group;
    this.user = user;
    this.title = title;
    this.startTime = startTime;
    this.endTime = endTime;
    this.isAlarmEnabled = isAlarmEnabled;
    this.alarmBeforeHours = alarmBeforeHours;
    this.location = location;
    this.locationLatitude = locationLatitude;
    this.locationLongitude = locationLongitude;
  }
}
