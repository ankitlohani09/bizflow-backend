package com.bizflow.modules.auth.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.auth.dto.LoginRequest;
import com.bizflow.modules.auth.dto.LoginResponse;
import com.bizflow.modules.auth.dto.RefreshTokenRequest;

public interface AuthService {
    ApiResponse<LoginResponse> login(LoginRequest request);

    ApiResponse<LoginResponse> refreshToken(RefreshTokenRequest request);

    ApiResponse<String> forgotPassword(String email);

    ApiResponse<String> resetPassword(String token, String newPassword);
    
    ApiResponse<Boolean> verifyResetToken(String token);
}