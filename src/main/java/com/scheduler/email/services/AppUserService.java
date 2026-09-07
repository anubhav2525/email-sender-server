package com.scheduler.email.services;

import com.scheduler.email.common.CustomResponse;
import com.scheduler.email.common.PagedResponse;
import com.scheduler.email.data.dtos.Request.AppUserRequest;
import com.scheduler.email.data.dtos.Response.AppUserResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.UUID;

public interface AppUserService {

    /***
     * This service is used to create the user first time if not exists
     * @param request - Full name, Email, Std code, Phone, password, Role ids
     * @param webRequest - Request header
     * @return - NULL
     */
    ResponseEntity<CustomResponse<Void>> create(AppUserRequest.Create request, WebRequest webRequest);

    /***
     * This service is used to verify the newly created account
     * @param request - Email, Verification code
     * @param webRequest - Request Header
     * @return - NULL
     */
    ResponseEntity<CustomResponse<Void>> verifyUser(AppUserRequest.Verify request, WebRequest webRequest);

    /***
     * This service is used to resend the verification code
     * @param resendVerificationCode - Email
     * @param webRequest - Request header
     * @return - NULL
     */
    ResponseEntity<CustomResponse<Void>> resendVerificationCode(AppUserRequest.ResendVerificationCode resendVerificationCode, WebRequest webRequest);

    /***
     * This service is used to verify and login the user when account is verified by admin
     * @param request - Email, Password
     * @param webRequest - Request header
     * @return - Auth token, Refresh token, Token type(Bearer)
     */
    ResponseEntity<CustomResponse<AppUserResponse.Auth>> login(AppUserRequest.Login request, WebRequest webRequest);

    /***
     * This service is used to get all the pending account information
     * @param webRequest - Request header
     * @return - Return the User basic detail
     */
    ResponseEntity<CustomResponse<PagedResponse<AppUserResponse.Summary>>> pendingAccount(AppUserRequest.PendingAccount request, WebRequest webRequest);

    /***
     * This service is used to approve the account by admin/platform management
     * @param request - Email
     * @param webRequest - Request Header
     * @return - NULL
     */
    ResponseEntity<CustomResponse<Void>> approveAccountByEmail(AppUserRequest.ApproveAccount request, WebRequest webRequest);

    /***
     * This service is used to forget the account by their email id and get the verification code
     * @param request - Email
     * @param webRequest - Request Header
     * @return - NUL but send the mail with verification code
     */
    ResponseEntity<CustomResponse<Void>> forgotPassword(AppUserRequest.ForgetPassword request, WebRequest webRequest);

    /***
     * This service is used to verify the code for the reset password & set new password after verified code for reset
     * @param request - Email, code, password
     * @param webRequest - Request header
     * @return - void
     */
    ResponseEntity<CustomResponse<Void>> verifyForgetPasswordCode(AppUserRequest.ResetPassword request, WebRequest webRequest);

    /***
     * This service is used to update details by user
     * @param request - Name, std code, phone
     * @param webRequest - Request Header
     * @return - User detail
     */
    ResponseEntity<CustomResponse<AppUserResponse.Detail>> updateAccountDetailsByUser(AppUserRequest.Update request, WebRequest webRequest);

    /***
     * This service is used to get user information by using their id
     * @param id -User id
     * @param webRequest - Request header
     * @return - Detail
     */
    ResponseEntity<CustomResponse<AppUserResponse.Detail>> getById(UUID id, WebRequest webRequest);

    /***
     * This service is used to change user password
     * @param request - new password
     * @param webRequest - Request header
     * @return - NULL
     */
    ResponseEntity<CustomResponse<Void>> changePassword(AppUserRequest.ChangePassword request, WebRequest webRequest);

    /***
     * This service is used to return the statistics of users
     * @param webRequest - Request header
     * @return - Stats
     */
    ResponseEntity<CustomResponse<AppUserResponse.Stats>> getStats(WebRequest webRequest);

    /***
     * This service is used to assign roles to user or a particular admin
     * @param userId - User id
     * @param request - UUID's of roles
     * @param webRequest - Request header
     * @return - User detail
     */
    ResponseEntity<CustomResponse<AppUserResponse.Detail>> assignRoles(UUID userId, AppUserRequest.Roles request, WebRequest webRequest);

    /***
     * This service is used to remove roles from a user
     * @param userId - User id
     * @param request - UUID's of roles
     * @param webRequest - Request header
     * @return - User detail
     */
    ResponseEntity<CustomResponse<AppUserResponse.Detail>> removeRoles(UUID userId, AppUserRequest.Roles request, WebRequest webRequest);

    /***
     * This service is used to upload profile image
     * @param profileImage - Image
     * @param webRequest - Request Header
     * @return - NULL
     */
    ResponseEntity<CustomResponse<Void>> imageUpload(AppUserRequest.ProfileImage profileImage, WebRequest webRequest);

    /***
     * This service is used to reject account
     * @param request - Email, Reason
     * @param webRequest - Request Header
     * @return - NULL
     */
    ResponseEntity<CustomResponse<Void>> rejectAccount(AppUserRequest.RejectAccount request, WebRequest webRequest);

    ResponseEntity<CustomResponse<Void>> remindToVerifyUserAccount(AppUserRequest.RemindAccountVerification remindAccountVerification, WebRequest webRequest);

    /***
     * This service is used to generate refresh token
     * @param refreshToken
     * @param webRequest - Request handler
     * @return - Auth tokens
     */
    ResponseEntity<CustomResponse<AppUserResponse.Auth>> refreshToken(AppUserRequest.RefreshToken refreshToken, WebRequest webRequest);

    /***
     * This service is used to logout user
     * @param accessToken - Access token
     * @param webRequest - Request handler
     * @return - NULL
     */
    ResponseEntity<CustomResponse<Void>> logout(
            String accessToken,
            WebRequest webRequest
    );
}
