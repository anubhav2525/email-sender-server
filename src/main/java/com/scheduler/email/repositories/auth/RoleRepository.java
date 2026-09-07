package com.scheduler.email.repositories.auth;

import com.scheduler.email.data.entities.auth.Roles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Roles, UUID> {
    // =========================================================
    // BASIC
    // =========================================================

    Optional<Roles> findByIdAndDeletedFalse(UUID id);

    Optional<Roles> findByNameAndDeletedFalse(String name);

    boolean existsByName(String name);

    // =========================================================
    // FETCH OPTIMIZATION
    // =========================================================

    @EntityGraph(attributePaths = {
            "permissions"
    })
    @Query("""
                SELECT r
                FROM Roles r
                WHERE r.id = :id
                AND r.deleted = false
            """)
    Optional<Roles> findWithPermissions(@Param("id") UUID id);

    @EntityGraph(attributePaths = {
            "permissions",
            "users"
    })
    @Query("""
                SELECT DISTINCT r
                FROM Roles r
                WHERE r.deleted = false
            """)
    Page<Roles> findAllWithRelations(Pageable pageable);

    @EntityGraph(attributePaths = {
            "permissions",
            "users"
    })
    @Query("""
                SELECT DISTINCT r
                FROM Roles r
            """)
    List<Roles> findAllWithPermissionsAndUsers();

    // =========================================================
    // SEARCH
    // =========================================================

    @Query("""
                SELECT r
                FROM Roles r
                WHERE r.deleted = false
                AND (
                    LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(r.displayName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
            """)
    Page<Roles> search(
            @Param("keyword") String keyword,
            Pageable pageable
    );

// =========================================================
// ANALYTICS
// =========================================================

    @Query(value = """
            SELECT
              COUNT(*) FILTER (WHERE deleted = false)                     AS totalRoles,
              COUNT(*) FILTER (WHERE enabled = true  AND deleted = false) AS activeRoles,
              COUNT(*) FILTER (WHERE enabled = false AND deleted = false) AS disabledRoles,
              COUNT(*) FILTER (WHERE deleted = true)                      AS deletedRoles
            FROM roles
            """, nativeQuery = true)
    RoleStatsProjection getRoleStats();

    interface RoleStatsProjection {
        Long getTotalRoles();

        Long getActiveRoles();

        Long getDisabledRoles();

        Long getDeletedRoles();
    }

    // =========================================================
    // SOFT DELETE
    // =========================================================

    @Modifying
    @Query("""
                UPDATE Roles r
                SET r.deleted = true,
                    r.enabled = false
                WHERE r.id = :id
            """)
    void softDelete(@Param("id") UUID id);


    // loads all users
    @Query("""
                SELECT COUNT(u)
                FROM AppUser u
                JOIN u.roles r
                WHERE r.id = :roleId
                  AND u.deleted = false
            """)
    long countUsersByRoleId(@Param("roleId") UUID roleId);

    // loads all permissions
    @Query("""
                SELECT COUNT(p)
                FROM Permissions p
                JOIN p.roles r
                WHERE r.id = :roleId
                  AND p.deleted = false
            """)
    long countPermissionsByRoleId(@Param("roleId") UUID roleId);
}
