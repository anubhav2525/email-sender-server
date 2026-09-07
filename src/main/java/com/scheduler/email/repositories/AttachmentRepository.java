package com.scheduler.email.repositories;

import com.scheduler.email.data.entities.Attachment;
import com.scheduler.email.data.enums.AttachmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    List<Attachment> findByUserId(UUID userId);

    // Type-wise filter: idx_attachments_user_type
    List<Attachment> findByUserIdAndType(UUID userId, AttachmentType type);

    // idx_attachments_inline — logos/banners for email rendering
    List<Attachment> findByUserIdAndIsInlineTrue(UUID userId);

    boolean existsByIdAndUserId(UUID attachmentId, UUID userId);

    Optional<Attachment> findByIdAndUserId(UUID id, UUID userId);

    void deleteByIdAndUserId(UUID id, UUID userId);
}
