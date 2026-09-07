package com.scheduler.email.repositories.auth;

import com.scheduler.email.data.entities.auth.EndpointPermission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EndpointPermissionRepository extends JpaRepository<EndpointPermission, UUID>,
        JpaSpecificationExecutor<EndpointPermission> {

    @EntityGraph(attributePaths = "permission")
    Optional<EndpointPermission> findByIdAndDeletedFalse(UUID id);

    Optional<EndpointPermission> findByHttpMethodAndPathPatternAndDeletedFalse(String httpMethod, String pathPattern);

    @EntityGraph(attributePaths = "permission")
    @Query("""
            SELECT ep
            FROM EndpointPermission ep
            WHERE ep.deleted = false
            ORDER BY ep.httpMethod, ep.pathPattern
            """)
    List<EndpointPermission> findAllActivePolicies();
    // =========================================================
    // ANALYTICS
    // =========================================================

    @Query(value = """
            SELECT
              COUNT(*) FILTER (WHERE deleted = false)                          AS totalEndpoints,
              COUNT(*) FILTER (WHERE is_public = true  AND deleted = false)    AS publicEndpoints,
              COUNT(*) FILTER (WHERE is_public = false AND deleted = false)    AS privateEndpoints,
              COUNT(*) FILTER (WHERE enabled = true    AND deleted = false)    AS activeEndpoints,
              COUNT(*) FILTER (WHERE enabled = false   AND deleted = false)    AS disabledEndpoints,
              COUNT(*) FILTER (WHERE deleted = true)                           AS deletedEndpoints
            FROM endpoint_permissions
            """, nativeQuery = true)
    EndpointStatsProjection getEndpointStats();

    interface EndpointStatsProjection {
        Long getTotalEndpoints();

        Long getPublicEndpoints();

        Long getPrivateEndpoints();

        Long getActiveEndpoints();

        Long getDisabledEndpoints();

        Long getDeletedEndpoints();
    }
}
