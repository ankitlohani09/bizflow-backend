package com.bizflow.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

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

    public static String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof BizFlowUserDetails userDetails) {
            return userDetails.getRole();
        }
        return null;
    }
}