package com.potato_y.where_are_you.post;

import static com.potato_y.where_are_you.group.GroupTestUtils.createGroup;
import static com.potato_y.where_are_you.user.UserTestUtils.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.potato_y.where_are_you.authentication.CurrentUserProvider;
import com.potato_y.where_are_you.aws.CloudFrontService;
import com.potato_y.where_are_you.aws.S3Service;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
  private PostFilePathGenerator postFilePathGenerator;

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
    given(postFilePathGenerator.generateFilePath(any(Post.class)))
        .willReturn(signedUrl1).willReturn(signedUrl2);
    given(s3Service.uploadPostFile(any(MultipartFile.class), anyString()))
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
}
