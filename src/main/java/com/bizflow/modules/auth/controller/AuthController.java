package com.bizflow.modules.auth.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.auth.dto.LoginRequest;
import com.bizflow.modules.auth.dto.LoginResponse;
import com.bizflow.modules.auth.dto.RefreshTokenRequest;
import com.bizflow.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Login and token management")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "User login", description = "Returns JWT token on successful authentication")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // ✅ NEW - Refresh token endpoint
    @Operation(summary = "Refresh token", description = "Returns new access token using refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    // ✅ NEW - Logout (client-side token invalidation hint)
    @Operation(summary = "Logout", description = "Invalidates current session")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}