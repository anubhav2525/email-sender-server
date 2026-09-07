package com.scheduler.email.data.dtos.Response;

import com.scheduler.email.data.entities.auth.Permissions;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionsResponse {
    private final ModelMapper modelMapper;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {
        private UUID id;
        private String name;
        private String resource;
        private String action;
        private Boolean enabled;
        private int assignedRoleCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {
        private UUID id;
        private String name;
        private String resource;
        private String action;
        private String description;
        private int assignedRoleCount;
        private Boolean enabled;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private long totalPermissions;
        private long activePermissions;
        private long disabledPermissions;
        private long deletedPermissions;
    }

    public PermissionsResponse.Summary toSummary(Permissions permissions) {
        Summary map = modelMapper.map(permissions, Summary.class);
        map.setAssignedRoleCount(permissions.getRoles() == null ? 0 : permissions.getRoles().size());
        log.info("Permission summary setails: {}", map.getName());
        log.info("{}", map);
        return map;
    }

    public PermissionsResponse.Detail toDetail(Permissions permissions) {
        PermissionsResponse.Detail detail = modelMapper.map(permissions, PermissionsResponse.Detail.class);
        detail.setAssignedRoleCount(permissions.getRoles() == null ? 0 : permissions.getRoles().size());
        log.info("Permission details: {}", detail.getName());
        log.info("{}", detail);
        return detail;
    }
}
