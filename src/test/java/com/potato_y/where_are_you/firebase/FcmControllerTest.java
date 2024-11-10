package com.potato_y.where_are_you.firebase;

import static com.potato_y.where_are_you.group.GroupTestUtils.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.potato_y.where_are_you.firebase.domain.FcmToken;
import com.potato_y.where_are_you.firebase.domain.FcmTokenRepository;
import com.potato_y.where_are_you.firebase.dto.FcmTokenRequest;
import com.potato_y.where_are_you.user.domain.User;
import com.potato_y.where_are_you.user.domain.UserRepository;
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
class FcmControllerTest {

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private FcmTokenRepository fcmTokenRepository;

  private User testUser;

  @BeforeEach
  void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

    fcmTokenRepository.deleteAll();
    userRepository.deleteAll();

    testUser = userRepository.save(createUser("test@mail.com", "test user", "1"));
  }

  @Test
  @WithMockUser("1")
  @DisplayName("saveOrUpdateFcmToken(): Token을 업데이트 할 수 있다.")
  void successSaveOrUpdateFcmToken_update() throws Exception {
    // given
    final String url = "/v1/fcm";

    fcmTokenRepository.save(FcmToken.builder()
        .user(testUser)
        .token("old_token")
        .build());

    FcmTokenRequest request = new FcmTokenRequest("newFcmToken");
    final String requestBody = objectMapper.writeValueAsString(request);

    // when
    ResultActions result = mockMvc.perform(post(url)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(requestBody));

    // then
    FcmToken saveToken = fcmTokenRepository.findByUser(testUser).get();
    result.andExpect(status().isOk());
    assertThat(saveToken.getToken()).isEqualTo(request.token());
  }

  @Test
  @WithMockUser("1")
  @DisplayName("saveOrUpdateFcmToken(): Token을 저장할 수 있다.")
  void successSaveOrUpdateFcmToken_save() throws Exception {
    // given
    final String url = "/v1/fcm";

    FcmTokenRequest request = new FcmTokenRequest("newFcmToken");
    final String requestBody = objectMapper.writeValueAsString(request);

    // when
    ResultActions result = mockMvc.perform(post(url)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(requestBody));

    // then
    FcmToken saveToken = fcmTokenRepository.findByUser(testUser).get();
    result.andExpect(status().isOk());
    assertThat(saveToken.getToken()).isEqualTo(request.token());
  }

  @Test
  @WithMockUser("1")
  @DisplayName("saveOrUpdateFcmToken(): 공백 Token은 업데이트 할 수 없다.")
  void failSaveOrUpdateFcmToken_update() throws Exception {
    // given
    final String url = "/v1/fcm";

    fcmTokenRepository.save(FcmToken.builder()
        .user(testUser)
        .token("old_token")
        .build());

    FcmTokenRequest request = new FcmTokenRequest("");
    final String requestBody = objectMapper.writeValueAsString(request);

    // when
    ResultActions result = mockMvc.perform(post(url)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(requestBody));

    // then
    result.andExpect(status().isInternalServerError());
  }

  @Test
  @WithMockUser("1")
  @DisplayName("saveOrUpdateFcmToken(): 빈 Token은 저장할 수 없다.")
  void failSaveOrUpdateFcmToken_save() throws Exception {
    // given
    final String url = "/v1/fcm";

    FcmTokenRequest request = new FcmTokenRequest("");
    final String requestBody = objectMapper.writeValueAsString(request);

    // when
    ResultActions result = mockMvc.perform(post(url)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(requestBody));

    // then
    result.andExpect(status().isInternalServerError());
  }
}