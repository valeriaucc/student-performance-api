package com.viveek.aiclass.security;

import com.viveek.aiclass.domain.model.User;
import com.viveek.aiclass.domain.model.enums.UserRole;
import com.viveek.aiclass.domain.repository.UserRepository;
import com.viveek.aiclass.util.EmailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Converts a Supabase JWT token into a custom authentication token with user details.
 * 
 * This converter extracts claims from the JWT (issued by Supabase Auth) and enriches
 * them with user information from our database. It creates an AuthenticatedUser object
 * that serves as the security principal throughout the application.
 * 
 * If a user is authenticated via Supabase but doesn't have a profile in our database,
 * this converter will automatically create one with the role from JWT metadata.
 * 
 * JWT Claims expected:
 * - sub: Supabase Auth UUID (required)
 * - email: User's email (required)
 * - user_metadata.role: User's role (optional, defaults to STUDENT)
 * - user_metadata.full_name: User's full name (optional)
 * 
 * @author AIClass API Team
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SupabaseJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;

    @Override
    @Transactional
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

        // Extract email from JWT claims and normalize it
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            log.error("JWT token missing email claim for auth_user_id: {}", authUserId);
            throw new IllegalArgumentException("JWT token must contain email claim");
        }
        
        // Normalize email to lowercase and trim whitespace for consistency
        String normalizedEmail = EmailUtils.normalizeEmail(email);

        // Build the authenticated user with JWT claims
        AuthenticatedUser.AuthenticatedUserBuilder userBuilder = AuthenticatedUser.builder()
                .authUserId(authUserId)
                .email(normalizedEmail);

        // Try to fetch additional user information from database
        Optional<User> userOptional = userRepository.findByAuthUserId(authUserId);
        
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            log.debug("Authenticated user found in database: {} ({})", user.getEmail(), user.getRole());
        } else {
            // User authenticated via Supabase but not yet in our database
            // Auto-create user profile from JWT claims
            log.info("User authenticated but not found in database. Auto-creating profile for: {}", normalizedEmail);
            user = createUserFromJwt(jwt, authUserId, normalizedEmail);
            log.info("User profile auto-created: {} with role {}", normalizedEmail, user.getRole());
        }
        
        // Populate authenticated user from database user
        userBuilder
                .userId(user.getId())
                .role(user.getRole())
                .fullName(user.getFullName());

        AuthenticatedUser authenticatedUser = userBuilder.build();
        
        // Create and return the authentication token with authorities
        return new SupabaseAuthenticationToken(
                authenticatedUser,
                authenticatedUser.getAuthorities()
        );
    }
    
    /**
     * Creates a new user profile from JWT claims.
     * 
     * Extracts user information from the JWT token's user_metadata claim and creates
     * a corresponding User entity in the database. This is called automatically when
     * a user authenticates via Supabase but doesn't yet have a profile in our system.
     * 
     * @param jwt the JWT token containing user claims
     * @param authUserId the Supabase Auth UUID
     * @param email the user's email address (should already be normalized)
     * @return the newly created User entity
     */
    private User createUserFromJwt(Jwt jwt, UUID authUserId, String email) {
        // Email should already be normalized by caller, but ensure it here as well
        String normalizedEmail = EmailUtils.normalizeEmail(email);
        // Extract user metadata from JWT
        Map<String, Object> userMetadata = jwt.getClaimAsMap("user_metadata");
        if (userMetadata == null) {
            userMetadata = new HashMap<>();
        }
        
        // Extract role from metadata, default to STUDENT if not specified
        String roleString = (String) userMetadata.get("role");
        UserRole role = UserRole.STUDENT; // Default role
        
        if (roleString != null && !roleString.isBlank()) {
            try {
                role = UserRole.valueOf(roleString.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role '{}' in JWT metadata for user {}. Defaulting to STUDENT", roleString, email);
            }
        }
        
        // Extract full name from metadata
        String fullName = (String) userMetadata.get("full_name");
        if (fullName == null || fullName.isBlank()) {
            // Use email prefix as fallback
            fullName = normalizedEmail.split("@")[0];
        }
        
        // Create and save the user with normalized email
        User newUser = User.builder()
                .authUserId(authUserId)
                .email(normalizedEmail)
                .fullName(fullName)
                .role(role)
                .metadata(new HashMap<>(userMetadata))
                .build();
        
        return userRepository.save(newUser);
    }
}

