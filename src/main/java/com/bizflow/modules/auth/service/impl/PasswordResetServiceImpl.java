package com.bizflow.modules.auth.service.impl;

import com.bizflow.modules.auth.entity.PasswordResetToken;
import com.bizflow.modules.auth.repository.PasswordResetTokenRepository;
import com.bizflow.modules.auth.service.PasswordResetService;
import com.bizflow.modules.email.service.EmailService;
import com.bizflow.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public String generateAndSendResetLink(User user, int expiryHours, boolean isNewUser) {
        // 1. Delete any existing tokens for this user
        tokenRepository.deleteByUser(user);

        // 2. Generate a new token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(expiryHours))
                .build();
        tokenRepository.save(resetToken);

        // 3. Send the appropriate email
        if (!isNewUser) {
            log.info("Sending password reset email to: {}", user.getEmail());
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        }

        return token;
    }
}
