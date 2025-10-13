package com.viveek.aiclass.security;

import com.viveek.aiclass.domain.model.User;
import com.viveek.aiclass.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Converts a Supabase JWT token into a custom authentication token with user details.
 * 
 * This converter extracts claims from the JWT (issued by Supabase Auth) and enriches
 * them with user information from our database. It creates an AuthenticatedUser object
 * that serves as the security principal throughout the application.
 * 
 * JWT Claims expected:
 * - sub: Supabase Auth UUID (required)
 * - email: User's email (required)
 * - role: User's role from JWT metadata (optional, will be fetched from DB if missing)
 * 
 * @author AIClass API Team
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SupabaseJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Extract the Supabase Auth UUID from the 'sub' claim
        String subClaim = jwt.getSubject();
        UUID authUserId;
        
        try {
            authUserId = UUID.fromString(subClaim);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID in JWT subject: {}", subClaim);
            throw new IllegalArgumentException("Invalid auth_user_id in JWT token", e);
        }

        // Extract email from JWT claims
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            log.error("JWT token missing email claim for auth_user_id: {}", authUserId);
            throw new IllegalArgumentException("JWT token must contain email claim");
        }

        // Build the authenticated user with JWT claims
        AuthenticatedUser.AuthenticatedUserBuilder userBuilder = AuthenticatedUser.builder()
                .authUserId(authUserId)
                .email(email);

        // Try to fetch additional user information from database
        Optional<User> userOptional = userRepository.findByAuthUserId(authUserId);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            userBuilder
                    .userId(user.getId())
                    .role(user.getRole())
                    .fullName(user.getFullName());
            log.debug("Authenticated user found in database: {} ({})", user.getEmail(), user.getRole());
        } else {
            // User authenticated via Supabase but not yet in our database
            // This can happen during initial sign-up flow
            log.warn("User authenticated but not found in database: {}", email);
            // Role will be null, which will restrict access until user record is created
        }

        AuthenticatedUser authenticatedUser = userBuilder.build();
        
        // Create and return the authentication token with authorities
        return new SupabaseAuthenticationToken(
                authenticatedUser,
                authenticatedUser.getAuthorities()
        );
    }
}

