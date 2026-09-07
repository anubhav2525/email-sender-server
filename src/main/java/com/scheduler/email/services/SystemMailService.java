package com.scheduler.email.services;

import com.scheduler.email.data.dtos.Request.SystemMailRequest;

public interface SystemMailService {

    /**
     * Send a mail to verify user with details
     *
     * @param to                recipient email address
     * @param username          account username
     * @param fullName          recipient's full name
     * @param phone             contact number
     * @param verificationToken Verification token
     */
    void sendAccountCreationEmail(
            String to, String username, String fullName, String phone, String verificationToken);

    /**
     * Send a welcome email after verification
     *
     * @param to       recipient email address
     * @param username account username
     * @param fullName recipient's full name*
     */
    void sendWelcomeEmail(
            String to, String username, String fullName);


    /**
     * Send a forgot-password OTP email.
     *
     * @param to                recipient email address
     * @param username          account username
     * @param fullName          recipient's full name
     * @param verificationToken one-time password
     */
    void sendForgotPasswordEmail(
            String to, String username, String fullName, String verificationToken);

    /**
     * Notify user that their password was changed successfully.
     *
     * @param to       recipient email address
     * @param fullName recipient's full name
     * @param username account username
     */
    void sendPasswordChangedEmail(
            String to, String fullName, String username);

    /**
     * Notify user that their account has been locked.
     *
     * @param to       recipient email address
     * @param fullName recipient's full name
     * @param username account username
     * @param reason   reason for lock (e.g., too many failed login attempts)
     */
    void sendAccountLockedEmail(
            String to, String fullName, String username, String reason);

    /**
     * Notify user that their account has been unlocked.
     *
     * @param to       recipient email address
     * @param fullName recipient's full name
     * @param username account username
     */
    void sendAccountUnlockedEmail(
            String to, String fullName, String username);

    /***
     * Notify user that their account is approved
     * @param to -  recipient email address
     * @param fullName - recipient's full name
     */
    void sendAccountApproveEmail(
            String to, String fullName
    );

    /***
     * Notify user that their account is rejected
     * @param to -  recipient email address
     * @param fullName - recipient's full name
     * @param reason - rejection message
     */
    void sendAccountRejectEmail(String to, String fullName, String reason);

    /***
     * Notify user that their account is rejected
     * @param to -  recipient email address
     * @param fullName - recipient's full name*
     */
    void sendRemindToVerifyAccount(String to, String fullName);

    /**
     * Generic low-level method to send any templated email.
     *
     * @param request fully constructed EmailRequest
     */
    void sendEmail(SystemMailRequest.EmailRequest request);
}
