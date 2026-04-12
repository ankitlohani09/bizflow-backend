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

    private static final String[] PUBLIC_URLS = { "/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html" };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers(PUBLIC_URLS).permitAll()

                        // ✅ Users - ADMIN only
                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN")

                        // ✅ Customers - DELETE only ADMIN, rest ADMIN+MANAGER+USER
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/customers/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/customers/**").hasAnyRole("ADMIN", "MANAGER", "USER")

                        // ✅ Categories - ADMIN+MANAGER
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").authenticated()
                        .requestMatchers("/api/v1/categories/**").hasAnyRole("ADMIN", "MANAGER")

                        // ✅ Items & Variants - GET all, POST/PUT ADMIN+MANAGER
                        .requestMatchers(HttpMethod.GET, "/api/v1/items/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/item-variants/**").authenticated()
                        .requestMatchers("/api/v1/items/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/v1/item-variants/**").hasAnyRole("ADMIN", "MANAGER")

                        // ✅ Inventory & Stock movements
                        .requestMatchers(HttpMethod.GET, "/api/v1/inventory/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/stock-movements/**").authenticated()
                        .requestMatchers("/api/v1/inventory/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/v1/stock-movements/**").hasAnyRole("ADMIN", "MANAGER")

                        // ✅ Invoices + Returns - GET all, POST ADMIN+MANAGER+USER
                        .requestMatchers(HttpMethod.GET, "/api/v1/invoices/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/returns/**").authenticated()
                        .requestMatchers("/api/v1/invoices/**").hasAnyRole("ADMIN", "MANAGER", "USER")
                        .requestMatchers("/api/v1/returns/**").hasAnyRole("ADMIN", "MANAGER", "USER")

                        // ✅ Purchases - ADMIN+MANAGER only
                        .requestMatchers("/api/v1/purchases/**").hasAnyRole("ADMIN", "MANAGER")

                        // ✅ Finance - ADMIN+MANAGER only
                        .requestMatchers("/api/v1/expenses/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/v1/expense-categories/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/v1/payment-modes/**").hasAnyRole("ADMIN", "MANAGER")

                        // ✅ Staff, Attendance, Advances - ADMIN+MANAGER only
                        .requestMatchers("/api/v1/staff/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/v1/attendance/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/v1/staff-advances/**").hasAnyRole("ADMIN", "MANAGER")

                        // ✅ Masters (Units, Suppliers) - GET all, POST ADMIN only
                        .requestMatchers(HttpMethod.GET, "/api/v1/units/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/suppliers/**").authenticated()
                        .requestMatchers("/api/v1/units/**").hasRole("ADMIN").requestMatchers("/api/v1/suppliers/**")
                        .hasAnyRole("ADMIN", "MANAGER")

                        // ✅ Logs - ADMIN only
                        .requestMatchers("/api/v1/ai-logs/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/activity-logs/**").hasRole("ADMIN")

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