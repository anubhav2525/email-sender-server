package com.scheduler.email.data.dtos.Response;

import com.scheduler.email.data.entities.auth.EndpointPermission;
import com.scheduler.email.services.impl.EndpointsPermissionService;
import lombok.*;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EndPointResponse {
    private final PermissionsResponse permissionsResponse;
    private final EndpointsPermissionService endpointsPermissionService;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private UUID id;
        private String httpMethod;
        private String pathPattern;
        private String permission;
        private Boolean isPublic;
        private Boolean enabled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {
        private UUID id;
        private String httpMethod;
        private String pathPattern;
        private PermissionsResponse.Summary permission;
        private Boolean isPublic;
        private Boolean enabled;
        private String description;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private long totalEndpoints;      // Total (deleted = false)
        private long publicEndpoints;     // isPublic = true AND deleted = false
        private long privateEndpoints;    // isPublic = false AND deleted = false
        private long activeEndpoints;     // enabled = true AND deleted = false
        private long disabledEndpoints;   // enabled = false AND deleted = false
        private long deletedEndpoints;    // deleted = true
    }

    public Summary toSummary(EndpointPermission endpointPermission) {
        UUID permissionId = endpointPermission.getPermission() == null ?
                null :
                endpointPermission.getPermission().getId();

        return Summary.builder()
                .id(endpointPermission.getId())
                .httpMethod(endpointPermission.getHttpMethod())
                .pathPattern(endpointPermission.getPathPattern())
                .permission(endpointsPermissionService.getPermissionName(permissionId))
                .isPublic(endpointPermission.getIsPublic())
                .enabled(endpointPermission.getEnabled())
                .build();
    }

    public Detail toDetail(EndpointPermission endpointPermission) {
        PermissionsResponse.Summary permission = endpointPermission.getPermission() == null ?
                null :
                permissionsResponse.toSummary(endpointPermission.getPermission());

        return Detail.builder()
                .id(endpointPermission.getId())
                .httpMethod(endpointPermission.getHttpMethod())
                .pathPattern(endpointPermission.getPathPattern())
                .permission(permission)
                .isPublic(endpointPermission.getIsPublic())
                .enabled(endpointPermission.getEnabled())
                .description(endpointPermission.getDescription())
                .createdAt(endpointPermission.getCreatedAt())
                .updatedAt(endpointPermission.getUpdatedAt())
                .build();
    }
}
