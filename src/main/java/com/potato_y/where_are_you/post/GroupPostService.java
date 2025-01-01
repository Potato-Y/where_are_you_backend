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
import com.potato_y.where_are_you.post.dto.UpdatePostRequest;
import com.potato_y.where_are_you.user.domain.User;
import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class GroupPostService {

  private final static int PAGE_SIZE = 10;

  private final S3Service s3Service;
  private final CloudFrontService cloudFrontService;
  private final GroupService groupService;
  private final CurrentUserProvider currentUserProvider;
  private final PostRepository postRepository;
  private final PostFileRepository postFileRepository;
  private final GroupPostFilePathGenerator groupPostFilePathGenerator;

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
        String path = groupPostFilePathGenerator.generateImagePath(post);

        try {
          String savePath = s3Service.uploadFile(file, path);

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
    List<String> postFilePaths = getFileUrls(post);

    return PostResponse.from(post, postFilePaths);
  }

  @Transactional(readOnly = true)
  public List<PostResponse> getGroupPosts(Long groupId, int page) {
    User user = currentUserProvider.getCurrentUser();
    if (!groupService.checkGroupMember(groupId, user)) {
      throw new ForbiddenException("그룹원이 아닙니다.");
    }

    PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending());
    Page<Post> posts = postRepository.findByGroupIdOrderByIdDesc(groupId, pageRequest);

    return posts.map(post -> {
      List<String> postFilePaths = getFileUrls(post);

      return PostResponse.from(post, postFilePaths);
    }).stream().toList();
  }

  @Transactional
  public void deleteGroupPost(Long groupId, Long postId) {
    User user = currentUserProvider.getCurrentUser();
    if (!groupService.checkGroupMember(groupId, user)) {
      throw new ForbiddenException("그룹원이 아닙니다.");
    }

    Post post = findByPostId(postId);
    validateWriter(user, post);

    if (!post.getPostFiles().isEmpty()) {
      s3Service.deleteFolder(groupPostFilePathGenerator.generateImagePath(post));
    }
    postRepository.delete(post);
  }

  @Transactional
  public PostResponse updateGroupPost(Long groupId, Long postId, UpdatePostRequest request) {
    User user = currentUserProvider.getCurrentUser();
    if (!groupService.checkGroupMember(groupId, user)) { // 현재 그룹 멤버인지 확인
      throw new ForbiddenException("그룹원이 아닙니다.");
    }
    Post post = findByPostId(postId);
    validateWriter(user, post);

    return PostResponse.from(
        post.updateTitle(request.title())
            .updateContent(request.content()),
        getFileUrls(post));
  }

  private Post findByPostId(Long postId) {
    return postRepository.findById(postId)
        .orElseThrow(() -> new NotFoundException("포스트를 찾을 수 없습니다"));
  }

  private List<String> getFileUrls(Post post) {
    List<String> urls = new ArrayList<>();
    post.getPostFiles().forEach(file -> {
      try {
        urls.add(cloudFrontService.generateSignedUrl(file.getFilePath()));
      } catch (InvalidKeySpecException | IOException e) {
        throw new RuntimeException(e);
      }
    });

    return urls;
  }

  private void validateWriter(User user, Post post) {
    if (!user.equals(post.getUser())) {
      throw new ForbiddenException("작성자가 아닙니다. 변경 권한이 없습니다.");
    }
  }
}
