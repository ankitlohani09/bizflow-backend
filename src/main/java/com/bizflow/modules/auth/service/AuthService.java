package com.bizflow.modules.auth.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.auth.dto.LoginRequest;
import com.bizflow.modules.auth.dto.LoginResponse;

public interface AuthService {

    ApiResponse<LoginResponse> login(LoginRequest request);
}