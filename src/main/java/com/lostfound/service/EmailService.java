package com.lostfound.service;

import com.lostfound.config.AppConfig;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reusable Centralized Email Service for Campus Lost & Found Portal.
 * Connects to Gmail SMTP (smtp.gmail.com:587 TLS) using Jakarta Mail & Eclipse Angus Mail.
 * Provides responsive, university-branded HTML email templates for OTP verification.
 */
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    private static final EmailService INSTANCE = new EmailService();

    public enum EmailPurpose {
        REGISTER("Email Verification OTP", "Campus Lost & Found - Email Verification OTP"),
        FORGOT_PASSWORD("Password Recovery OTP", "Campus Lost & Found - Password Reset OTP"),
        CLAIM_ITEM("Claim Verification OTP", "Campus Lost & Found - Claim Verification OTP"),
        REPORT_LOST_ITEM("Report Verification OTP", "Campus Lost & Found - Lost Item Report Verification OTP"),
        EMAIL_VERIFICATION("Email Verification", "Campus Lost & Found - Email Verification");

        private final String label;
        private final String subject;

        EmailPurpose(String label, String subject) {
            this.label = label;
            this.subject = subject;
        }

        public String getLabel() {
            return label;
        }

        public String getSubject() {
            return subject;
        }
    }

    private EmailService() {
        // Singleton
    }

    public static EmailService getInstance() {
        return INSTANCE;
    }

    /**
     * Dispatches a 6-digit OTP email to the recipient with professional HTML formatting.
     * Returns true if sent via SMTP or successfully generated in dev mode.
     */
    public boolean sendOtpEmail(String recipientEmail, String otp, EmailPurpose purpose) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            LOGGER.warning("Cannot send OTP email: recipient email is empty.");
            return false;
        }

        String normalizedEmail = recipientEmail.trim().toLowerCase();
        String subject = purpose.getSubject();
        String htmlBody = buildHtmlTemplate(normalizedEmail, otp, purpose);

        // Always print prominently to console for instant developer feedback & audit
        logOtpBanner(normalizedEmail, otp, purpose);

        if (!AppConfig.isGmailConfigured()) {
            LOGGER.info("[DEV MODE] Real Gmail SMTP not configured in .env (GMAIL_USER & GMAIL_APP_PASSWORD). OTP logged to console.");
            return true;
        }

        // Send via live Gmail SMTP
        try {
            return sendSmtpEmail(normalizedEmail, subject, htmlBody);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to dispatch email via Gmail SMTP: " + e.getMessage() +
                    ". Falling back to in-memory/console OTP verification.", e);
            return false;
        }
    }

    /**
     * Sends HTML email using Jakarta Mail over Gmail SMTP TLS Port 587.
     */
    public boolean sendSmtpEmail(String toAddress, String subject, String htmlContent) throws Exception {
        String gmailUser = AppConfig.getGmailUser();
        String gmailAppPassword = AppConfig.getGmailAppPassword();

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.connectiontimeout", "7000");
        props.put("mail.smtp.timeout", "7000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(gmailUser, gmailAppPassword);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(gmailUser, "Campus Lost & Found Portal", StandardCharsets.UTF_8.name()));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
        message.setSubject(subject, StandardCharsets.UTF_8.name());
        message.setContent(htmlContent, "text/html; charset=UTF-8");

        Transport.send(message);
        LOGGER.info("[GMAIL SMTP] Successfully dispatched email to: " + toAddress + " with subject: " + subject);
        return true;
    }

    private void logOtpBanner(String email, String otp, EmailPurpose purpose) {
        System.out.println("================================================================================");
        System.out.println(" [CAMPUS LOST & FOUND - GMAIL OTP DISPATCH]");
        System.out.println(" Recipient: " + email);
        System.out.println(" Purpose  : " + purpose.getLabel());
        System.out.println(" OTP Code : >>> " + otp + " <<<");
        System.out.println(" Validity : 5 Minutes (Cryptographically Secure)");
        System.out.println(" Status   : " + (AppConfig.isGmailConfigured() ? "Dispatched to Gmail SMTP" : "Development Mode / Ready"));
        System.out.println("================================================================================");
    }

    /**
     * Builds responsive HTML email template.
     */
    private String buildHtmlTemplate(String email, String otp, EmailPurpose purpose) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f1f5f9; margin: 0; padding: 24px; color: #1e293b; }" +
                ".container { max-width: 560px; margin: 0 auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.06); border: 1px solid #e2e8f0; }" +
                ".header { background: linear-gradient(135deg, #0d6efd 0%, #0a58ca 100%); padding: 32px 24px; text-align: center; color: #ffffff; }" +
                ".logo-badge { display: inline-block; background: rgba(255,255,255,0.2); padding: 8px 16px; border-radius: 50px; font-size: 13px; font-weight: 600; letter-spacing: 0.5px; margin-bottom: 12px; }" +
                ".header h1 { margin: 0; font-size: 24px; font-weight: 700; }" +
                ".content { padding: 36px 32px; text-align: center; }" +
                ".badge-purpose { display: inline-block; background: #e0f2fe; color: #0369a1; padding: 6px 14px; border-radius: 20px; font-size: 13px; font-weight: 600; margin-bottom: 20px; }" +
                ".lead-text { font-size: 15px; line-height: 1.6; color: #475569; margin-bottom: 28px; }" +
                ".otp-box { background: #f8fafc; border: 2px dashed #cbd5e1; border-radius: 12px; padding: 20px; margin: 0 auto 28px auto; max-width: 320px; }" +
                ".otp-label { font-size: 12px; text-transform: uppercase; letter-spacing: 1.5px; color: #64748b; font-weight: 600; margin-bottom: 8px; }" +
                ".otp-code { font-family: 'Courier New', Courier, monospace; font-size: 38px; font-weight: 800; letter-spacing: 8px; color: #0d6efd; }" +
                ".expiry-alert { font-size: 13px; color: #d97706; background: #fffbeb; padding: 10px 16px; border-radius: 8px; display: inline-block; margin-bottom: 24px; border: 1px solid #fef3c7; }" +
                ".security-note { font-size: 13px; color: #64748b; line-height: 1.5; border-top: 1px solid #f1f5f9; padding-top: 20px; }" +
                ".footer { background: #f8fafc; padding: 20px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #e2e8f0; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<div class='logo-badge'>CAMPUS PROPERTY RECOVERY NETWORK</div>" +
                "<h1>Campus Lost & Found Portal</h1>" +
                "<p style='margin: 8px 0 0 0; opacity: 0.9; font-size: 14px;'>Find what you've lost. Return what you've found.</p>" +
                "</div>" +
                "<div class='content'>" +
                "<div class='badge-purpose'>" + purpose.getLabel() + "</div>" +
                "<p class='lead-text'>Hello,<br>We received a verification request for your account <strong>" + email + "</strong>. Use the 6-digit One-Time Password below to complete your verification:</p>" +
                "<div class='otp-box'>" +
                "<div class='otp-label'>Your Verification Code</div>" +
                "<div class='otp-code'>" + otp + "</div>" +
                "</div>" +
                "<div class='expiry-alert'><strong>Important:</strong> This verification code expires in 5 minutes.</div>" +
                "<div class='security-note'>Do NOT share this code with anyone. Campus administrators will never ask for your password or verification codes.<br>If you did not request this code, you can safely ignore this email.</div>" +
                "</div>" +
                "<div class='footer'>" +
                "&copy; " + java.time.Year.now().getValue() + " Campus Lost & Found Management System &bull; Secure Multi-Factor Authentication" +
                "</div>" +
                "</div></body></html>";
    }
}
