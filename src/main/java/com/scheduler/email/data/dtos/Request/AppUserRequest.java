package com.scheduler.email.data.dtos.Request;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

public class AppUserRequest {

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class Create {

        @NotBlank(message = "Full name is required")
        @Size(max = 150)
        private String fullName;

        @Size(max = 4)
        private String stdCode = "+91";

        @NotBlank(message = "Phone is required")
        @Size(max = 15)
        private String phone;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be 8–100 characters")
        private String password;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class Login {

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class ForgetPassword {

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class Verify {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Token is required")
        @Size(min = 6, max = 6, message = "Token must be 6 characters")
        private String verificationToken;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class ResetPassword {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be 8–100 characters")
        private String newPassword;

        @NotBlank(message = "Token is required")
        @Size(min = 6, max = 6, message = "Token must be 6 characters")
        private String verificationToken;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class ChangePassword {
        @NotBlank(message = "Old Password is required")
        @Size(min = 8, max = 100, message = "Old Password must be 8–100 characters")
        private String oldPassword;

        @NotBlank(message = "New Password is required")
        @Size(min = 8, max = 100, message = "New Password must be 8–100 characters")
        private String newPassword;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class ProfileImage {
        MultipartFile image;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class Update {
        @NotBlank(message = "Full name is required")
        @Size(max = 150)
        private String fullName;

        @Size(max = 4)
        private String stdCode = "+91";

        @NotBlank(message = "Phone is required")
        @Size(max = 15)
        private String phone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Search {
        private String keyword;
        private String roleName;
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

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class ApproveAccount {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class Roles {
        private Set<UUID> roleIds;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class RejectAccount {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Reason is required")
        private String rejectReason;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class ResendVerificationCode {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class RemindAccountVerification {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingAccount {
        @Builder.Default
        private Integer page = 0;
        @Builder.Default
        private Integer size = 20;
        @Builder.Default
        private String sortBy = "createdAt";
        @Builder.Default
        private String sortDir = "desc";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefreshToken {
        @NotBlank(message = "Refresh token is required")
        private String refreshToken;
    }
}
