package com.potato_y.timely.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.potato_y.timely.config.jwt.FirebaseProperties;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Slf4j
@Configuration
@AllArgsConstructor
public class FirebaseConfig {

  private final FirebaseProperties firebaseProperties;

  @PostConstruct
  public void init() {
    try {
      InputStream serviceAccount = new ClassPathResource(
          firebaseProperties.getPath()).getInputStream();
      FirebaseOptions options = new FirebaseOptions.Builder()
          .setCredentials(GoogleCredentials.fromStream(serviceAccount))
          .build();

      if (FirebaseApp.getApps().isEmpty()) { // FirebaseApp이 이미 초기화되어 있지 않은 경우에만 초기화 실행
        FirebaseApp.initializeApp(options);
      }
    } catch (Exception e) {
      log.warn(e.getMessage());
    }
  }
}
