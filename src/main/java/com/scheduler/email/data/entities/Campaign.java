package com.scheduler.email.data.entities;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.scheduler.email.data.entities.auth.AppUser;
import com.scheduler.email.data.enums.CampaignStatus;
import com.scheduler.email.data.enums.CampaignType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
        name = "campaigns",
        uniqueConstraints = @UniqueConstraint(name = "uq_campaigns_name_user",
                columnNames = {"user_id", "name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_campaigns_user"))
    private AppUser user;

    @NotNull(message = "Profile is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_campaigns_profile"))
    private Profile profile;

    @NotNull(message = "Mail account is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mail_account_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_campaigns_mail_account"))
    private MailAccount mailAccount;

    // Optional — SET NULL on delete
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id",
            foreignKey = @ForeignKey(name = "fk_campaigns_template"))
    private EmailTemplate template;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "scheduled_start_at", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime scheduledStartAt;

    @NotBlank(message = "Timezone is required")
    @Size(max = 50)
    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone = "Asia/Kolkata";

    @Column(name = "completed_at", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime completedAt;

    @NotBlank(message = "Campaign name is required")
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @NotNull(message = "Campaign type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "campaign_type")
    private CampaignType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "campaign_status")
    private CampaignStatus status = CampaignStatus.DRAFT;

    // Only for INDIVIDUAL campaigns
    @Size(max = 500)
    @Column(name = "custom_subject", length = 500)
    private String customSubject;

    @Column(name = "custom_body", columnDefinition = "TEXT")
    private String customBody;

    @Min(value = 1)
    @Column(name = "interval_min_minutes", nullable = false)
    private int intervalMinMinutes = 4;

    @Min(value = 1)
    @Max(value = 60)
    @Column(name = "interval_max_minutes", nullable = false)
    private int intervalMaxMinutes = 7;

    // M:N with attachments
    @ManyToMany
    @JoinTable(
            name = "campaign_attachments",
            joinColumns = @JoinColumn(name = "campaign_id"),
            inverseJoinColumns = @JoinColumn(name = "attachment_id"),
            foreignKey = @ForeignKey(name = "fk_ca_campaign"),
            inverseForeignKey = @ForeignKey(name = "fk_ca_attachment")
    )
    @Builder.Default
    private Set<Attachment> attachments = new HashSet<>();

    // M:N with recipients
    @ManyToMany
    @JoinTable(
            name = "campaign_recipients",
            joinColumns = @JoinColumn(name = "campaign_id"),
            inverseJoinColumns = @JoinColumn(name = "recipient_id"),
            foreignKey = @ForeignKey(name = "fk_cr_campaign"),
            inverseForeignKey = @ForeignKey(name = "fk_cr_recipient")
    )
    @Builder.Default
    private Set<Recipient> recipients = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime updatedAt;
}
