package com.potato_y.where_are_you.post.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

  Page<Post> findByGroupIdOrderByIdDesc(Long groupId, Pageable pageable);
}
