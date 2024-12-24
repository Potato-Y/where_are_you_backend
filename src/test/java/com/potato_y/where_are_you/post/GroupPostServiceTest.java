package com.potato_y.where_are_you.post;

import static com.potato_y.where_are_you.group.GroupTestUtils.createGroup;
import static com.potato_y.where_are_you.user.UserTestUtils.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.potato_y.where_are_you.authentication.CurrentUserProvider;
import com.potato_y.where_are_you.aws.CloudFrontService;
import com.potato_y.where_are_you.aws.S3Service;
import com.potato_y.where_are_you.error.exception.ForbiddenException;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class GroupPostServiceTest {

  @InjectMocks
  private GroupPostService groupPostService;

  @Mock
  private S3Service s3Service;

  @Mock
  private CloudFrontService cloudFrontService;

  @Mock
  GroupService groupService;

  @Mock
  CurrentUserProvider currentUserProvider;

  @Mock
  private PostRepository postRepository;

  @Mock
  private PostFileRepository postFileRepository;

  @Mock
  private GroupPostFilePathGenerator groupPostFilePathGenerator;

  private User testUser;
  private Group testGroup;

  @BeforeEach
  void setUp() {
    testUser = testUser = createUser("test@mail.com", "test user", "1");
    testGroup = createGroup("group", testUser);
  }

  @Test
  @DisplayName("createGroupPost(): 게시글을 생성할 수 있다 - 파일 없음")
  void successCreateGroupPost_noFile() {
    CreatePostRequest request = new CreatePostRequest("title", "content", null);
    Post post = Post.builder()
        .group(testGroup)
        .user(testUser)
        .title(request.title())
        .content(request.content())
        .build();

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupService.findByGroup(anyLong())).willReturn(testGroup);
    given(groupService.checkGroupMember(anyLong(), any(User.class))).willReturn(true);
    given(postRepository.save(any(Post.class))).willReturn(post);

    PostResponse response = groupPostService.createGroupPost(1L, request);

    assertThat(response.title()).isEqualTo(request.title());
    assertThat(response.content()).isEqualTo(request.content());
    assertThat(response.files()).isEmpty();
  }

  @Test
  @DisplayName("createGroupPost(): 게시글을 생성할 수 있다 - 파일 있음")
  void successCreateGroupPost_twoFiles() throws IOException, InvalidKeySpecException {
    String fileName1 = "test1.jpg";
    String fileName2 = "test2.jpg";
    byte[] content = "test image".getBytes();
    MultipartFile file1 = new MockMultipartFile("test1", fileName1, "image/jpeg", content);
    MultipartFile file2 = new MockMultipartFile("test2", fileName2, "image/jpeg", content);
    List<MultipartFile> files = List.of(file1, file2);

    String signedUrl1 = "signedUrl1";
    String signedUrl2 = "signedUrl2";

    CreatePostRequest request = new CreatePostRequest("title", "content", files);

    Post post = Post.builder()
        .group(testGroup)
        .user(testUser)
        .title(request.title())
        .content(request.content())
        .build();

    PostFile postFile1 = PostFile.builder().post(post).filePath(fileName1).build();
    PostFile postFile2 = PostFile.builder().post(post).filePath(fileName2).build();

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupService.findByGroup(anyLong())).willReturn(testGroup);
    given(groupService.checkGroupMember(anyLong(), any(User.class))).willReturn(true);
    given(postRepository.save(any(Post.class))).willReturn(post);
    given(groupPostFilePathGenerator.generateImagePath(any(Post.class)))
        .willReturn(signedUrl1).willReturn(signedUrl2);
    given(s3Service.uploadFile(any(MultipartFile.class), anyString()))
        .willReturn("test1").willReturn("test2");
    given(postFileRepository.save(any(PostFile.class))).willReturn(postFile1).willReturn(postFile2);
    given(cloudFrontService.generateSignedUrl("test1.jpg")).willReturn(signedUrl1);
    given(cloudFrontService.generateSignedUrl("test2.jpg")).willReturn(signedUrl2);

    PostResponse response = groupPostService.createGroupPost(1L, request);

    assertThat(response.title()).isEqualTo(request.title());
    assertThat(response.content()).isEqualTo(request.content());
    assertThat(response.files()).hasSize(2);
    assertThat(response.files().getFirst()).isEqualTo(signedUrl1);
    assertThat(response.files().getLast()).isEqualTo(signedUrl2);
  }

  @Test
  @DisplayName("createGroupPost(): 그룹원이 아니라면 포스트 생성 시 예외가 발생한다")
  void failCreateGroupPost() {
    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupService.checkGroupMember(anyLong(), any(User.class))).willReturn(false);

    assertThatThrownBy(() -> groupPostService.createGroupPost(1L,
        new CreatePostRequest("title", "content", List.of())))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("getGroupPost(): 특정 포스트 조회할 수 있다 - 빈 파일")
  void successGetGroupPost_noFile() {
    var title = "title";
    var content = "content";

    Post post = Post.builder()
        .group(testGroup)
        .user(testUser)
        .title(title)
        .content(content)
        .build();

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupService.checkGroupMember(anyLong(), any(User.class))).willReturn(true);
    given(postRepository.findById(anyLong())).willReturn(Optional.of(post));

    PostResponse response = groupPostService.getGroupPost(1L, 1L);

    assertThat(response.title()).isEqualTo(title);
    assertThat(response.content()).isEqualTo(content);
    assertThat(response.files()).hasSize(0);
  }

  @Test
  @DisplayName("getGroupPost(): 특정 포스트 조회할 수 있다 - 두 개의 파일")
  void successGetGroupPost_twoFile() throws IOException, InvalidKeySpecException {
    var title = "title";
    var content = "content";
    var signedUrl1 = "path1";
    var signedUrl2 = "path2";

    Post post = Post.builder()
        .group(testGroup)
        .user(testUser)
        .title(title)
        .content(content)
        .build();
    PostFile postFile1 = PostFile.builder().post(post).filePath("path").build();
    PostFile postFile2 = PostFile.builder().post(post).filePath("path").build();
    post.updatePostFiles(List.of(postFile1, postFile2));

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupService.checkGroupMember(anyLong(), any(User.class))).willReturn(true);
    given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
    given(cloudFrontService.generateSignedUrl(anyString()))
        .willReturn(signedUrl1).willReturn(signedUrl2);

    PostResponse response = groupPostService.getGroupPost(1L, 1L);

    assertThat(response.title()).isEqualTo(title);
    assertThat(response.content()).isEqualTo(content);
    assertThat(response.files()).hasSize(2);
    assertThat(response.files().getFirst()).isEqualTo(signedUrl1);
    assertThat(response.files().getLast()).isEqualTo(signedUrl2);
  }

  @Test
  @DisplayName("getGroupPost(): 그룹원이 아니라면 포스트 조회 시 예외가 발생한다")
  void failGetGroupPost() {
    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupService.checkGroupMember(anyLong(), any(User.class))).willReturn(false);

    assertThatThrownBy(() -> groupPostService.getGroupPost(1L, 1L))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("getGroupPosts(): 그룹 포스트들을 조회할 수 있다")
  void successGetGroupPosts() {
    int page = 0;
    int pageSize = 10;

    List<Post> postList = List.of(
        Post.builder().title("title1").content("content1").group(testGroup).user(testUser).build(),
        Post.builder().title("title1").content("content1").group(testGroup).user(testUser).build(),
        Post.builder().title("title1").content("content1").group(testGroup).user(testUser).build(),
        Post.builder().title("title1").content("content1").group(testGroup).user(testUser).build()
    );

    PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
    Page<Post> postPage = new PageImpl<>(postList, pageRequest, postList.size());

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupService.checkGroupMember(anyLong(), any(User.class))).willReturn(true);
    given(postRepository.findByGroupIdOrderByIdDesc(anyLong(), any(Pageable.class))).willReturn(
        postPage);

    List<PostResponse> response = groupPostService.getGroupPosts(1L, page);

    assertThat(response.size()).isEqualTo(postList.size());
    assertThat(response.get(0).title()).isEqualTo(postList.get(0).getTitle());
    assertThat(response.get(1).title()).isEqualTo(postList.get(1).getTitle());
    assertThat(response.get(2).title()).isEqualTo(postList.get(2).getTitle());
    assertThat(response.get(3).title()).isEqualTo(postList.get(3).getTitle());
  }

  @Test
  @DisplayName("getGroupPosts(): 그룹원이 아니면 예외가 발생한다")
  void failGetGroupPosts() {
    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupService.checkGroupMember(anyLong(), any(User.class))).willReturn(false);

    assertThatThrownBy(() -> groupPostService.getGroupPosts(1L, 1))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("deleteGroupPost(): 포스트를 삭제할 수 있다 - 파일이 있다")
  void successDeleteGroupPost_twoFile() throws IOException, InvalidKeySpecException {
    Post post = Post.builder()
        .group(testGroup)
        .user(testUser)
        .title("title")
        .content("content")
        .build();
    PostFile postFile1 = PostFile.builder().post(post).filePath("path").build();
    PostFile postFile2 = PostFile.builder().post(post).filePath("path").build();
    post.updatePostFiles(List.of(postFile1, postFile2));

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupService.checkGroupMember(anyLong(), any(User.class))).willReturn(true);
    given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
    given(groupPostFilePathGenerator.generateImagePath(any(Post.class))).willReturn("path");
    doNothing().when(s3Service).deleteFolder(anyString());

    groupPostService.deleteGroupPost(1L, 1L);

    verify(postRepository, times(1)).delete(any(Post.class));
  }

  @Test
  @DisplayName("deleteGroupPost(): 포스트를 삭제할 수 있다 - 파일이 없다")
  void successDeleteGroupPost_noFile() {
    Post post = Post.builder()
        .group(testGroup)
        .user(testUser)
        .title("title")
        .content("content")
        .build();

    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupService.checkGroupMember(anyLong(), any(User.class))).willReturn(true);
    given(postRepository.findById(anyLong())).willReturn(Optional.of(post));

    groupPostService.deleteGroupPost(1L, 1L);

    verify(postRepository, times(1)).delete(any(Post.class));
  }

  @Test
  @DisplayName("deleteGroupPost(): 그룹원이 아니면 예외가 발생한다")
  void failDeleteGroupPost() {
    given(currentUserProvider.getCurrentUser()).willReturn(testUser);
    given(groupService.checkGroupMember(anyLong(), any(User.class))).willReturn(false);

    assertThatThrownBy(() -> groupPostService.deleteGroupPost(1L, 1L))
        .isInstanceOf(ForbiddenException.class);
  }
}
