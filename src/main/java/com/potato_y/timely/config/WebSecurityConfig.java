package com.potato_y.where_are_you.config;


import com.potato_y.where_are_you.authentication.domain.oauth.RequestOAuthInfoService;
import com.potato_y.where_are_you.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {

  private final TokenProvider tokenProvider;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
      RequestOAuthInfoService requestOAuthInfoService) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable).logout(AbstractHttpConfigurer::disable)

        // JWT 사용을 위해 세션 사용 비활성화
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // 헤더를 확인하는 커스텀 필터 추가
        .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

        .authorizeHttpRequests(authz -> authz
            // 로그인, 회원가입, 토큰 갱신을 제외한 api는 인증을 하도록 설정
            .requestMatchers("/auth/**").permitAll()
            .anyRequest().authenticated())

        .build();
  }

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public TokenAuthenticationFilter tokenAuthenticationFilter() {
    return new TokenAuthenticationFilter(tokenProvider);
  }
}
