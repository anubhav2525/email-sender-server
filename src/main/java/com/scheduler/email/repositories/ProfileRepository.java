package com.scheduler.email.repositories;

import com.scheduler.email.data.entities.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    Optional<Profile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    List<Profile> findByOrganizationIgnoreCase(String organization);

    List<Profile> findByOrganizationContainingIgnoreCase(String keyword);
}
