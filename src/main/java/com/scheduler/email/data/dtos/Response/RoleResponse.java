package com.scheduler.email.data.dtos.Response;

import com.scheduler.email.data.entities.auth.Roles;
import com.scheduler.email.repositories.auth.RoleRepository;
import lombok.*;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleResponse {
    private final PermissionsResponse permissionsResponse;
    private final RoleRepository roleRepository;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private UUID id;
        private String name;
        private String displayName;
        private Boolean enabled;
        private Long permissionCount;
        private Long userCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {
        private UUID id;
        private String name;
        private String displayName;
        private String description;
        private Boolean enabled;
        private Set<PermissionsResponse.Summary> permissions;
        private Long userCount;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private long totalRoles;
        private long activeRoles;
        private long disabledRoles;
        private long deletedRoles;
    }

    public Summary toSummary(Roles role) {
        Long permissionCount = roleRepository.countPermissionsByRoleId(role.getId());
        Long userCount = roleRepository.countUsersByRoleId(role.getId());
        return Summary.builder()
                .id(role.getId())
                .name(role.getName())
                .displayName(role.getDisplayName())
                .enabled(role.getEnabled())
                .permissionCount(permissionCount)
                .userCount(userCount)
                .build();
    }

    public Detail toDetail(Roles role) {
        Set<PermissionsResponse.Summary> permissions = role
                .getPermissions()
                .stream()
                .map(permissionsResponse::toSummary)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        long userCount = roleRepository.countUsersByRoleId(role.getId());

        return Detail.builder()
                .id(role.getId())
                .name(role.getName())
                .displayName(role.getDisplayName())
                .description(role.getDescription())
                .enabled(role.getEnabled())
                .permissions(permissions)
                .userCount(userCount > 0 ? userCount : 0L)
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}
