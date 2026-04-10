package com.bizflow;

import com.bizflow.modules.auth.entity.User;
import com.bizflow.modules.auth.repository.UserRepository;
import com.bizflow.modules.tenant.entity.Tenant;
import com.bizflow.modules.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class BizflowBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BizflowBackendApplication.class, args);
    }

    @Bean
    CommandLineRunner initData(TenantRepository tenantRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            if (tenantRepository.count() == 0) {

                Tenant tenant = Tenant.builder().name("Test Business").email("test@bizflow.com").phone("9999999999")
                        .businessType("RETAIL").isActive(true).build();
                tenant = tenantRepository.save(tenant);

                User user = User.builder().tenantId(tenant.getId()).name("Admin User").email("admin@bizflow.com")
                        .password(passwordEncoder.encode("password123")).role(User.Role.OWNER).isActive(true).build();
                userRepository.save(user);

                log.info("✅ Test data created!");
                log.info("📧 Email: admin@bizflow.com");
                log.info("🔑 Password: password123");
                log.info("🏢 TenantId: {}", tenant.getId());
            }
        };
    }
}