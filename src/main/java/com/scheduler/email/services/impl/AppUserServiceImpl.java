package com.scheduler.email.services.impl;

import com.scheduler.email.common.CustomResponse;
import com.scheduler.email.common.PagedResponse;
import com.scheduler.email.data.dtos.Request.AppUserRequest;
import com.scheduler.email.data.dtos.Response.AppUserResponse;
import com.scheduler.email.data.entities.auth.AppUser;
import com.scheduler.email.data.entities.auth.Roles;
import com.scheduler.email.data.enums.AccountStatus;
import com.scheduler.email.exceptions.custom.*;
import com.scheduler.email.repositories.auth.AppUserRepository;
import com.scheduler.email.security.AuthorizationCacheService;
import com.scheduler.email.security.JwtService;
import com.scheduler.email.services.AppUserService;
import com.scheduler.email.services.SystemMailService;
import com.scheduler.email.utils.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.WebRequest;

import java.nio.file.AccessDeniedException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {
    private static final String RESOURCE = "User";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("fullName", "email", "phone", "createdAt", "updatedAt", "enabled");
    private static final String DEFAULT_SORT_FIELD = "createdAt";

    private static final String REDIS_OTP_PREFIX = "VERIFICATION_OTP:";
    private static final String REDIS_FAILED_ATTEMPT_BY_USER = "FAILED_USER_COUNT:";
    private static final String REDIS_USER_ACCOUNT_LOCKED = "LOCKED_USER:";
    private static final String REDIS_RESET_PASSWORD_OTP_PREFIX = "RESET_PASSWORD_OTP:";
    // This key is used in JwtAuthenticationFilter
    private static final String REDIS_BLACKLISTED_TOKEN = "BLACKLISTED_TOKEN:";

    private final JwtService jwtService;
    private final RedisService redisService;
    private final AuthorizationCacheService authorizationCacheService;
    private final CreateResponseEntity createResponseEntity;
    private final AppUserRepository userRepository;
    private final AppUserResponse userResponse;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;

    private final BuildPageable buildPageable;
    private final StringOperations stringOperations;
    private final SystemMailService systemMailService;
    private final UserInfoService userInfoService;

    private Specification<AppUser> buildSearchSpec(
            AppUserRequest.Search request) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));
            if (request.getEnabled() != null) predicates.add(cb.equal(root.get("enabled"), request.getEnabled()));
            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                String pattern = "%" + request.getKeyword().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(root.get("phone"), "%" + request.getKeyword().trim() + "%")
                ));
            }
            if (request.getRoleName() != null && !request.getRoleName().isBlank()) {
                Join<AppUser, Roles> roleJoin = root.join("roles");
                predicates.add(cb.equal(roleJoin.get("name"), request.getRoleName().trim().toUpperCase()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<AppUser> buildPendingAccountSpec(
            AppUserRequest.PendingAccount request) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));
            predicates.add(cb.equal(root.get("accountStatus"), AccountStatus.PENDING));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private AppUser getUser(UUID id) {
        return userRepository.findByIdWithRolesAndPermissions(id)
                .orElseThrow(() -> new ResourceNotExistsException(RESOURCE + " not found with id: " + id));
    }

    private AppUser getUser(String email) {
        return userRepository.findByEmailAndDeletedFalse(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotExistsException(RESOURCE + " not found with email: " + email));
    }

    private AppUserResponse.Auth buildAuth(AppUser user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AppUserResponse.Auth.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void saveRefreshToken(
            AppUser user, String refreshToken) {
        userRepository.saveRefreshToken(
                user.getId(),
                jwtService.hashToken(refreshToken),
                OffsetDateTime.ofInstant(jwtService.refreshTokenExpiry(), ZoneId.systemDefault()),
                OffsetDateTime.now()
        );
    }

    private String redisOtpKey(String email) {
        return REDIS_OTP_PREFIX + email;
    }

    private String redisFailedAttemptKey(String email) {
        return REDIS_FAILED_ATTEMPT_BY_USER + email;
    }

    private String redisUserAccountLockedKey(String email) {
        return REDIS_USER_ACCOUNT_LOCKED + email;
    }

    private String redisResetPasswordOtpKey(String email) {
        return REDIS_RESET_PASSWORD_OTP_PREFIX + email;
    }

    private String redisBlacklistKey(String token) {
        return REDIS_BLACKLISTED_TOKEN + jwtService.hashToken(token);
    }

    private AppUser createUserEntity(
            AppUserRequest.Create request, Set<UUID> roleIds) {
        String email = stringOperations.trimOrNull(request.getEmail().toLowerCase());
        String phone = stringOperations.trimOrNull(request.getPhone());
        String fullName = stringOperations.trimOrNull(request.getFullName());

        if (userRepository.existsByEmail(email)) {
            log.error(RESOURCE + " already exists with email: {}", email);
            throw new ResourceAlreadyExistsException(RESOURCE + " already exists with email: " + email);
        }

        if (userRepository.existsByPhone(phone)) {
            log.error(RESOURCE + " already exists with phone: {}", phone);
            throw new ResourceAlreadyExistsException(RESOURCE + " already exists with phone: " + phone);
        }

        return AppUser.builder()
                .fullName(fullName)
                .phone(phone)
                .stdCode(request.getStdCode())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(false)
                .deleted(false)
                .accountStatus(AccountStatus.PENDING)
                .isEmailVerified(false)
                .isActive(false)
                .profileUrl(null)
                .refreshTokenExpiresAt(null)
                .refreshTokenHash(null)
                .roles(userRoleService.resolveRolesOrDefault(roleIds))
                .build();
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<Void>> create(AppUserRequest.Create request, WebRequest webRequest) {
        Set<UUID> roleIds = new HashSet<>();
        AppUser user = createUserEntity(request, roleIds);

        // Generate random String for OTP & save into redis
        String verificationToken = stringOperations.generateRandomString(6);
        String redisKey = redisOtpKey(user.getEmail().toLowerCase().trim());

        // save user to DB
        userRepository.save(user);

        // send mail to user that the verification token will expire in 15 mins
        systemMailService.sendAccountCreationEmail(user.getEmail(), user.getEmail(), user.getFullName(), user.getPhone(), verificationToken);

        // Save token into redis for 15 minutes
        redisService.set(redisKey, verificationToken, Duration.ofMinutes(15));
        return createResponseEntity.buildResponse(
                RESOURCE + " created successfully, Please verify your account.",
                null,
                HttpStatus.CREATED,
                webRequest);
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<Void>> verifyUser(AppUserRequest.Verify request, WebRequest webRequest) {
        // Get token first from redis
        String redisKey = redisOtpKey(request.getEmail().toLowerCase().trim());
        Optional<Object> redisVerificationToken = redisService.get(redisKey);

        if (redisVerificationToken.isEmpty())
            return createResponseEntity.buildResponse(RESOURCE + " has no token store, Retry", null, HttpStatus.BAD_REQUEST, webRequest);

        if (!redisVerificationToken.get().equals(request.getVerificationToken()))
            throw new ResourceNotFound("In valid token, make sure to type valid token");

        AppUser user = getUser(request.getEmail());
        user.setEmailVerified(true);
        user.setActive(true);
        user.setEnabled(true);
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);

        systemMailService.sendWelcomeEmail(user.getEmail(), user.getFullName(), user.getFullName());
        redisService.delete(redisKey);

        return createResponseEntity.buildResponse("Email verification successfully completed", null, HttpStatus.OK, webRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<CustomResponse<Void>> resendVerificationCode(AppUserRequest.ResendVerificationCode resendVerificationCode, WebRequest webRequest) {
        AppUser user = getUser(resendVerificationCode.getEmail());
        if (user.isEmailVerified())
            throw new ResourceAlreadyEnabledException("Email is already verified");

        // redis storage
        String verificationToken = stringOperations.generateRandomString(6);
        String redisKey = redisOtpKey(user.getEmail());

        // send mail to user that the verification token will expire in 15 mins
        systemMailService.sendAccountCreationEmail(user.getEmail(), user.getEmail(), user.getFullName(), user.getPhone(), verificationToken);

        // Save token into redis for 15 minutes
        redisService.set(redisKey, verificationToken, Duration.ofMinutes(15));
        return createResponseEntity.buildResponse(
                "Verification code sent",
                null,
                HttpStatus.OK,
                webRequest);
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<AppUserResponse.Auth>> login(AppUserRequest.Login request, WebRequest webRequest) {
        String userEmail = request.getEmail().toLowerCase().trim();
        AppUser user = userRepository.findSecurityUserByEmailAndDeletedFalse(userEmail).orElseThrow(() -> new ResourceNotFound("Invalid email to login"));

        String lockedAccountKey = redisUserAccountLockedKey(userEmail);
        String failedAttemptKey = redisFailedAttemptKey(userEmail);

        // ── Step 1b: Account must be enabled and not soft-deleted ─────────────
        if (!user.isEnabled() || user.isDeleted()) {
            throw new ResourceOperationNotAllowed("User account is disabled");
        }

        // ── Step 1c: Email must be verified ───────────────────────────────────
        if (!user.isEmailVerified()) {
            throw new ResourceOperationNotAllowed(
                    "Email not verified. Please verify your account first.");
        }

        // ── Step 2: Check if account is locked in Redis ───────────────────────
        Optional<Object> lockedEntry = redisService.get(lockedAccountKey);
        if (lockedEntry.isPresent()) {
            throw new ResourceOperationNotAllowed(
                    "Account temporarily locked due to too many failed attempts. " +
                            "Try again after " + LOCK_MINUTES + " minutes.");
        }
        if (!passwordEncoder.matches(request.getPassword().trim(), user.getPassword())) {
            // Fetch current failed-attempt count from Redis (default 0)
            int attempts = redisService.get(failedAttemptKey)
                    .map(val -> Integer.parseInt(val.toString()))
                    .orElse(0);
            attempts++;

            // ── Step 5: Lock account when max attempts reached ────────────────
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                // Lock for 15 mins
                redisService.set(lockedAccountKey, true, Duration.ofMinutes(LOCK_MINUTES));

                // Clean up the counter — it's no longer needed
                redisService.delete(failedAttemptKey);

                // Notify the user by email
                systemMailService.sendAccountLockedEmail(
                        userEmail, user.getFullName(), String.valueOf(LOCK_MINUTES), "We noticed multiple unsuccessful login attempts on your account. For your security, your account has been temporarily locked.");

                throw new ResourceOperationNotAllowed(
                        "Account locked due to " + MAX_FAILED_ATTEMPTS +
                                " failed login attempts. Try again after " + LOCK_MINUTES + " minutes.");
            }
            // Save the updated counter — expires in 15 mins (same window as lock)
            redisService.set(failedAttemptKey, attempts, Duration.ofMinutes(LOCK_MINUTES));

            int remaining = MAX_FAILED_ATTEMPTS - attempts;
            throw new ResourceOperationNotAllowed(
                    "Invalid password. " + remaining +
                            " attempt(s) remaining before your account is locked.");
        }


        // ── Step 6: Password correct — reset counter & issue tokens ──────────
        redisService.delete(failedAttemptKey); // clear any leftover failed attempts

        AppUserResponse.Auth auth = buildAuth(user);
        saveRefreshToken(user, auth.getRefreshToken()); // persist hashed refresh token to DB

        // Update last active timestamp
        user.setLastActiveAt(OffsetDateTime.now());
        userRepository.save(user);
        return createResponseEntity.buildResponse(
                "Login successful",
                auth,
                HttpStatus.OK,
                webRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<CustomResponse<PagedResponse<AppUserResponse.Summary>>> pendingAccount(AppUserRequest.PendingAccount request, WebRequest webRequest) {
        Pageable pageable = buildPageable.build(
                request.getPage(), request.getSize(),
                request.getSortBy(), request.getSortDir(),
                ALLOWED_SORT_FIELDS, DEFAULT_SORT_FIELD);

        Page<AppUser> page = userRepository.findAll(buildPendingAccountSpec(request), pageable);
        return createResponseEntity.buildResponse("User account fetched", PagedResponse.of(page.map(userResponse::toSummary)), HttpStatus.OK, webRequest);
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<Void>> approveAccountByEmail(AppUserRequest.ApproveAccount request, WebRequest webRequest) {
        AppUser user = getUser(request.getEmail());

        if (!user.isEmailVerified())
            throw new ResourceValidationException(RESOURCE + " email is not verified yet, can't approve or reject account");

        if (user.getAccountStatus().equals(AccountStatus.APPROVE))
            throw new ResourceAlreadyExistsException(RESOURCE + " already verified");

        if (user.getAccountStatus().equals(AccountStatus.REJECT))
            throw new ResourceOperationNotAllowed(RESOURCE + " is rejected, cannot approve directly");

        user.setAccountStatus(AccountStatus.APPROVE);
        user.setEnabled(true);
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);

        systemMailService.sendAccountApproveEmail(user.getEmail(), user.getFullName());
        return createResponseEntity.buildResponse(RESOURCE + " account verified", null, HttpStatus.OK, webRequest);
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<Void>> rejectAccount(AppUserRequest.RejectAccount request, WebRequest webRequest) {
        AppUser user = getUser(request.getEmail());

        if (!user.isEmailVerified())
            throw new ResourceValidationException(RESOURCE + " email is not verified yet, can't approve or reject account");

        if (user.getAccountStatus().equals(AccountStatus.REJECT))
            throw new ResourceAlreadyExistsException(RESOURCE + " already rejected");

        if (user.getAccountStatus().equals(AccountStatus.APPROVE))
            throw new ResourceInUseException(RESOURCE + " already verified, can't reject");

        user.setAccountStatus(AccountStatus.REJECT);
        user.setRejectReason(request.getRejectReason().trim());
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);

        systemMailService.sendAccountRejectEmail(user.getEmail(), user.getFullName(), user.getRejectReason());
        return createResponseEntity.buildResponse(RESOURCE + " account rejected", null, HttpStatus.OK, webRequest);
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<Void>> remindToVerifyUserAccount(AppUserRequest.RemindAccountVerification remindAccountVerification, WebRequest webRequest) {
        AppUser user = getUser(remindAccountVerification.getEmail());
        if (user.isEmailVerified())
            throw new ResourceAlreadyEnabledException("User email already verified");

        if (user.getAccountStatus().equals(AccountStatus.REJECT))
            throw new ResourceNotRestoreException("User account rejected, user can't remind to verify");

        if (user.getAccountStatus().equals(AccountStatus.APPROVE))
            throw new ResourceNotRestoreException("User account approved, user can't remind to verify");

        systemMailService.sendRemindToVerifyAccount(user.getEmail(), user.getFullName());
        return createResponseEntity.buildResponse("Remind sent to verify", null, HttpStatus.OK, webRequest);
    }

    @Override
    public ResponseEntity<CustomResponse<AppUserResponse.Auth>> refreshToken(AppUserRequest.RefreshToken refreshToken, WebRequest webRequest) {
        return null;
    }

    @Override
    public ResponseEntity<CustomResponse<Void>> logout(String accessToken, WebRequest webRequest) {
        UUID loggedInUserId = userInfoService.checkUserLoggedInByUserId();

        // Step 1: Access Token Blacklist kro
        Duration remainingTtl = jwtService.getRemainingValidity(accessToken);

        if (!remainingTtl.isZero() && !remainingTtl.isNegative()) {
            // Sirf remaining time ke liye blacklist karo — memory waste nahi
            redisService.set(
                    redisBlacklistKey(accessToken),
                    "BLACKLISTED",
                    remainingTtl   // 13 min bache → 13 min ke liye Redis mein rahega
            );
        }
        // ── Step 2: DB se Refresh Token delete karo ──────────────────────────
        userRepository.clearRefreshToken(loggedInUserId);

        // ── Step 3: Last active update karo ──────────────────────────────────
        AppUser user = getUser(loggedInUserId);
        user.setLastActiveAt(OffsetDateTime.now());
        userRepository.save(user);

        return createResponseEntity.buildResponse(
                "Logged out successfully.",
                null,
                HttpStatus.OK,
                webRequest);
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<Void>> forgotPassword(AppUserRequest.ForgetPassword request, WebRequest webRequest) {
        AppUser user = getUser(request.getEmail());

        if (!user.isEmailVerified())
            throw new ResourceNotExistsException(RESOURCE + " email not verified yet");

        // redis storage
        String verificationToken = stringOperations.generateRandomString(6);
        String redisKey = redisResetPasswordOtpKey(user.getEmail());

        // send mail to user that the verification token will expire in 15 mins
        systemMailService.sendAccountCreationEmail(user.getEmail(), user.getEmail(), user.getFullName(), user.getPhone(), verificationToken);

        // Save token into redis for 15 minutes
        redisService.set(redisKey, passwordEncoder.encode(verificationToken), Duration.ofMinutes(15));
        return createResponseEntity.buildResponse("Reset password verification code sent to your email", null, HttpStatus.OK, webRequest);
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<Void>> verifyForgetPasswordCode(AppUserRequest.ResetPassword request, WebRequest webRequest) {
        // ── Step 1: User fetch ────────────────────────────────────────────────
        AppUser user = getUser(request.getEmail().toLowerCase().trim());

        // ── Step 2: Redis se token fetch (alag key — email OTP se collision nahi)
        String redisKey = redisResetPasswordOtpKey(user.getEmail());
        Optional<Object> token = redisService.get(redisKey);

        // ── Step 3: Token exists check ────────────────────────────────────────
        if (token.isEmpty())
            return createResponseEntity.buildResponse(
                    "No saved token found for this operation. " +
                            "Please request a new reset code and try again.",
                    null,
                    HttpStatus.BAD_REQUEST,
                    webRequest);

        // ── Step 4: Token comparison ──────────────────────────────────────────
        // BCrypt hash kabhi bhi plain text ke equal nahi hoga via .equals()
        if (!passwordEncoder.matches(
                request.getVerificationToken().trim(),
                token.get().toString())) {
            return createResponseEntity.buildResponse(
                    "Invalid token. Please check and try again.",
                    null,
                    HttpStatus.BAD_REQUEST,
                    webRequest);
        }

        // ── Step 5: New password same as current? ─────────────────────────────
        if (passwordEncoder.matches(
                request.getNewPassword().trim(),
                user.getPassword())) {
            return createResponseEntity.buildResponse(
                    "New password cannot be same as your current password.",
                    null,
                    HttpStatus.BAD_REQUEST,
                    webRequest);
        }

        // ── Step 6: Password update ───────────────────────────────────────────
        user.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);

        redisService.delete(redisKey);

        // ── Step 7: Refresh token invalidate ─────────────────────────────────
        // Warna koi aur device pe purane token se naya access token le sakta tha
        userRepository.clearRefreshToken(user.getId());

        // ── Step 8: Redis se OTP delete ───────────────────────────────────────
        redisService.delete(redisKey);

        // ── Step 9: Confirmation mail ─────────────────────────────────────────
        systemMailService.sendPasswordChangedEmail(
                user.getEmail(),
                user.getFullName(),
                user.getEmail());

        return createResponseEntity.buildResponse(
                "Password reset successfully. Please login with your new password.",
                null,
                HttpStatus.OK,
                webRequest);
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<AppUserResponse.Detail>> updateAccountDetailsByUser(AppUserRequest.Update request, WebRequest webRequest) {
        UUID loggedInUserId = userInfoService.checkUserLoggedInByUserId();
        AppUser user = getUser(loggedInUserId);
        user.setFullName(
                (request.getFullName() != null && !request.getFullName().equals(user.getFullName())) ? request.getFullName().trim() : user.getFullName()
        );
        user.setStdCode(
                (request.getStdCode() != null && !request.getStdCode().equals(user.getStdCode())) ? request.getStdCode().trim() : user.getStdCode()
        );
        String newPhone = (request.getPhone() != null && !request.getPhone().equals(user.getPhone()))
                ? request.getPhone().trim() : user.getPhone();

        if (!newPhone.equals(user.getPhone()) && userRepository.existsByPhone(newPhone))
            throw new ResourceAlreadyExistsException("Phone number already in use");

        user.setPhone(newPhone);
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);

        return createResponseEntity.buildResponse("User profile updated", userResponse.toDetail(user), HttpStatus.OK, webRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<CustomResponse<AppUserResponse.Detail>> getById(UUID id, WebRequest webRequest) {
        return createResponseEntity.buildResponse(
                RESOURCE + " fetched",
                userResponse.toDetail(getUser(id)),
                HttpStatus.OK,
                webRequest);
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<Void>> changePassword(AppUserRequest.ChangePassword request, WebRequest webRequest) {
        UUID loggedInUserId = userInfoService.checkUserLoggedInByUserId();
        AppUser user = getUser(loggedInUserId);

        if (!passwordEncoder.matches(request.getOldPassword().trim(), user.getPassword())) {
            return createResponseEntity.buildResponse("Old password not correct", null, HttpStatus.BAD_REQUEST, webRequest);
        }

        if (request.getOldPassword().trim().equals(request.getNewPassword().trim()))
            return createResponseEntity.buildResponse(
                    "New password cannot be same as old password", null, HttpStatus.BAD_REQUEST, webRequest);

        user.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);
        userRepository.clearRefreshToken(user.getId());

        systemMailService.sendPasswordChangedEmail(user.getEmail(), user.getFullName(), user.getEmail());

        return createResponseEntity.buildResponse("Password changed completed", null, HttpStatus.OK, webRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<CustomResponse<AppUserResponse.Stats>> getStats(WebRequest webRequest) {
        AppUserRepository.UserStatsProjection raw = userRepository.getUserStats();

        AppUserResponse.Stats stats = AppUserResponse.Stats.builder()
                .totalUsers(raw.getTotalUsers())
                .activeUsers(raw.getActiveUsers())
                .disabledUsers(raw.getDisabledUsers())
                .deletedUsers(raw.getDeletedUsers())
                .build();

        return createResponseEntity.buildResponse(
                "Users stats fetched successfully",
                stats,
                HttpStatus.OK,
                webRequest
        );
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<AppUserResponse.Detail>> assignRoles(UUID userId, AppUserRequest.Roles request, WebRequest webRequest) {
        AppUser user = getUser(userId);
        Set<Roles> existingRoles = userRoleService.resolveRolesOrDefault(request.getRoleIds());

        user.setRoles(existingRoles);
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);
        return createResponseEntity.buildResponse("User roles has assigned", userResponse.toDetail(user), HttpStatus.OK, webRequest);
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<AppUserResponse.Detail>> removeRoles(UUID userId, AppUserRequest.Roles request, WebRequest webRequest) {
        AppUser user = getUser(userId);
        Set<Roles> existingRoles = userRoleService.resolveRolesOrDefault(request.getRoleIds());

        user.getRoles().removeAll(existingRoles);
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);
        return createResponseEntity.buildResponse("User roles has removed", userResponse.toDetail(user), HttpStatus.OK, webRequest);
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<Void>> imageUpload(AppUserRequest.ProfileImage profileImage, WebRequest webRequest) {
        return null;
    }
}
