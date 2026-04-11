package com.bizflow.modules.auth.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.constant.MessageConstant;
import com.bizflow.common.exception.BusinessException;
import com.bizflow.modules.auth.dto.LoginRequest;
import com.bizflow.modules.auth.dto.LoginResponse;
import com.bizflow.modules.auth.entity.User;
import com.bizflow.modules.auth.repository.UserRepository;
import com.bizflow.modules.auth.service.AuthService;
import com.bizflow.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
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

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(MessageConstant.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
        }

        if (!user.getIsActive()) {
            throw new BusinessException(MessageConstant.ACCOUNT_DISABLED, HttpStatus.FORBIDDEN);
        }

        String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getTenantId(),
                user.getRole().name());

        LoginResponse response = LoginResponse.builder().token(token).tokenType("Bearer").userId(user.getId())
                .tenantId(user.getTenantId()).name(user.getName()).email(user.getEmail()).role(user.getRole().name())
                .build();

        return ApiResponse.success(MessageConstant.LOGIN_SUCCESS, response);
    }
}