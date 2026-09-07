package com.scheduler.email.repositories;

import com.scheduler.email.data.entities.Unsubscribe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UnsubscribeRepository extends JpaRepository<Unsubscribe, UUID> {
    List<Unsubscribe> findByUserId(UUID userId);

    Optional<Unsubscribe> findByUserIdAndEmail(UUID userId, String email);

    boolean existsByUserIdAndEmail(UUID userId, String email);

    List<Unsubscribe> findByUserIdAndEmailIn(UUID userId, List<String> emails);
}
