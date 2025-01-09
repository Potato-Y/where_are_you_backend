package com.potato_y.timely.group.domain;

import com.potato_y.timely.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "group_members")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class GroupMember {

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
  @Enumerated(EnumType.STRING)
  private GroupMemberType memberType;

  @CreatedDate
  @NotNull
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public GroupMember(Group group, User user, GroupMemberType memberType) {
    this.group = group;
    this.user = user;
    this.memberType = memberType;
  }
}
