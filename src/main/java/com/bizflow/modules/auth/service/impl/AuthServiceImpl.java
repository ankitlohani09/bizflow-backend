package com.bizflow.modules.auth.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.exception.BusinessException;
import com.bizflow.modules.auth.dto.LoginRequest;
import com.bizflow.modules.auth.dto.LoginResponse;
import com.bizflow.modules.auth.dto.RefreshTokenRequest;
import com.bizflow.modules.user.entity.User;
import com.bizflow.modules.user.repository.UserRepository;
import com.bizflow.modules.user.repository.UserRoleRepository;
import com.bizflow.modules.auth.repository.PasswordResetTokenRepository;
import com.bizflow.modules.auth.entity.PasswordResetToken;
import com.bizflow.modules.email.service.EmailService;
import com.bizflow.modules.tenant.repository.TenantRepository;
import com.bizflow.modules.tenant.entity.Tenant;
import com.bizflow.modules.auth.service.AuthService;
import com.bizflow.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final TenantRepository tenantRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public ApiResponse<LoginResponse> login(LoginRequest request) {
        // ... (existing implementation)
        User user;
        String identifier = request.getEmail(); // The 'email' field in LoginRequest might contain a phone number

        if (request.getTenantId() != null) {
            user = userRepository.findByEmailAndTenantId(identifier, request.getTenantId())
                    .or(() -> userRepository.findByPhoneAndTenantId(identifier, request.getTenantId())).orElseThrow(
                            () -> new BusinessException(MessageConstant.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED));
        } else {
            user = userRepository.findByEmail(identifier).or(() -> userRepository.findByPhone(identifier)).orElseThrow(
                    () -> new BusinessException(MessageConstant.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new BusinessException(MessageConstant.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);

        if (!user.getIsActive())
            throw new BusinessException(MessageConstant.ACCOUNT_DISABLED, HttpStatus.FORBIDDEN);

        List<String> roles = userRoleRepository.findRoleNamesByUserId(user.getId());
        Tenant tenant = tenantRepository.findById(user.getTenantId()).orElse(null);
        String tenantCode = tenant != null ? tenant.getCode() : null;

        String accessToken = jwtService.generateToken(user.getEmail(), user.getId(), user.getTenantId(), roles);
        String refreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getId(), user.getTenantId());

        LoginResponse response = LoginResponse.builder().token(accessToken).refreshToken(refreshToken)
                .userId(user.getId()).tenantId(user.getTenantId()).tenantCode(tenantCode).name(user.getName())
                .email(user.getEmail()).roles(roles).profilePictureUrl(user.getProfilePictureUrl()).build();

        return ApiResponse.success(MessageConstant.LOGIN_SUCCESS, response);
    }

    @Override
    public ApiResponse<LoginResponse> refreshToken(RefreshTokenRequest request) {
        // ... (existing implementation)
        String token = request.getRefreshToken();

        if (jwtService.isTokenExpired(token))
            throw new BusinessException(MessageConstant.SESSION_EXPIRED, HttpStatus.UNAUTHORIZED);

        if (!jwtService.isRefreshToken(token))
            throw new BusinessException(MessageConstant.INVALID_TOKEN, HttpStatus.UNAUTHORIZED);

        String email = jwtService.extractUsername(token);
        Long userId = jwtService.extractUserId(token);
        Long tenantId = jwtService.extractTenantId(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(MessageConstant.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED));

        if (!user.getIsActive())
            throw new BusinessException(MessageConstant.ACCOUNT_DISABLED, HttpStatus.FORBIDDEN);

        List<String> roles = userRoleRepository.findRoleNamesByUserId(userId);
        Tenant tenant = tenantRepository.findById(tenantId).orElse(null);
        String tenantCode = tenant != null ? tenant.getCode() : null;

        String newAccessToken = jwtService.generateToken(email, userId, tenantId, roles);

        LoginResponse response = LoginResponse.builder().token(newAccessToken).refreshToken(token).userId(userId)
                .tenantId(tenantId).tenantCode(tenantCode).name(user.getName()).email(email).roles(roles)
                .profilePictureUrl(user.getProfilePictureUrl()).build();

        return ApiResponse.success(MessageConstant.TOKEN_REFRESHED, response);
    }

    @Override
    @Transactional
    public ApiResponse<String> forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found with this email", HttpStatus.NOT_FOUND));

        // Delete old tokens
        tokenRepository.deleteByUser(user);

        // Generate new token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder().token(token).user(user)
                .expiryDate(LocalDateTime.now().plusHours(24)).build();

        tokenRepository.save(resetToken);

        // Send email
        emailService.sendPasswordResetEmail(email, token);

        return ApiResponse.success("Password reset instructions sent to your email", null);
    }

    @Override
    @Transactional
    public ApiResponse<String> resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Invalid reset token", HttpStatus.BAD_REQUEST));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new BusinessException("Reset token has expired", HttpStatus.BAD_REQUEST);
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);

        return ApiResponse.success("Password reset successful. You can now login.", null);
    }
}
