package com.scheduler.email.repositories;

import com.scheduler.email.data.entities.RecipientGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecipientGroupRepository extends JpaRepository<RecipientGroup, UUID> {
    List<RecipientGroup> findByUserId(UUID userId);

    Optional<RecipientGroup> findByUserIdAndName(UUID userId, String name);

    boolean existsByUserIdAndName(UUID userId, String name);

    boolean existsByIdAndUserId(UUID groupId, UUID userId);
}
