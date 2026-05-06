package com.bizflow.modules.email.service.impl;

import com.bizflow.modules.email.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Async
    public void sendMail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom(fromEmail);
            mailSender.send(message);
            log.info("Simple email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", toEmail, e);
        }
    }

    @Override
    @Async
    public void sendOnboardingEmail(String to, String companyName) {
        Context context = new Context();
        context.setVariable("companyName", companyName);

        String body = templateEngine.process("onboarding-email", context);

        sendHtmlEmail(to, "Welcome to BizFlow - Complete Your Setup", body);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String resetToken) {
        Context context = new Context();
        context.setVariable("resetToken", resetToken);
        // Assuming there will be a frontend URL for password reset
        context.setVariable("resetUrl", "http://localhost:5173/reset-password?token=" + resetToken);

        String body = templateEngine.process("password-reset-email", context);

        sendHtmlEmail(to, "BizFlow - Password Reset Request", body);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.setFrom(fromEmail);
            mailSender.send(mimeMessage);
            log.info("HTML email '{}' sent to: {}", subject, to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email '{}' to: {}", subject, to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
