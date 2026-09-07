package com.scheduler.email.repositories.auth;

import com.scheduler.email.data.entities.auth.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID>, JpaSpecificationExecutor<AppUser> {
    Optional<AppUser> findByEmailAndDeletedFalse(String email);

    boolean existsByEmailAndDeletedFalse(String email);

    Optional<AppUser> findByEmailAndDeletedFalseAndIsActiveTrue(String email);

    List<AppUser> findByDeletedFalseAndIsActiveTrueAndEnabledTrue();

    Optional<AppUser> findByRefreshTokenHash(String refreshTokenHash);

    List<AppUser> findByLastActiveAtAfter(OffsetDateTime since);

    @Modifying
    @Query("UPDATE AppUser u SET u.deleted = true, u.isActive = false WHERE u.id = :id")
    void softDeleteById(UUID id);

    @Modifying
    @Query("UPDATE AppUser u SET u.refreshTokenHash = null, u.refreshTokenExpiresAt = null WHERE u.id = :id")
    void clearRefreshToken(UUID id);

    @Query("""
                SELECT DISTINCT u FROM AppUser u
                LEFT JOIN FETCH u.roles r
                LEFT JOIN FETCH r.permissions p
                WHERE u.email = :email
                AND u.deleted = false
            """)
    Optional<AppUser> findSecurityUserByEmailAndDeletedFalse(@Param("email") String email);

    /**
     * Used during JWT refresh — validates token hash before issuing a new pair.
     */
    @Query("""
            SELECT u FROM AppUser u
            WHERE u.refreshTokenHash = :tokenHash
              AND u.refreshTokenExpiresAt > :now
              AND u.enabled = true
            """)
    Optional<AppUser> findByValidRefreshToken(
            @Param("tokenHash") String tokenHash,
            @Param("now") LocalDateTime now
    );

    // ─── Stats Optimization ───────────────────────────────────────────────────
    @Query(value = """
            SELECT
              COUNT(*) FILTER (WHERE deleted = false)                     AS totalUsers,
              COUNT(*) FILTER (WHERE enabled = true  AND deleted = false) AS activeUsers,
              COUNT(*) FILTER (WHERE enabled = false AND deleted = false) AS disabledUsers,
              COUNT(*) FILTER (WHERE deleted = true)                      AS deletedUsers
            FROM AppUser
            """, nativeQuery = true)
    UserStatsProjection getUserStats();

    interface UserStatsProjection {
        Long getTotalUsers();

        Long getActiveUsers();

        Long getDisabledUsers();

        Long getDeletedUsers();
    }

    @Modifying
    @Query("""
            UPDATE AppUser u
            SET u.refreshTokenHash       = :tokenHash,
                u.refreshTokenExpiresAt  = :expiresAt,
                u.updatedAt              = :now
            WHERE u.id = :id
            """)
    void saveRefreshToken(
            @Param("id") UUID id,
            @Param("tokenHash") String tokenHash,
            @Param("expiresAt") OffsetDateTime expiresAt,
            @Param("now") OffsetDateTime now
    );

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    @Query("""
            SELECT DISTINCT u FROM AppUser u
            LEFT JOIN FETCH u.roles r
            LEFT JOIN FETCH r.permissions
            WHERE u.id = :id
            """)
    Optional<AppUser> findByIdWithRolesAndPermissions(@Param("id") UUID id);

}
