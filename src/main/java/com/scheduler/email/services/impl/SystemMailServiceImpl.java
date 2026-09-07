package com.scheduler.email.services.impl;

import com.scheduler.email.data.dtos.Request.SystemMailRequest;
import com.scheduler.email.services.SystemMailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemMailServiceImpl implements SystemMailService {
    private final JavaMailSender mailSender;
    @Qualifier("emailTemplateEngine")
    private final SpringTemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.from-name:No Reply}")
    private String fromName;

    @Value("${app.name:MyApp}")
    private String appName;

    @Value("${app.support-email:support@myapp.com}")
    private String supportEmail;

    @Value("${app.logo-url:}")
    private String logoUrl;

    @Override
    public void sendAccountCreationEmail(String to, String username, String fullName, String phone, String verificationToken) {
        log.info("Sending account creation email to {}", to);

        Map<String, Object> vars = baseVars();
        vars.put("fullName", fullName);
        vars.put("username", username);
        vars.put("verificationToken", verificationToken);
        vars.put("phone", phone);

        sendEmail(SystemMailRequest.EmailRequest.builder()
                .to(to)
                .subject("Welcome to " + appName + " – Please verify your account")
                .templateName("account-verification")
                .variables(vars)
                .build());
    }

    @Override
    public void sendWelcomeEmail(String to, String username, String fullName) {
        log.info("Sending welcome email to {}", to);

        Map<String, Object> vars = baseVars();
        vars.put("fullName", fullName);
        vars.put("username", username);

        sendEmail(SystemMailRequest.EmailRequest.builder()
                .to(to)
                .subject("Welcome to " + appName + " – Your Account Is Ready")
                .templateName("welcome-email")
                .variables(vars)
                .build());
    }

    @Override
    public void sendForgotPasswordEmail(String to, String username, String fullName, String verificationToken) {
        log.info("Sending forgot-password OTP email to {}", to);

        Map<String, Object> vars = baseVars();
        vars.put("fullName", fullName);
        vars.put("username", username);
        vars.put("verificationToken", verificationToken);

        sendEmail(SystemMailRequest.EmailRequest.builder()
                .to(to)
                .subject(appName + " – Password Reset OTP")
                .templateName("forget-password")
                .variables(vars)
                .build());
    }

    @Override
    public void sendPasswordChangedEmail(String to, String fullName, String username) {
        log.info("Sending password changed email to {}", to);

        Map<String, Object> vars = baseVars();
        vars.put("fullName", fullName);
        vars.put("username", username);

        sendEmail(SystemMailRequest.EmailRequest.builder()
                .to(to)
                .subject(appName + " – Your Password Has Been Changed")
                .templateName("password-changed")
                .variables(vars)
                .build());
    }

    @Override
    public void sendAccountLockedEmail(String to, String fullName, String username, String reason) {
        log.info("Sending account locked email to {}", to);

        Map<String, Object> vars = baseVars();
        vars.put("fullName", fullName);
        vars.put("username", username);
        vars.put("reason", reason);

        sendEmail(SystemMailRequest.EmailRequest.builder()
                .to(to)
                .subject(appName + " – Your Account Has Been Locked")
                .templateName("account-locked")
                .variables(vars)
                .build());
    }

    @Override
    public void sendAccountUnlockedEmail(String to, String fullName, String username) {
        log.info("Sending account unlocked email to {}", to);

        Map<String, Object> vars = baseVars();
        vars.put("fullName", fullName);
        vars.put("username", username);

        sendEmail(SystemMailRequest.EmailRequest.builder()
                .to(to)
                .subject(appName + " – Your Account Has Been Unlocked")
                .templateName("account-unlocked")
                .variables(vars)
                .build());
    }

    @Override
    public void sendAccountApproveEmail(String to, String fullName) {
        log.info("Sending account approved email to {}", to);

        Map<String, Object> vars = baseVars();
        vars.put("fullName", fullName);

        sendEmail(SystemMailRequest.EmailRequest.builder()
                .to(to)
                .subject(appName + " – Your Account Has Been Approved")
                .templateName("account-approve")
                .variables(vars)
                .build());
    }

    @Override
    public void sendAccountRejectEmail(String to, String fullName, String reason) {
        log.info("Sending account rejected email to {}", to);

        Map<String, Object> vars = baseVars();
        vars.put("fullName", fullName);
        vars.put("reason", reason);

        sendEmail(SystemMailRequest.EmailRequest.builder()
                .to(to)
                .subject(appName + " – Your Account Has Been Rejected")
                .templateName("account-reject")
                .variables(vars)
                .build());
    }

    @Override
    public void sendRemindToVerifyAccount(String to, String fullName) {
        log.info("Sending account verify remind email to {}", to);

        Map<String, Object> vars = baseVars();
        vars.put("fullName", fullName);

        sendEmail(SystemMailRequest.EmailRequest.builder()
                .to(to)
                .subject(appName + " – Reminder to verify your account")
                .templateName("account-verify-remind")
                .variables(vars)
                .build());
    }

    @Override
    public void sendEmail(SystemMailRequest.EmailRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            Context ctx = new Context();
            if (request.getVariables() != null) {
                ctx.setVariables(request.getVariables());
            }

            String htmlContent = templateEngine.process(request.getTemplateName(), ctx);

            helper.setFrom(fromAddress, fromName);
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email [{}] sent successfully to {}", request.getSubject(), request.getTo());

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send email to {}: {}", request.getTo(), e.getMessage(), e);
            throw new RuntimeException("Failed to send email to " + request.getTo(), e);
        }
    }

    /**
     * Provides variables common to every template.
     */
    private Map<String, Object> baseVars() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("appName", appName);
        vars.put("supportEmail", supportEmail);
        vars.put("logoUrl", logoUrl);
        return vars;
    }
}
