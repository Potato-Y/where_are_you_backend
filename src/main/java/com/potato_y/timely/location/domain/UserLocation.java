package com.potato_y.timely.location.domain;

import com.potato_y.timely.location.converter.LocationEncryptConverter;
import com.potato_y.timely.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@Table(name = "user_location")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserLocation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;

  @Convert(converter = LocationEncryptConverter.class)
  @Column(name = "location_latitude")
  private Double locationLatitude;

  @Convert(converter = LocationEncryptConverter.class)
  @Column(name = "location_longitude")
  private Double locationLongitude;

  @Column(name = "state_message")
  private String stateMessage;

  @LastModifiedDate
  private LocalDateTime updateAt;

  @Builder
  public UserLocation(User user, Double locationLatitude, Double locationLongitude,
      String stateMessage) {
    this.user = user;
    this.locationLatitude = locationLatitude;
    this.locationLongitude = locationLongitude;
    this.stateMessage = stateMessage;
  }

  public UserLocation updateLocation(Double locationLatitude, Double locationLongitude) {
    this.locationLatitude = locationLatitude;
    this.locationLongitude = locationLongitude;

    return this;
  }

  public UserLocation updateStateMessage(String stateMessage) {
    this.stateMessage = stateMessage;

    return this;
  }
}
