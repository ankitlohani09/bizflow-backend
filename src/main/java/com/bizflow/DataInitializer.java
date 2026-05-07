package com.bizflow;

import com.bizflow.modules.role.entity.Role;
import com.bizflow.modules.user.entity.User;
import com.bizflow.modules.user.entity.UserRole;
import com.bizflow.modules.role.repository.RoleRepository;
import com.bizflow.modules.user.repository.UserRepository;
import com.bizflow.modules.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.findByEmail("admin@bizflow.com").isPresent()) {
            return;
        }

        User user = User.builder().tenantId(1L).name("Admin User").email("admin@bizflow.com")
                .password(passwordEncoder.encode("admin")).isActive(true).build();

        user = userRepository.save(user);

        Role adminRole = roleRepository.findByNameAndTenantId("ADMIN", 1L).get();

        UserRole userRole = UserRole.builder().userId(user.getId()).roleId(adminRole.getId()).build();

        userRoleRepository.save(userRole);
    }
}