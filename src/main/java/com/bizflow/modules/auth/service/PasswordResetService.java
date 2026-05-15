package com.bizflow.modules.auth.service;

import com.bizflow.modules.user.entity.User;

public interface PasswordResetService {
    String generateAndSendResetLink(User user, int expiryHours, boolean isNewUser);
}
