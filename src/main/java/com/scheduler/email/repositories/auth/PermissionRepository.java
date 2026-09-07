package com.scheduler.email.repositories.auth;

import com.scheduler.email.data.entities.auth.Permissions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permissions, UUID> {

    Optional<Permissions> findByIdAndDeletedFalse(UUID id);

    List<Permissions> findAllByDeletedFalse();

    Page<Permissions> findAllByDeletedFalse(Pageable pageable);

    Page<Permissions> findAllByEnabledAndDeletedFalse(
            Boolean enabled,
            Pageable pageable
    );

    @Query("""
                SELECT p FROM Permissions p
                WHERE p.deleted = false
                AND (
                    LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.resource) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.action) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
            """)
    Page<Permissions> search(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
                SELECT p FROM Permissions p
                WHERE p.deleted = false
                AND p.enabled = true
            """)
    Page<Permissions> findAllByDeletedFalseAndEnabledTrue(
            Pageable pageable
    );

    @Query("""
                SELECT DISTINCT p.name
                FROM Permissions p
                JOIN p.roles r
                JOIN r.users u
                WHERE u.id = :userId
                AND u.enabled = true
                AND r.enabled = true
                AND p.enabled = true
                AND p.deleted = false
            """)
    Set<String> findPermissionNamesByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("""
                UPDATE Permissions p
                SET p.deleted = true,
                    p.enabled = false
                WHERE p.id = :id
            """)
    void softDelete(@Param("id") UUID id);

    @Query(value = """
            SELECT
              COUNT(*) FILTER (WHERE deleted = false)                     AS totalPermissions,
              COUNT(*) FILTER (WHERE enabled = true  AND deleted = false) AS activePermissions,
              COUNT(*) FILTER (WHERE enabled = false AND deleted = false) AS disabledPermissions,
              COUNT(*) FILTER (WHERE deleted = true)                      AS deletedPermissions
            FROM permissions
            """, nativeQuery = true)
    PermissionStatsProjection getPermissionStats();

    interface PermissionStatsProjection {
        Long getTotalPermissions();

        Long getActivePermissions();

        Long getDisabledPermissions();

        Long getDeletedPermissions();
    }
}
