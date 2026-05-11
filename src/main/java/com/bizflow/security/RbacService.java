package com.bizflow.security;

import com.bizflow.modules.role.entity.Role;
import com.bizflow.modules.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component("rbac")
@RequiredArgsConstructor
public class RbacService {

    private final RoleRepository roleRepository;

    public boolean hasPermission(String requiredPermission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof BizFlowUserDetails)) {
            return false;
        }

        BizFlowUserDetails userDetails = (BizFlowUserDetails) principal;
        Long tenantId = userDetails.getTenantId();
        List<String> roles = userDetails.getRoles();

        // OWNER bypass
        if (roles.contains("OWNER")) {
            return true;
        }

        List<Role> roleEntities = roleRepository.findByTenantIdAndNameIn(tenantId, roles);
        return roleEntities.stream()
                .map(Role::getPermissions)
                .filter(Objects::nonNull)
                .flatMap(p -> Arrays.stream(p.split(",")))
                .anyMatch(p -> p.equals(requiredPermission) || p.equals("ALL"));
    }
}
