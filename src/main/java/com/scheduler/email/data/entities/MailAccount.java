package com.scheduler.email.data.entities;

import com.scheduler.email.data.entities.auth.AppUser;
import com.scheduler.email.data.enums.ProviderType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "mail_accounts",
        uniqueConstraints = @UniqueConstraint(name = "uq_mail_accounts_user_email",
                columnNames = {"user_id", "email"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_mail_accounts_user"))
    private AppUser user;

    @NotNull(message = "Provider is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, columnDefinition = "provider_type")
    private ProviderType provider;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @NotBlank(message = "App password is required")
    @Column(name = "app_password", nullable = false, columnDefinition = "TEXT")
    private String appPassword;

    @Size(max = 150)
    @Column(name = "display_name", length = 150)
    private String displayName;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Min(value = 0, message = "Daily sent count cannot be negative")
    @Column(name = "daily_sent_count", nullable = false)
    private int dailySentCount = 0;

    @Min(value = 1) @Max(value = 500)
    @Column(name = "daily_limit", nullable = false)
    private int dailyLimit = 150;

    @Min(value = 1) @Max(value = 50)
    @Column(name = "emails_per_batch", nullable = false)
    private int emailsPerBatch = 20;

    @Min(value = 10) @Max(value = 1440)
    @Column(name = "batch_cooldown_mins", nullable = false)
    private int batchCooldownMins = 60;

    @Column(name = "last_sent_at", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime lastSentAt;

    @Column(name = "cooldown_until", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime cooldownUntil;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime updatedAt;
}
