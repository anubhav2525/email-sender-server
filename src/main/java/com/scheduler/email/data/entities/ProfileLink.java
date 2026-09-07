package com.scheduler.email.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "profile_links",
        uniqueConstraints = @UniqueConstraint(name = "uq_pl_profile_label",
                columnNames = {"profile_id", "label"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileLink {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Profile is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pl_profile"))
    private Profile profile;

    @NotBlank(message = "Label is required")
    @Size(max = 100, message = "Label must be at most 100 characters")
    @Column(name = "label", nullable = false, length = 100)
    private String label;

    @NotBlank(message = "URL is required")
    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "is_visible", nullable = false)
    private boolean isVisible = true;

    @Column(name = "sort_order", nullable = false)
    private short sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime updatedAt;
}
