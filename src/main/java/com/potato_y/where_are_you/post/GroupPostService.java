package com.potato_y.where_are_you.post;

import com.potato_y.where_are_you.authentication.CurrentUserProvider;
import com.potato_y.where_are_you.aws.CloudFrontService;
import com.potato_y.where_are_you.aws.S3Service;
import com.potato_y.where_are_you.error.exception.BadRequestException;
import com.potato_y.where_are_you.error.exception.ForbiddenException;
import com.potato_y.where_are_you.error.exception.NotFoundException;
import com.potato_y.where_are_you.group.GroupService;
import com.potato_y.where_are_you.group.domain.Group;
import com.potato_y.where_are_you.post.domain.Post;
import com.potato_y.where_are_you.post.domain.PostFile;
import com.potato_y.where_are_you.post.domain.PostFileRepository;
import com.potato_y.where_are_you.post.domain.PostRepository;
import com.potato_y.where_are_you.post.dto.CreatePostRequest;
import com.potato_y.where_are_you.post.dto.PostResponse;
import com.potato_y.where_are_you.user.domain.User;
import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class GroupPostService {

  private final S3Service s3Service;
  private final CloudFrontService cloudFrontService;
  private final GroupService groupService;
  private final CurrentUserProvider currentUserProvider;
  private final PostRepository postRepository;
  private final PostFileRepository postFileRepository;
  private final PostFilePathGenerator postFilePathGenerator;

  @Transactional
  public PostResponse createGroupPost(Long groupId, CreatePostRequest request) {
    User user = currentUserProvider.getCurrentUser();
    if (!groupService.checkGroupMember(groupId, user)) {
      throw new ForbiddenException("그룹원이 아닙니다.");
    }
    Group group = groupService.findByGroup(groupId);

    Post post = postRepository.save(Post.builder()
        .group(group)
        .user(user)
        .title(request.title())
        .content(request.content())
        .build());

    if (request.isFiles()) {
      ArrayList<PostFile> postFiles = new ArrayList<>();

      for (MultipartFile file : request.files()) {
        String path = postFilePathGenerator.generateFilePath(post);

        try {
          String savePath = s3Service.uploadPostFile(file, path);

          postFiles.add(postFileRepository.save(PostFile.builder()
              .post(post)
              .filePath(savePath)
              .build()));
        } catch (IOException e) {
          postRepository.delete(post);
          throw new BadRequestException("잘못된 파일 형식입니다");
        }
      }
      post.updatePostFiles(postFiles);

      List<String> urls = postFiles.stream().map(it -> {
        try {
          return cloudFrontService.generateSignedUrl(it.getFilePath());
        } catch (IOException | InvalidKeySpecException e) {
          throw new BadRequestException("파일 URL을 생성할 수 없습니다");
        }
      }).toList();

      return PostResponse.from(post, urls);
    }

    return PostResponse.from(post);
  }

  @Transactional(readOnly = true)
  public PostResponse getGroupPost(Long groupId, Long postId) {
    User user = currentUserProvider.getCurrentUser();
    if (!groupService.checkGroupMember(groupId, user)) {
      throw new ForbiddenException("그룹원이 아닙니다.");
    }

    Post post = findByPostId(postId);
    List<String> postFilePaths = post.getPostFiles().stream().map(it -> {
      try {
        return cloudFrontService.generateSignedUrl(it.getFilePath());
      } catch (IOException | InvalidKeySpecException e) {
        throw new BadRequestException("파일 URL을 생성할 수 없습니다");
      }
    }).toList();

    return PostResponse.from(post, postFilePaths);
  }

  private Post findByPostId(Long postId) {
    return postRepository.findById(postId)
        .orElseThrow(() -> new NotFoundException("포스트를 찾을 수 없습니다"));
  }
}
