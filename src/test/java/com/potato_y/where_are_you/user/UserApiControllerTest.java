package com.potato_y.where_are_you.user;

import static com.potato_y.where_are_you.user.UserTestUtils.createUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.potato_y.where_are_you.authentication.domain.oauth.OAuthProvider;
import com.potato_y.where_are_you.user.domain.User;
import com.potato_y.where_are_you.user.domain.UserLate;
import com.potato_y.where_are_you.user.domain.UserLateRepository;
import com.potato_y.where_are_you.user.domain.UserRepository;
import com.potato_y.where_are_you.user.dto.UserLateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
class UserApiControllerTest {

  @Autowired
  protected MockMvc mockMvc;
  @Autowired
  protected ObjectMapper objectMapper;
  @Autowired
  private WebApplicationContext context;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private UserLateRepository userLateRepository;

  @BeforeEach
  public void mockMvcSetup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    userRepository.deleteAll();
  }

  @DisplayName("getMyAccount(): 자신의 정보를 불러온다")
  @WithMockUser(username = "1")
  @Test
  public void successGetMyAccount() throws Exception {
    final String url = "/v1/users/me";
    User user = userRepository.save(User.builder()
        .oAuthProvider(OAuthProvider.KAKAO)
        .email("user@mail.com")
        .password("password")
        .nickname("name")
        .serviceId("2")
        .build());

    ResultActions result = mockMvc.perform(get(url));

    result.andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(user.getId()))
        .andExpect(jsonPath("$.email").value(user.getEmail()))
        .andExpect(jsonPath("$.nickname").value(user.getNickname()));
  }

  @Test
  @WithMockUser("1")
  @DisplayName("updateUserLate(): 지각 카운트를 추가할 수 있다 - 새로운 추가")
  void successUpdateUserLate_new_true() throws Exception {
    final String url = "/v1/users/late";
    User user = userRepository.save(createUser("test@mail.com", "test user", "1"));
    UserLateRequest request = new UserLateRequest(true);
    final var requestBody = objectMapper.writeValueAsString(request);

    ResultActions result = mockMvc.perform(
        post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody));

    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.userId").value(user.getId()))
        .andExpect(jsonPath("$.user.nickname").value(user.getNickname()))
        .andExpect(jsonPath("$.lateData.participation").value(1L))
        .andExpect(jsonPath("$.lateData.late").value(1L));
  }

  @Test
  @WithMockUser("1")
  @DisplayName("updateUserLate(): 지각 카운트를 추가할 수 있다 - 새로운 추가")
  void successUpdateUserLate_new_false() throws Exception {
    final String url = "/v1/users/late";
    User user = userRepository.save(createUser("test@mail.com", "test user", "1"));
    UserLateRequest request = new UserLateRequest(false);
    final var requestBody = objectMapper.writeValueAsString(request);

    ResultActions result = mockMvc.perform(
        post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody));

    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.userId").value(user.getId()))
        .andExpect(jsonPath("$.user.nickname").value(user.getNickname()))
        .andExpect(jsonPath("$.lateData.participation").value(1L))
        .andExpect(jsonPath("$.lateData.late").value(0L));
  }

  @Test
  @WithMockUser("1")
  @DisplayName("updateUserLate(): 지각 카운트를 추가할 수 있다 - 기존 값을 업데이트")
  void successUpdateUserLate_update() throws Exception {
    final String url = "/v1/users/late";
    User user = userRepository.save(createUser("test@mail.com", "test user", "1"));
    UserLate userLate = userLateRepository.save(UserLate.builder().user(user).build())
        .upCount(true);
    UserLateRequest request = new UserLateRequest(true);
    final var requestBody = objectMapper.writeValueAsString(request);

    ResultActions result = mockMvc.perform(
        post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody));

    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.userId").value(user.getId()))
        .andExpect(jsonPath("$.user.nickname").value(user.getNickname()))
        .andExpect(jsonPath("$.lateData.participation").value(2L))
        .andExpect(jsonPath("$.lateData.late").value(2L));
  }
}
