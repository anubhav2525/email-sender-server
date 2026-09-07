package com.scheduler.email.repositories;

import com.scheduler.email.data.entities.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, UUID> {

    List<EmailTemplate> findByUserId(UUID userId);

    // Category filter: idx_templates_user_category
    List<EmailTemplate> findByUserIdAndCategory(UUID userId, String category);

    List<EmailTemplate> findByUserIdAndCategoryIsNotNull(UUID userId);

    Optional<EmailTemplate> findByUserIdAndName(UUID userId, String name);

    boolean existsByUserIdAndName(UUID userId, String name);

    boolean existsByIdAndUserId(UUID templateId, UUID userId);
}
