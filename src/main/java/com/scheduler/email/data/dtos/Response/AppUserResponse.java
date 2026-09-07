package com.scheduler.email.data.dtos.Response;

import com.scheduler.email.data.entities.auth.AppUser;
import com.scheduler.email.data.entities.auth.Roles;
import lombok.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AppUserResponse {
    private final ModelMapper modelMapper;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Auth {
        private String accessToken;
        private String refreshToken;
        @Builder.Default
        private String tokenType = "Bearer";
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private long totalUsers;
        private long activeUsers;
        private long disabledUsers;
        private long deletedUsers;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private UUID id;
        private String fullName;
        private String phone;
        private String email;
        private String profileUrl;
        private long roles;
        private boolean activeStatus;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {
        private UUID id;
        private String fullName;
        private String stdCode;
        private String phone;
        private String email;
        private String profileUrl;
        private Set<Roles> roles;
        private boolean activeStatus;
        private OffsetDateTime updatedAt;
        private OffsetDateTime lastActiveAt;
    }

    public Detail toDetail(AppUser appUser) {
        return modelMapper.map(appUser, Detail.class);
    }

    public Summary toSummary(AppUser appUser) {
        return modelMapper.map(appUser, Summary.class);
    }

}
