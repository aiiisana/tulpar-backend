package kz.diploma.tulpar.config;

import kz.diploma.tulpar.security.FirebaseTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless JWT/Firebase security configuration.
 *
 * Route rules:
 *   - Actuator health endpoints  → public
 *   - GET /exercises/**          → authenticated (any role)
 *   - POST /progress             → authenticated (any role)
 *   - /admin/**                  → ADMIN or CONTENT_MANAGER
 *   - Everything else            → authenticated
 *
 * @EnableMethodSecurity enables @PreAuthorize on individual methods for
 * fine-grained control (e.g. ADMIN-only user management).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final FirebaseTokenFilter firebaseTokenFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // public health check
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // exercises — any authenticated user
                .requestMatchers(HttpMethod.GET, "/exercises", "/exercises/**").authenticated()
                // progress submission — any authenticated user
                .requestMatchers(HttpMethod.POST, "/progress").authenticated()
                // admin panel — ADMIN or CONTENT_MANAGER
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "CONTENT_MANAGER")
                // catch-all — must be authenticated
                .anyRequest().authenticated()
            )
            .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
