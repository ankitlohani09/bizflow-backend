package com.bizflow.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long userId;
    private Long tenantId;
    private String tenantCode;
    private String name;
    private String email;
    private List<String> roles;
    private String profilePictureUrl;

}