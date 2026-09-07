package com.scheduler.email.controllers;

import com.scheduler.email.common.ApiVersion;
import com.scheduler.email.common.CustomResponse;
import com.scheduler.email.data.dtos.Request.AppUserRequest;
import com.scheduler.email.data.dtos.Response.AppUserResponse;
import com.scheduler.email.services.AppUserService;
import com.scheduler.email.utils.CreateResponseEntity;
import com.scheduler.email.utils.UserInfoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestController
@RequestMapping(ApiVersion.V1 + "/auth/users")
@RequiredArgsConstructor
public class AuthController {
    private final AppUserService appUserService;
    private final CreateResponseEntity createResponseEntity;
    private final UserInfoService userInfoService;

    /***
     * This Api is used to create user
     * @param create - Name, Phone, Std code, Email
     * @param webRequest - Request Header
     * @return - NULL
     */
    @PostMapping("/sign-up")
    public ResponseEntity<CustomResponse<Void>> createUser(
            @Valid @RequestBody AppUserRequest.Create create,
            WebRequest webRequest) {
        log.info("API path: {}", createResponseEntity.getApiPath(webRequest));
        log.info("Request to create user account: {}, {}", create.getEmail(), create);
        return appUserService.create(create, webRequest);
    }

    /***
     * This Api is used to verify user token
     * @param request - Email, Verification token
     * @param webRequest - Request Header
     * @return - NULL
     */
    @PostMapping("/verify")
    public ResponseEntity<CustomResponse<Void>> verifyUser(
            @Valid @RequestBody AppUserRequest.Verify request,
            WebRequest webRequest) {
        log.info("API path: {}", createResponseEntity.getApiPath(webRequest));
        log.info("Request to verify user account: {}", request.getEmail());
        return appUserService.verifyUser(request, webRequest);
    }

    @PostMapping("/resend-verification-code")
    public ResponseEntity<CustomResponse<Void>> resendVerificationToken(
            @Valid @RequestBody AppUserRequest.ResendVerificationCode resendVerificationCode, WebRequest webRequest
    ) {
        log.info("API path: {}", createResponseEntity.getApiPath(webRequest));
        log.info("Resend verification code to {}", resendVerificationCode.getEmail());
        return appUserService.resendVerificationCode(resendVerificationCode, webRequest);
    }

    /***
     * This APi is used to sign in the user
     * @param request - Email, password
     * @param webRequest - Request header
     * @return - Token
     */
    @PostMapping("/sign-in")
    public ResponseEntity<CustomResponse<AppUserResponse.Auth>> loginUser(
            @Valid @RequestBody AppUserRequest.Login request,
            WebRequest webRequest) {
        log.info("API path: {}", createResponseEntity.getApiPath(webRequest));
        log.info("Request to login user account: {}", request.getEmail());
        return appUserService.login(request, webRequest);
    }

    /***
     * This service is used to change user password
     * @param request - new password
     * @param webRequest - Request header
     * @return - NULL
     */
    @PutMapping("/change-password")
    public ResponseEntity<CustomResponse<Void>> changePassword(
            @Valid @RequestBody AppUserRequest.ChangePassword request,
            WebRequest webRequest) {
        log.info("API path: {}", createResponseEntity.getApiPath(webRequest));
        log.info("Password change request for {} ", userInfoService.getUserLoggedInUserId());
        return appUserService.changePassword(request, webRequest);
    }

    /***
     * This service is used to forget the account by their email id and get the verification code
     * @param request - Email
     * @param webRequest - Request Header
     * @return - NUL but send the mail with verification code
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<CustomResponse<Void>> forgotPassword(
            @Valid @RequestBody AppUserRequest.ForgetPassword request,
            WebRequest webRequest) {
        log.info("API path: {}", createResponseEntity.getApiPath(webRequest));
        log.info("Forgot account for {} ", request.getEmail());
        return appUserService.forgotPassword(request, webRequest);
    }

    /***
     * This service is used to verify the code for the reset password
     * @param request - Email, code
     * @param webRequest - Request header
     * @return - void
     */
    @PostMapping("/verify-reset-token")
    public ResponseEntity<CustomResponse<Void>> verifyForgetPasswordCode(
            @Valid @RequestBody AppUserRequest.ResetPassword request,
            WebRequest webRequest) {
        log.info("API path: {}", createResponseEntity.getApiPath(webRequest));
        log.info("Verify reset token for {} ", request.getEmail());
        return appUserService.verifyForgetPasswordCode(request, webRequest);
    }

    /***
     * This service is used to update details by user
     * @param request - Name, std code, phone
     * @param webRequest - Request Header
     * @return - User detail
     */
    @PutMapping("/update-profile")
    public ResponseEntity<CustomResponse<AppUserResponse.Detail>> updateAccountDetailsByUser(
            @Valid @RequestBody AppUserRequest.Update request,
            WebRequest webRequest) {
        log.info("API path: {}", createResponseEntity.getApiPath(webRequest));
        log.info("Update account details for {} ", userInfoService.getUserLoggedInUserId());
        return appUserService.updateAccountDetailsByUser(request, webRequest);
    }

    /***
     * This API is used to logout user
     * @param request - HttpServletRequest
     * @param webRequest
     * @return
     */
    @PostMapping("/logout")
    public ResponseEntity<CustomResponse<Void>> logout(
            HttpServletRequest request,
            WebRequest webRequest) {

        // Header se token nikalo
        String authHeader = request.getHeader("Authorization");
        String accessToken = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        return appUserService.logout(accessToken, webRequest);
    }
}
