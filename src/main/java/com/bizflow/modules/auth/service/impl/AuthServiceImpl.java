package com.bizflow.modules.auth.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.exception.BusinessException;
import com.bizflow.modules.auth.dto.LoginRequest;
import com.bizflow.modules.auth.dto.LoginResponse;
import com.bizflow.modules.auth.dto.RefreshTokenRequest;
import com.bizflow.modules.auth.entity.User;
import com.bizflow.modules.auth.repository.UserRepository;
import com.bizflow.modules.auth.repository.UserRoleRepository;
import com.bizflow.modules.auth.service.AuthService;
import com.bizflow.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<LoginResponse> login(LoginRequest request) {

        User user;
        if (request.getTenantId() != null) {
            user = userRepository.findByEmailAndTenantId(request.getEmail(), request.getTenantId()).orElseThrow(
                    () -> new BusinessException(MessageConstant.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED));
        } else {
            user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                    () -> new BusinessException(MessageConstant.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new BusinessException(MessageConstant.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);

        if (!user.getIsActive())
            throw new BusinessException(MessageConstant.ACCOUNT_DISABLED, HttpStatus.FORBIDDEN);

        List<String> roles = userRoleRepository.findRoleNamesByUserId(user.getId());

        String accessToken = jwtService.generateToken(user.getEmail(), user.getId(), user.getTenantId(), roles);
        String refreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getId(), user.getTenantId());

        LoginResponse response = LoginResponse.builder().token(accessToken).refreshToken(refreshToken)
                .userId(user.getId()).tenantId(user.getTenantId()).name(user.getName()).email(user.getEmail())
                .roles(roles).profilePictureUrl(user.getProfilePictureUrl()).build();

        return ApiResponse.success(MessageConstant.LOGIN_SUCCESS, response);
    }

    @Override
    public ApiResponse<LoginResponse> refreshToken(RefreshTokenRequest request) {
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

        String newAccessToken = jwtService.generateToken(email, userId, tenantId, roles);

        LoginResponse response = LoginResponse.builder().token(newAccessToken).refreshToken(token).userId(userId)
                .tenantId(tenantId).name(user.getName()).email(email).roles(roles)
                .profilePictureUrl(user.getProfilePictureUrl()).build();

        return ApiResponse.success(MessageConstant.TOKEN_REFRESHED, response);
    }
}