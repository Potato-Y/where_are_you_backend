package com.potato_y.timely.group.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@NoArgsConstructor
@RedisHash(value = "groupInviteCode", timeToLive = 172800)  // 2일 유효 기간
public class GroupInviteCode {

  @Id
  private String code;

  private Long groupId;

  @Builder
  public GroupInviteCode(String code, Long groupId) {
    this.code = code;
    this.groupId = groupId;
  }
}
