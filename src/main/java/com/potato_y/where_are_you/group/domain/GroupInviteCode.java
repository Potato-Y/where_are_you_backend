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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@Table(name = "group_invite_codes")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class GroupInviteCode {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Group group;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;

  @NotNull
  @Column(name = "code", unique = true)
  private String code;

  @CreatedDate
  @NotNull
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public GroupInviteCode(Group group, User createUser, String code) {
    this.group = group;
    this.user = createUser;
    this.code = code;
  }
}