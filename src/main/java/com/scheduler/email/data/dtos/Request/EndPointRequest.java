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
public class EndPointRequest {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Search {
        private String keyword;
        private String httpMethod;
        private Boolean isPublic;
        private Boolean enabled;

        @Min(0)
        @Builder.Default
        private Integer page = 0;

        @Min(1)
        @Max(100)
        @Builder.Default
        private Integer size = 20;

        @Builder.Default
        private String sortBy = "createdAt";

        @Pattern(regexp = "^(asc|desc)$")
        @Builder.Default
        private String sortDir = "desc";
    }
}
