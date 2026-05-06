package com.bizflow.modules.user.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.common.utility.FileStorageService;
import com.bizflow.modules.user.dto.UserRequest;
import com.bizflow.modules.user.dto.UserResponse;
import com.bizflow.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Users", description = "User management operations")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
public class UserController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    @Operation(summary = "Upload profile picture")
    @PostMapping("/{id}/profile-picture")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER') or #id == authentication.principal.userId")
    public ResponseEntity<ApiResponse<UserResponse>> uploadProfilePicture(@PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File is empty"));
        }

        String imageUrl = fileStorageService.uploadFile(file, "profile-pics");
        return ResponseEntity.ok(userService.updateProfilePicture(id, imageUrl));
    }

    @Operation(summary = "Delete profile picture")
    @DeleteMapping("/{id}/profile-picture")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER') or #id == authentication.principal.userId")
    public ResponseEntity<ApiResponse<UserResponse>> deleteProfilePicture(@PathVariable Long id) {
        return ResponseEntity.ok(userService.updateProfilePicture(id, null));
    }

    @Operation(summary = "Get all users")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Create new user")
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @Operation(summary = "Update user by ID")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @Operation(summary = "Delete user by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }
}
