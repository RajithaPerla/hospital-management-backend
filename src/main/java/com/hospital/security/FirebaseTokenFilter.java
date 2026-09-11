package com.hospital.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.hospital.model.User;
import com.hospital.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Intercepts every request, looks for "Authorization: Bearer <Firebase ID token>",
 * verifies it against Firebase's public keys via the Admin SDK, and - if valid -
 * establishes the Spring Security context. Auto-provisions a local `users` row
 * on first sign-in so the rest of the app can just deal with our own User entity.
 */
@Component
public class FirebaseTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FirebaseTokenFilter.class);

    private final UserRepository userRepository;

    public FirebaseTokenFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String idToken = header.substring(7);
            try {
                FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);

                String firebaseUid = decoded.getUid();
                String email = decoded.getEmail();
                String name = decoded.getName() != null ? decoded.getName() : email;

                // First-login auto-provisioning: create a local User row keyed
                // to the Firebase UID so downstream code never has to talk to
                // Firebase again for authorization decisions.
                User user = userRepository.findByFirebaseUid(firebaseUid)
                        .orElseGet(() -> userRepository.save(
                                User.builder()
                                        .firebaseUid(firebaseUid)
                                        .email(email)
                                        .fullName(name)
                                        .role(User.Role.PATIENT)
                                        .build()));

                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));

                var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (FirebaseAuthException e) {
                log.warn("Invalid Firebase ID token: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                // Let the request continue unauthenticated; Spring Security's
                // authorization rules will reject it with 401/403 as configured.
            }
        }

        filterChain.doFilter(request, response);
    }
}
