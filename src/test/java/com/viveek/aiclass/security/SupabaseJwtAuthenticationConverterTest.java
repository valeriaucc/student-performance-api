package com.viveek.aiclass.security;

import com.viveek.aiclass.domain.model.User;
import com.viveek.aiclass.domain.model.enums.UserRole;
import com.viveek.aiclass.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupabaseJwtAuthenticationConverterTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SupabaseJwtAuthenticationConverter converter;

    private UUID authUserId;
    private String email;

    @BeforeEach
    void setUp() {
        authUserId = UUID.randomUUID();
        email = "test@example.com";
    }

    @Test
    void convert_ExistingUser_ReturnsAuthenticationToken() {
        User existingUser = User.builder()
                .authUserId(authUserId)
                .email(email)
                .fullName("Test User")
                .role(UserRole.TEACHER)
                .build();
        existingUser.setId(UUID.randomUUID());
        
        when(userRepository.findByAuthUserId(authUserId)).thenReturn(Optional.of(existingUser));
        
        Jwt jwt = createJwt(authUserId.toString(), email, null);
        
        AbstractAuthenticationToken token = converter.convert(jwt);
        
        assertThat(token).isNotNull();
        assertThat(token).isInstanceOf(SupabaseAuthenticationToken.class);
        assertThat(token.isAuthenticated()).isTrue();
        
        AuthenticatedUser user = (AuthenticatedUser) token.getPrincipal();
        assertThat(user.getAuthUserId()).isEqualTo(authUserId);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getRole()).isEqualTo(UserRole.TEACHER);
        assertThat(user.getFullName()).isEqualTo("Test User");
    }

    @Test
    void convert_NewUser_CreatesUserProfile() {
        when(userRepository.findByAuthUserId(authUserId)).thenReturn(Optional.empty());
        
        Map<String, Object> userMetadata = new HashMap<>();
        userMetadata.put("role", "STUDENT");
        userMetadata.put("full_name", "New Student");
        
        User newUser = User.builder()
                .authUserId(authUserId)
                .email(email)
                .fullName("New Student")
                .role(UserRole.STUDENT)
                .build();
        newUser.setId(UUID.randomUUID());
        
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        
        Jwt jwt = createJwt(authUserId.toString(), email, userMetadata);
        
        AbstractAuthenticationToken token = converter.convert(jwt);
        
        assertThat(token).isNotNull();
        AuthenticatedUser user = (AuthenticatedUser) token.getPrincipal();
        assertThat(user.getAuthUserId()).isEqualTo(authUserId);
        assertThat(user.getEmail()).isEqualTo(email);
    }

    @Test
    void convert_InvalidUUID_ThrowsException() {
        Jwt jwt = createJwt("invalid-uuid", email, null);
        
        assertThatThrownBy(() -> converter.convert(jwt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid auth_user_id");
    }

    @Test
    void convert_MissingEmail_ThrowsException() {
        Jwt jwt = createJwt(authUserId.toString(), null, null);
        
        assertThatThrownBy(() -> converter.convert(jwt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email claim");
    }

    @Test
    void convert_BlankEmail_ThrowsException() {
        Jwt jwt = createJwt(authUserId.toString(), "   ", null);
        
        assertThatThrownBy(() -> converter.convert(jwt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email claim");
    }

    @Test
    void convert_NormalizesEmail() {
        User existingUser = User.builder()
                .authUserId(authUserId)
                .email("test@example.com") // lowercase
                .fullName("Test User")
                .role(UserRole.TEACHER)
                .build();
        existingUser.setId(UUID.randomUUID());
        
        when(userRepository.findByAuthUserId(authUserId)).thenReturn(Optional.of(existingUser));
        
        // JWT has uppercase email
        Jwt jwt = createJwt(authUserId.toString(), "Test@EXAMPLE.COM", null);
        
        AbstractAuthenticationToken token = converter.convert(jwt);
        
        AuthenticatedUser user = (AuthenticatedUser) token.getPrincipal();
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    private Jwt createJwt(String sub, String email, Map<String, Object> userMetadata) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", sub);
        if (email != null) {
            claims.put("email", email);
        }
        if (userMetadata != null) {
            claims.put("user_metadata", userMetadata);
        }
        
        return new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                claims
        );
    }
}

