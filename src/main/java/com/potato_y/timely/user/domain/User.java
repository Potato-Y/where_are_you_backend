package com.potato_y.timely.user.domain;

import com.potato_y.timely.authentication.domain.oauth.OAuthProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Entity
@NoArgsConstructor
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "oauth_id_unique",
            columnNames = {
                "provider_account_id",
                "oauth_provider"
            }
        )
    }
)
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Column(name = "provider_account_id")
  private String providerAccountId;

  @Column(name = "email")
  private String email;

  @NotNull
  @Column(name = "nickname")
  private String nickname;

  @NotNull
  @Column(name = "password")
  private String password;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "oauth_provider")
  private OAuthProvider oauthProvider;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("user"));
  }

  @Override // 사용자 id 반환
  public String getUsername() {
    return String.valueOf(id);
  }

  @Override // 사용자 패스워드 반환
  public String getPassword() {
    return password;
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
  public User(String providerAccountId, String email, String nickname, String password,
      OAuthProvider oauthProvider) {
    this.providerAccountId = providerAccountId;
    this.email = email;
    this.password = password;
    this.nickname = nickname;
    this.oauthProvider = oauthProvider;
  }
}
