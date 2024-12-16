package com.potato_y.where_are_you.post;

import com.potato_y.where_are_you.post.domain.Post;
import java.util.StringJoiner;
import org.springframework.stereotype.Component;

@Component
public class PostFilePathGenerator {

  public String generateFilePath(Post post) {
    return new StringJoiner("/")
        .add(post.getGroup().getId().toString())
        .add(post.getId().toString())
        .toString();
  }
}
