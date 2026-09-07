package com.scheduler.email.data.dtos.Request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class PermissionRequest {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Search {
        private String name;

        private Boolean enabled;

        private Boolean deleted;

        @Min(value = 0, message = "Page index must be 0 or greater")
        @Builder.Default
        private Integer page = 0;

        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size must not exceed 100")
        @Builder.Default
        private Integer size = 20;

        @Builder.Default
        private String sortBy = "createdAt";

        @Pattern(regexp = "^(asc|desc)$", message = "Sort direction must be 'asc' or 'desc'")
        @Builder.Default
        private String sortDir = "desc";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllActive {
        @Min(value = 0, message = "Page index must be 0 or greater")
        @Builder.Default
        private Integer page = 0;

        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size must not exceed 100")
        @Builder.Default
        private Integer size = 20;

        @Builder.Default
        private String sortBy = "createdAt";

        @Pattern(regexp = "^(asc|desc)$", message = "Sort direction must be 'asc' or 'desc'")
        @Builder.Default
        private String sortDir = "desc";
    }
}
