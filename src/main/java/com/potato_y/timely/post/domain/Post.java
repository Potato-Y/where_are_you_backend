package com.potato_y.timely.post.domain;

import com.potato_y.timely.group.domain.Group;
import com.potato_y.timely.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@Table(name = "posts")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Post {

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
  @Column(name = "title", length = 20)
  private String title;

  @Column(name = "content")
  private String content;

  @CreatedDate
  @NotNull
  @Column(name = "create_at", updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updateAt;

  @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
  private List<PostFile> postFiles = new ArrayList<>();

  @Builder
  public Post(Group group, User user, String title, String content) {
    this.group = group;
    this.user = user;
    this.title = title;
    this.content = content;
  }

  public Post updateTitle(String title) {
    this.title = title;

    return this;
  }

  public Post updateContent(String content) {
    this.content = content;

    return this;
  }

  public Post updatePostFiles(List<PostFile> postFiles) {
    this.postFiles = postFiles;

    return this;
  }
}
