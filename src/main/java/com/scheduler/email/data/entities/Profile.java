package com.scheduler.email.data.entities;

import com.scheduler.email.data.entities.auth.AppUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "profiles",
        uniqueConstraints = @UniqueConstraint(name = "uq_profiles_user", columnNames = "user_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "User is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_profiles_user"))
    private AppUser user;

    @NotBlank(message = "First name is required")
    @Size(max = 150)
    @Column(name = "first_name", nullable = false, length = 150)
    private String firstName;

    @Size(max = 150)
    @Column(name = "middle_name", length = 150)
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Size(max = 150)
    @Column(name = "last_name", nullable = false, length = 150)
    private String lastName;

    @NotBlank(message = "Phone is required")
    @Size(max = 20)
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Email(message = "Invalid email format")
    @Size(max = 255)
    @Column(name = "email")
    private String email;

    @Size(max = 200)
    @Column(name = "organization", length = 200)
    private String organization;

    @Size(max = 300)
    @Column(name = "website", length = 300)
    private String website;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime updatedAt;
}
