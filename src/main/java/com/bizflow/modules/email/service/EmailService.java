package com.bizflow.modules.email.service;

public interface EmailService {
    void sendMail(String toEmail, String subject, String body);

    void sendOnboardingEmail(String to, String companyName, String resetToken);

    void sendPasswordResetEmail(String to, String resetToken);

    void sendPasswordSetupEmail(String to, String resetToken);
}
