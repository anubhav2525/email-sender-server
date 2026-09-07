package com.scheduler.email.repositories;

import com.scheduler.email.data.entities.ProfileLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileLinkRepository extends JpaRepository<ProfileLink, UUID> {
    List<ProfileLink> findByProfileIdOrderBySortOrderAsc(UUID profileId);

    List<ProfileLink> findByProfileIdAndIsVisibleTrueOrderBySortOrderAsc(UUID profileId);

    boolean existsByProfileIdAndLabel(UUID profileId, String label);

    Optional<ProfileLink> findByProfileIdAndLabel(UUID profileId, String label);

    void deleteByProfileId(UUID profileId);
}
