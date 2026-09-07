package com.scheduler.email.repositories;

import com.scheduler.email.data.entities.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecipientRepository extends JpaRepository<Recipient, UUID> {
    List<Recipient> findByUserId(UUID userId);

    // Active recipients: idx_recipients_user_active
    List<Recipient> findByUserIdAndIsActiveTrue(UUID userId);

    // Name search: idx_recipients_name
    List<Recipient> findByUserIdAndNameContainingIgnoreCase(UUID userId, String name);

    Optional<Recipient> findByUserIdAndEmail(UUID userId, String email);

    boolean existsByUserIdAndEmail(UUID userId, String email);

    boolean existsByIdAndUserId(UUID recipientId, UUID userId);

    Optional<Recipient> findByIdAndUserId(UUID id, UUID userId);

    // JSONB custom_fields search — e.g. WHERE custom_fields @> '{"department": "HR"}'
    @Query(value = "SELECT * FROM recipients WHERE user_id = :userId AND custom_fields @> CAST(:filter AS jsonb)",
            nativeQuery = true)
    List<Recipient> findByUserIdAndCustomFieldsContaining(UUID userId, String filter);

    // Unsubscribe check: campaign bhejne se pehle
    @Query("SELECT r FROM Recipient r WHERE r.user.id = :userId AND r.email IN :emails AND r.isActive = true")
    List<Recipient> findActiveByUserIdAndEmailIn(UUID userId, List<String> emails);
}
