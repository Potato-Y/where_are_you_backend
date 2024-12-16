package com.potato_y.where_are_you.post.domain;

import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@Table(name = "posts")
@NoArgsConstructor
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
  @Column(name = "group_name", length = 20)
  private String title;

  @Column(name = "content")
  private String content;

  @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
  private List<PostFile> postFiles = new ArrayList<>();

  @Builder
  public Post(Group group, User user, String title, String content) {
    this.group = group;
    this.user = user;
    this.title = title;
    this.content = content;
  }

  public Post updatePostFiles(List<PostFile> postFiles) {
    this.postFiles = postFiles;

    return this;
  }
}
