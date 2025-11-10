package com.viveek.aiclass.security;

import com.viveek.aiclass.domain.model.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupabaseAuthenticationTokenTest {

    @Test
    void constructor_CreatesAuthenticatedToken() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .authUserId(UUID.randomUUID())
                .email("test@example.com")
                .role(UserRole.TEACHER)
                .build();
        
        SupabaseAuthenticationToken token = new SupabaseAuthenticationToken(
                user, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_TEACHER"))
        );
        
        assertThat(token).isNotNull();
        assertThat(token.isAuthenticated()).isTrue();
        assertThat(token.getPrincipal()).isEqualTo(user);
        assertThat(token.getCredentials()).isNull();
        assertThat(token.getAuthorities()).hasSize(1);
    }

    @Test
    void setAuthenticated_False_Works() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .authUserId(UUID.randomUUID())
                .email("test@example.com")
                .role(UserRole.STUDENT)
                .build();
        
        SupabaseAuthenticationToken token = new SupabaseAuthenticationToken(
                user, 
                Collections.emptyList()
        );
        
        token.setAuthenticated(false);
        
        assertThat(token.isAuthenticated()).isFalse();
    }

    @Test
    void setAuthenticated_True_ThrowsException() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .authUserId(UUID.randomUUID())
                .email("test@example.com")
                .role(UserRole.STUDENT)
                .build();
        
        SupabaseAuthenticationToken token = new SupabaseAuthenticationToken(
                user, 
                Collections.emptyList()
        );
        
        assertThatThrownBy(() -> token.setAuthenticated(true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot set this token to trusted");
    }

    @Test
    void getPrincipal_ReturnsAuthenticatedUser() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .authUserId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .email("test@example.com")
                .role(UserRole.TEACHER)
                .fullName("Test User")
                .build();
        
        SupabaseAuthenticationToken token = new SupabaseAuthenticationToken(
                user, 
                user.getAuthorities()
        );
        
        AuthenticatedUser principal = token.getPrincipal();
        
        assertThat(principal).isNotNull();
        assertThat(principal.getEmail()).isEqualTo("test@example.com");
        assertThat(principal.getRole()).isEqualTo(UserRole.TEACHER);
        assertThat(principal.getFullName()).isEqualTo("Test User");
    }
}


