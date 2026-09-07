package com.scheduler.email.repositories;

import com.scheduler.email.data.entities.EmailJob;
import com.scheduler.email.data.enums.JobStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailJobRepository extends JpaRepository<EmailJob, UUID> {
    List<EmailJob> findByCampaignId(UUID campaignId);

    List<EmailJob> findByCampaignIdAndStatus(UUID campaignId, JobStatus status);

    Optional<EmailJob> findByCampaignIdAndRecipientId(UUID campaignId, UUID recipientId);

    // Most critical: idx_jobs_scheduler
    // SELECT * FROM email_jobs WHERE status = 'PENDING' AND scheduled_at <= now() ORDER BY scheduled_at ASC LIMIT n
    @Query("SELECT j FROM EmailJob j WHERE j.status = 'PENDING' AND j.scheduledAt <= :now ORDER BY j.scheduledAt ASC")
    List<EmailJob> findPendingJobsDue(OffsetDateTime now, Pageable pageable);

    // Retry queue: idx_jobs_retry — FAILED with retryCount < 3
    @Query("SELECT j FROM EmailJob j WHERE j.status = 'FAILED' AND j.retryCount < 3 AND j.campaign.id = :campaignId")
    List<EmailJob> findRetryableJobs(UUID campaignId);

    // Campaign progress stats
    long countByCampaignIdAndStatus(UUID campaignId, JobStatus status);

    // Recipient ke saare jobs (reverse lookup)
    List<EmailJob> findByRecipientId(UUID recipientId);

    boolean existsByCampaignIdAndRecipientId(UUID campaignId, UUID recipientId);
}
