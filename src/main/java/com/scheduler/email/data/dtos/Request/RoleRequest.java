package com.scheduler.email.data.dtos.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class RoleRequest {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Create {
        @NotBlank
        private String name;
        @NotBlank
        private String displayName;
        private String description;
        private Set<UUID> permissionIds;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Update {
        private String name;
        private String displayName;
        private String description;
        private Set<UUID> permissionIds;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdatePermissions {
        private Set<UUID> permissionIds;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Search {
        private String keyword;

        private Boolean enabled;

        @Builder.Default
        private Integer page = 0;

        @Builder.Default
        private Integer size = 20;

        @Builder.Default
        private String sortBy = "createdAt";

        @Builder.Default
        private String sortDir = "desc";
    }
}
