package com.potato_y.where_are_you.user.domain;

import com.potato_y.where_are_you.authentication.domain.oauth.OAuthProvider;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Table(name = "users")
@Getter
@Entity
@NoArgsConstructor
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String serviceId;

  private String email;

  private String nickname;

  @Enumerated(EnumType.STRING)
  private OAuthProvider oAuthProvider;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("user"));
  }

  @Override // 사용자 id 반환
  public String getUsername() {
    return email;
  }

  @Override // 사용자 패스워드 반환
  public String getPassword() {
    // TODO: 랜덤 값으로 변경하기
    return "sd43adkfl2Kkejrasd12!@#q135";
  }

  @Override
  public boolean isAccountNonExpired() {
    // 만료되었는지 확인하는 로직
    return true; // true -> 만료되지 않음
  }

  @Override
  public boolean isAccountNonLocked() {
    // 계정이 잠금되었는지 확인하는 로직
    return true; // true -> 잠금되지 않음
  }

  @Override
  public boolean isCredentialsNonExpired() {
    // 패스워드가 만료되었는지 확인하는 로직
    return true; // true -> 만료되지 않음
  }

  @Override
  public boolean isEnabled() {
    // 계정이 사용 가능한지 확인하는 로직
    return true; // true -> 사용 가능
  }

  @Builder
  public User(String serviceId, String email, String nickname, OAuthProvider oAuthProvider) {
    this.serviceId = serviceId;
    this.email = email;
    this.nickname = nickname;
    this.oAuthProvider = oAuthProvider;
  }
}
