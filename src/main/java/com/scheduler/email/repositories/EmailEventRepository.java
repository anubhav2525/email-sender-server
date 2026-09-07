package com.scheduler.email.repositories;

import com.scheduler.email.data.entities.EmailEvent;
import com.scheduler.email.data.enums.EmailEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmailEventRepository extends JpaRepository<EmailEvent, UUID> {

    // idx_ee_job_id — job ke saare events
    List<EmailEvent> findByJobId(UUID jobId);

    // idx_ee_job_event — event type breakdown per job
    List<EmailEvent> findByJobIdAndEventType(UUID jobId, EmailEventType eventType);

    // Time-range analytics: idx_ee_occurred_at
    List<EmailEvent> findByOccurredAtBetweenOrderByOccurredAtDesc(
            OffsetDateTime from, OffsetDateTime to);

    // Campaign-level analytics: JOIN via email_jobs
    @Query("""
                SELECT e FROM EmailEvent e
                JOIN e.job j
                WHERE j.campaign.id = :campaignId
            """)
    List<EmailEvent> findByCampaignId(UUID campaignId);

    // Campaign analytics: event type count per campaign
    @Query("""
                SELECT e.eventType, COUNT(e)
                FROM EmailEvent e
                JOIN e.job j
                WHERE j.campaign.id = :campaignId
                GROUP BY e.eventType
            """)
    List<Object[]> countEventsByCampaignGrouped(UUID campaignId);

    // Is week total opens/clicks
    @Query("""
                SELECT e.eventType, COUNT(e)
                FROM EmailEvent e
                WHERE e.occurredAt BETWEEN :from AND :to
                GROUP BY e.eventType
            """)
    List<Object[]> countEventsByTypeInRange(OffsetDateTime from, OffsetDateTime to);
}
