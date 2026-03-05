package kz.diploma.tulpar.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.diploma.tulpar.domain.entity.User;
import kz.diploma.tulpar.domain.enums.UserRole;
import kz.diploma.tulpar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Intercepts every request, extracts the Firebase Bearer token, verifies it
 * with Firebase Admin SDK, and populates the Spring Security context.
 *
 * If a verified user does not yet exist in the database, they are auto-provisioned
 * with the USER role. This removes the need for a separate registration endpoint.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseTokenFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractBearerToken(request);

        if (token != null) {
            try {
                FirebaseToken decoded = firebaseAuth.verifyIdToken(token);
                String uid   = decoded.getUid();
                String email = decoded.getEmail();

                // Auto-provision user on first login
                User user = userRepository.findById(uid)
                        .orElseGet(() -> userRepository.save(
                                User.builder()
                                        .id(uid)
                                        .email(email)
                                        .role(UserRole.USER)
                                        .build()
                        ));

                UserPrincipal principal = new UserPrincipal(uid, email, user.getRole());
                FirebaseAuthenticationToken auth = new FirebaseAuthenticationToken(principal);
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (FirebaseAuthException ex) {
                log.warn("Invalid Firebase token: {}", ex.getMessage());
                // Do not set authentication — SecurityConfig will deny access to protected routes
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
