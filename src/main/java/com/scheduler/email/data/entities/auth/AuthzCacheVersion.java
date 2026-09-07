package com.scheduler.email.data.entities.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "authz_cache_version")
@Getter
@Setter
public class AuthzCacheVersion {
    @Id
    @Column(name = "key", length = 50)
    private String key;

    @Column(nullable = false)
    private Long version;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
