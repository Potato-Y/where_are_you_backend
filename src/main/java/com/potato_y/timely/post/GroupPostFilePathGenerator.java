package com.potato_y.where_are_you.post;

import com.potato_y.where_are_you.post.domain.Post;
import java.util.StringJoiner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GroupPostFilePathGenerator {

  @Value("${aws.s3.default-image-path}")
  private String defaultImagePath;

  public String generateImagePath(Post post) {
    return defaultImagePath +
        new StringJoiner("/")
            .add(post.getGroup().getId().toString())
            .add(post.getId().toString());
  }
}
