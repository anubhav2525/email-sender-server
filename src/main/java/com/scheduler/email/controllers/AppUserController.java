package com.scheduler.email.controllers;

import com.scheduler.email.common.ApiVersion;
import com.scheduler.email.common.CustomResponse;
import com.scheduler.email.common.PagedResponse;
import com.scheduler.email.data.dtos.Request.AppUserRequest;
import com.scheduler.email.data.dtos.Response.AppUserResponse;
import com.scheduler.email.services.AppUserService;
import com.scheduler.email.utils.CreateResponseEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(ApiVersion.V1 + "/users")
@RequiredArgsConstructor
public class AppUserController {
    private final AppUserService appUserService;
    private final CreateResponseEntity createResponseEntity;

    /***
     * This API is used to get all pending accounts to approve
     * @param webRequest - Request header
     * @return - Summary
     */
    @GetMapping("/pending-accounts")
    public ResponseEntity<CustomResponse<PagedResponse<AppUserResponse.Summary>>> pendingAccount(@RequestBody AppUserRequest.PendingAccount request, WebRequest webRequest) {
        log.info("API path: {}", createResponseEntity.getApiPath(webRequest));
        log.info("Get all the pending accounts");
        return appUserService.pendingAccount(request, webRequest);
    }

    /***
     * This API is used to approve the account by admin/platform management
     * @param request - Email
     * @param webRequest - Request Header
     * @return - NULL
     */
    @PutMapping("/approve-account")
    public ResponseEntity<CustomResponse<Void>> approveAccountByEmail(
            @Valid @RequestBody AppUserRequest.ApproveAccount request,
            WebRequest webRequest) {
        log.info("API path: {}", createResponseEntity.getApiPath(webRequest));
        log.info("Approve account for {}", request.getEmail());
        return appUserService.approveAccountByEmail(request, webRequest);
    }

    /***
     * This API is used to reject newly account
     * @param request - Email, Reason
     * @param webRequest - Request Header
     * @return - NULL
     */
    @PutMapping("/reject-account")
    public ResponseEntity<CustomResponse<Void>> rejectAccount(
            @Valid @RequestBody AppUserRequest.RejectAccount request,
            WebRequest webRequest) {
        log.info("API path: {}", createResponseEntity.getApiPath(webRequest));
        log.info("Reject account of user: {}", request.getEmail().toLowerCase().trim());
        return appUserService.rejectAccount(request, webRequest);
    }

    /***
     * This API is used to get user information by using their id
     * @param id -User id
     * @param webRequest - Request header
     * @return - Detail
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<AppUserResponse.Detail>> getById(
            @PathVariable UUID id,
            WebRequest webRequest) {
        log.info("API path: {}", createResponseEntity.getApiPath(webRequest));
        log.info("Request to login user account: {}", id);
        return appUserService.getById(id, webRequest);
    }

    /***
     * This API is used to upload profile image
     * @param profileImage - Image
     * @param webRequest - Request Header
     * @return - NULL
     */
    @PostMapping("/profile-image")
    public ResponseEntity<CustomResponse<Void>> imageUpload(
            @Valid @RequestBody AppUserRequest.ProfileImage profileImage,
            WebRequest webRequest) {
        log.info("API path: {}", createResponseEntity.getApiPath(webRequest));
        log.info("Upload profile image to user: {}", 1);
        return appUserService.imageUpload(profileImage, webRequest);
    }

    /***
     * This API is used to return the statistics of users
     * @param webRequest - Request header
     * @return - Stats
     */
    @GetMapping("/stats")
    public ResponseEntity<CustomResponse<AppUserResponse.Stats>> getStats(WebRequest webRequest) {
        log.info("API path: {}", createResponseEntity.getApiPath(webRequest));
        log.info("Get stats of user");
        return appUserService.getStats(webRequest);
    }

    /***
     * This API is used to assign roles to user or a particular admin
     * @param userId - User id
     * @param request - UUID's of roles
     * @param webRequest - Request header
     * @return - User detail
     */
    @PutMapping("/assign-roles/{userId}")
    public ResponseEntity<CustomResponse<AppUserResponse.Detail>> assignRoles(
            @PathVariable UUID userId,
            @Valid @RequestBody AppUserRequest.Roles request,
            WebRequest webRequest) {
        log.info("API path: {}", createResponseEntity.getApiPath(webRequest));
        log.info("Assign roles for user: {}", userId);
        return appUserService.assignRoles(userId, request, webRequest);
    }

    /***
     * This API is used to remove roles from a user
     * @param userId - User id
     * @param request - UUID's of roles
     * @param webRequest - Request header
     * @return - User detail
     */
    @PutMapping("/remove-roles/{userId}")
    public ResponseEntity<CustomResponse<AppUserResponse.Detail>> removeRoles(
            @PathVariable UUID userId,
            @Valid @RequestBody AppUserRequest.Roles request,
            WebRequest webRequest) {
        log.info("API path: {}", createResponseEntity.getApiPath(webRequest));
        log.info("Remove roles for user: {}", userId);
        return appUserService.removeRoles(userId, request, webRequest);
    }
}
