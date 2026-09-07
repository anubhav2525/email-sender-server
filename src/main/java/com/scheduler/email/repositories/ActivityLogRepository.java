package com.scheduler.email.repositories;

import com.scheduler.email.data.entities.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    // idx_al_user_id — user ki activity history
    Page<ActivityLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    // idx_al_entity — entity audit trail
    List<ActivityLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType, UUID entityId);

    // idx_al_action — saare CAMPAIGN_STARTED events
    List<ActivityLog> findByActionOrderByCreatedAtDesc(String action);

    List<ActivityLog> findByActionAndCreatedAtBetweenOrderByCreatedAtDesc(
            String action, OffsetDateTime from, OffsetDateTime to);

    // Recent logs (paginated)
    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
