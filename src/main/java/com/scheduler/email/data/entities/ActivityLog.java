package com.scheduler.email.data.entities;

import com.scheduler.email.data.entities.auth.AppUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_al_user"))
    private AppUser user;

    @NotBlank(message = "Action is required")
    @Size(max = 100)
    @Column(name = "action", nullable = false, length = 100)
    private String action;
    // e.g. CAMPAIGN_STARTED, MAIL_ACCOUNT_ADDED, RECIPIENT_IMPORTED

    @Size(max = 50)
    @Column(name = "entity_type", length = 50)
    private String entityType;
    // e.g. CAMPAIGN, RECIPIENT, MAIL_ACCOUNT, TEMPLATE

    @Column(name = "entity_id")
    private UUID entityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;
}
