package com.bizflow.security;

import com.bizflow.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public class SecurityUtils {

    private static BizFlowUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof BizFlowUserDetails userDetails) {
            return userDetails;
        }
        throw new BusinessException("User not authenticated", HttpStatus.UNAUTHORIZED);
    }

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof BizFlowUserDetails u)
            return u.getUserId();
        return null;
    }

    public static Long getCurrentTenantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof BizFlowUserDetails u)
            return u.getTenantId();
        return null;
    }

    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof BizFlowUserDetails u)
            return u.getUsername();
        return null;
    }

    public static String getCurrentUserEmail() {
        return getCurrentUser().getUsername();
    }

    public static List<String> getCurrentRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof BizFlowUserDetails u)
            return u.getRoles();
        return List.of();
    }

    public static boolean hasRole(String role) {
        return getCurrentRoles().contains(role);
    }
}