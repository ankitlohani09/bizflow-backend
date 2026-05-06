package com.bizflow.config;

import com.bizflow.common.constant.MessageConstant;
import com.bizflow.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final ObjectMapper objectMapper;

    private static final String[] PUBLIC_URLS = { "/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
            "/uploads/**" };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.requestMatchers(PUBLIC_URLS).permitAll()

                        // ✅ Users - ADMIN + OWNER only
                        .requestMatchers("/api/v1/users/**").hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/roles/**").hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers("/api/v1/roles/**").hasRole("ADMIN")

                        // ✅ Customers
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/customers/**").hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers("/api/v1/customers/**").hasAnyRole("ADMIN", "OWNER", "MANAGER", "USER")

                        // ✅ Categories
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").authenticated()
                        .requestMatchers("/api/v1/categories/**").hasRole("ADMIN")

                        // ✅ Items & Variants
                        .requestMatchers(HttpMethod.GET, "/api/v1/items/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/item-variants/**").authenticated()
                        .requestMatchers("/api/v1/items/**").hasAnyRole("ADMIN", "OWNER", "MANAGER")
                        .requestMatchers("/api/v1/item-variants/**").hasAnyRole("ADMIN", "OWNER", "MANAGER")

                        // ✅ Inventory & Stock Movements
                        .requestMatchers(HttpMethod.GET, "/api/v1/inventory/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/stock-movements/**").authenticated()
                        .requestMatchers("/api/v1/inventory/**").hasAnyRole("ADMIN", "OWNER", "MANAGER")
                        .requestMatchers("/api/v1/stock-movements/**").hasAnyRole("ADMIN", "OWNER", "MANAGER")

                        // ✅ Invoices + Returns
                        .requestMatchers(HttpMethod.GET, "/api/v1/invoices/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/returns/**").authenticated()
                        .requestMatchers("/api/v1/invoices/**").hasAnyRole("ADMIN", "OWNER", "MANAGER", "USER")
                        .requestMatchers("/api/v1/returns/**").hasAnyRole("ADMIN", "OWNER", "MANAGER", "USER")

                        // ✅ Kitchen Orders (Restaurant)
                        .requestMatchers(HttpMethod.GET, "/api/v1/kitchen-orders/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/kitchen-orders/**")
                        .hasAnyRole("ADMIN", "OWNER", "MANAGER").requestMatchers("/api/v1/kitchen-orders/**")
                        .hasAnyRole("ADMIN", "OWNER", "MANAGER", "USER")

                        // ✅ Purchases
                        .requestMatchers("/api/v1/purchases/**").hasAnyRole("ADMIN", "OWNER", "MANAGER")

                        // ✅ Finance
                        .requestMatchers("/api/v1/expenses/**").hasAnyRole("ADMIN", "OWNER", "MANAGER")
                        .requestMatchers("/api/v1/expense-categories/**").hasAnyRole("ADMIN", "OWNER", "MANAGER")
                        .requestMatchers("/api/v1/payment-modes/**").hasAnyRole("ADMIN", "OWNER", "MANAGER")

                        // ✅ Staff, Attendance, Advances
                        .requestMatchers("/api/v1/staff/**").hasAnyRole("ADMIN", "OWNER", "MANAGER")
                        .requestMatchers("/api/v1/attendance/**").hasAnyRole("ADMIN", "OWNER", "MANAGER")
                        .requestMatchers("/api/v1/staff-advances/**").hasAnyRole("ADMIN", "OWNER", "MANAGER")

                        // ✅ Masters
                        .requestMatchers(HttpMethod.GET, "/api/v1/units/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/suppliers/**").authenticated()
                        .requestMatchers("/api/v1/units/**").hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers("/api/v1/suppliers/**").hasAnyRole("ADMIN", "OWNER", "MANAGER")

                        // ✅ Reports - ADMIN+OWNER+MANAGER (P&L sirf ADMIN+OWNER — @PreAuthorize handles it)
                        .requestMatchers("/api/v1/reports/**").hasAnyRole("ADMIN", "OWNER", "MANAGER") // ✅ NEW

                        // ✅ Tenants - ADMIN only (super admin)
                        .requestMatchers("/api/v1/tenants/**").hasRole("ADMIN") // ✅ NEW

                        // ✅ Logs, White Label, AI
                        .requestMatchers("/api/v1/ai-logs/**").hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers("/api/v1/activity-logs/**").hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers("/api/v1/white-label/**").hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers("/api/v1/ai/**").hasAnyRole("ADMIN", "OWNER", "MANAGER")

                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(objectMapper
                            .writeValueAsString(Map.of("success", false, "message", MessageConstant.SESSION_EXPIRED)));
                }).accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(objectMapper
                            .writeValueAsString(Map.of("success", false, "message", MessageConstant.ACCESS_DENIED)));
                })).addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Use JWT authentication");
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
