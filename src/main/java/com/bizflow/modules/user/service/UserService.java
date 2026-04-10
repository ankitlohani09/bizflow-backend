package com.bizflow.modules.user.service;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.user.dto.UserRequest;
import com.bizflow.modules.user.dto.UserResponse;

import java.util.List;

public interface UserService {

    ApiResponse<List<UserResponse>> getAllUsers();

    ApiResponse<UserResponse> getUserById(Long id);

    ApiResponse<UserResponse> createUser(UserRequest request);

    ApiResponse<UserResponse> updateUser(Long id, UserRequest request);

    ApiResponse<Void> deleteUser(Long id);
}