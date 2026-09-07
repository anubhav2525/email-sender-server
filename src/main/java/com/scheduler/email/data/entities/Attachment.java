package com.scheduler.email.data.entities;

import com.scheduler.email.data.entities.auth.AppUser;
import com.scheduler.email.data.enums.AttachmentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_attachments_user"))
    private AppUser user;

    @NotBlank(message = "File name is required")
    @Size(max = 255)
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @NotBlank(message = "File path is required")
    @Column(name = "file_path", nullable = false, columnDefinition = "TEXT")
    private String filePath;

    @Min(value = 0, message = "File size cannot be negative")
    @Column(name = "file_size", nullable = false)
    private long fileSize = 0L;

    @NotNull(message = "Attachment type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "attachment_type")
    private AttachmentType type;

    @Size(max = 100)
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "is_inline", nullable = false)
    private boolean isInline = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;
}
