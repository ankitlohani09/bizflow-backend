package com.bizflow.security;

import com.bizflow.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private static BizFlowUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof BizFlowUserDetails userDetails) {
            return userDetails;
        }
        throw new BusinessException("User not authenticated", HttpStatus.UNAUTHORIZED);
    }

    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof BizFlowUserDetails userDetails) {
            return userDetails.getUsername();
        }
        return null;
    }

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof BizFlowUserDetails userDetails) {
            return userDetails.getUserId();
        }
        return null;
    }

    public static Long getCurrentTenantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof BizFlowUserDetails userDetails) {
            return userDetails.getTenantId();
        }
        return null;
    }

    public static String getCurrentUserEmail() {
        return getCurrentUser().getUsername();
    }

    public static String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof BizFlowUserDetails userDetails) {
            return userDetails.getRole();
        }
        return null;
    }
}