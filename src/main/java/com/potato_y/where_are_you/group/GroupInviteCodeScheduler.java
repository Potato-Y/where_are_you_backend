package com.potato_y.where_are_you.group;

import com.potato_y.where_are_you.group.domain.GroupInviteCode;
import com.potato_y.where_are_you.group.domain.GroupInviteCodeRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GroupInviteCodeScheduler {

  private final GroupInviteCodeRepository groupInviteCodeRepository;
  private static final int EXPIRATION_DATE_CRITERIA = 2;

  @Scheduled(fixedDelay = 60000)
  @Transactional
  public void deleteUserLocation() {
    LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(EXPIRATION_DATE_CRITERIA);

    List<GroupInviteCode> groupInviteCodes = groupInviteCodeRepository
        .findAllByCreatedAtBefore(twoDaysAgo);

    groupInviteCodeRepository.deleteAll(groupInviteCodes);
  }
}
