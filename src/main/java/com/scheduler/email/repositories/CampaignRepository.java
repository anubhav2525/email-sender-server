package com.scheduler.email.repositories;

import com.scheduler.email.data.entities.Campaign;
import com.scheduler.email.data.enums.CampaignStatus;
import com.scheduler.email.data.enums.CampaignType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, UUID> {
    List<Campaign> findByUserId(UUID userId);

    // Status filter: idx_campaigns_user_status
    List<Campaign> findByUserIdAndStatus(UUID userId, CampaignStatus status);

    // Scheduler: idx_campaigns_running — sirf RUNNING campaigns
    @Query("SELECT c FROM Campaign c WHERE c.status = 'RUNNING'")
    List<Campaign> findAllRunning();

    // Future scheduling: idx_campaigns_scheduled
    @Query("SELECT c FROM Campaign c WHERE c.status = 'DRAFT' AND c.scheduledStartAt IS NOT NULL AND c.scheduledStartAt <= :now")
    List<Campaign> findDraftsDueToStart(OffsetDateTime now);

    // Mail account load check: idx_campaigns_mail_account
    List<Campaign> findByMailAccountIdAndStatus(UUID mailAccountId, CampaignStatus status);

    // Profile campaigns: idx_campaigns_profile_id
    List<Campaign> findByProfileId(UUID profileId);

    boolean existsByUserIdAndName(UUID userId, String name);

    Optional<Campaign> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByIdAndUserId(UUID campaignId, UUID userId);

    List<Campaign> findByUserIdAndType(UUID userId, CampaignType type);
}
