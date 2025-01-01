package com.potato_y.where_are_you.group.domain;

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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@Table(name = "groups")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Group {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "host_user_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User hostUser;

  @NotNull
  @Column(name = "group_name", length = 20)
  private String groupName;

  @NotNull
  @ColumnDefault("1")
  @Column(name = "cover_color")
  private int coverColor = 0;

  @CreatedDate
  @NotNull
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public Group(User hostUser, String groupName, int coverColor) {
    this.hostUser = hostUser;
    this.groupName = groupName;
    this.coverColor = coverColor;
  }

  public Group updateGroupName(String groupName) {
    this.groupName = groupName;

    return this;
  }

  public Group updateCoverColor(int coverColor) {
    this.coverColor = coverColor;

    return this;
  }
}
