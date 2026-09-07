package com.scheduler.email.data.entities;

import com.scheduler.email.data.enums.JobStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "email_jobs",
        uniqueConstraints = @UniqueConstraint(name = "uq_jobs_campaign_recipient",
                columnNames = {"campaign_id", "recipient_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Campaign is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_jobs_campaign"))
    private Campaign campaign;

    @NotNull(message = "Recipient is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_jobs_recipient"))
    private Recipient recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "job_status")
    private JobStatus status = JobStatus.PENDING;

    @Column(name = "scheduled_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime scheduledAt = OffsetDateTime.now();

    @Column(name = "sent_at", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime sentAt;

    @Min(0)
    @Max(3)
    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime updatedAt;
}
