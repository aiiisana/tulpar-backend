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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // public health check
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Swagger UI & OpenAPI spec
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                // Static admin UI (auth is handled client-side via Firebase Web SDK)
                .requestMatchers("/admin-ui/**").permitAll()
                // Public read-only content (cached, no sensitive data)
                .requestMatchers(HttpMethod.GET,
                        "/flashcards/**", "/articles/**", "/grammar/**",
                        "/daily-challenge", "/conversation-club", "/settings").permitAll()
                // exercises — any authenticated user
                .requestMatchers(HttpMethod.GET, "/exercises", "/exercises/**").authenticated()
                // progress submission — any authenticated user
                .requestMatchers(HttpMethod.POST, "/progress").authenticated()
                // admin panel — ADMIN or CONTENT_MANAGER
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "CONTENT_MANAGER")
                // authenticated user features
                .requestMatchers("/onboarding/**", "/profile/**", "/stats/**",
                        "/chat/**", "/notifications/**", "/courses/**", "/lessons/**").authenticated()
                // catch-all — must be authenticated
                .anyRequest().authenticated()
            )
            .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Flutter mobile apps don't need CORS, but web clients and Swagger UI do.
        // In production replace "*" with your actual frontend origin(s).
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
