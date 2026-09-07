package com.scheduler.email.data.entities;

import com.scheduler.email.data.entities.auth.AppUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "unsubscribes",
        uniqueConstraints = @UniqueConstraint(name = "uq_unsub_user_email",
                columnNames = {"user_id", "email"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unsubscribe {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_unsub_user"))
    private AppUser user;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "unsubscribed_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime unsubscribedAt = OffsetDateTime.now();
}
