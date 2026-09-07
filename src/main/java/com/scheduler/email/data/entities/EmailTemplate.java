package com.scheduler.email.data.entities;

import com.scheduler.email.data.entities.auth.AppUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "email_templates",
        uniqueConstraints = @UniqueConstraint(name = "uq_templates_user_name",
                columnNames = {"user_id", "name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_templates_user"))
    private AppUser user;

    @NotBlank(message = "Template name is required")
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @NotBlank(message = "Subject template is required")
    @Size(max = 500)
    @Column(name = "subject_template", nullable = false, length = 500)
    private String subjectTemplate;

    @NotBlank(message = "Body template is required")
    @Column(name = "body_template", nullable = false, columnDefinition = "TEXT")
    private String bodyTemplate;

    @Size(max = 200)
    @Column(name = "preview_text", length = 200)
    private String previewText;

    @Size(max = 100)
    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "is_html", nullable = false)
    private boolean isHtml = true;

    // M:N with attachments
    @ManyToMany
    @JoinTable(
            name = "template_attachments",
            joinColumns = @JoinColumn(name = "template_id"),
            inverseJoinColumns = @JoinColumn(name = "attachment_id"),
            foreignKey = @ForeignKey(name = "fk_ta_template"),
            inverseForeignKey = @ForeignKey(name = "fk_ta_attachment")
    )
    @Builder.Default
    private Set<Attachment> attachments = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime updatedAt;
}
