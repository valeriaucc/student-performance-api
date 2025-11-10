package com.viveek.aiclass.security;

import com.viveek.aiclass.domain.model.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticatedUserTest {

    @Test
    void builder_CreatesAuthenticatedUser() {
        UUID authUserId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        AuthenticatedUser user = AuthenticatedUser.builder()
                .authUserId(authUserId)
                .userId(userId)
                .email("test@example.com")
                .role(UserRole.TEACHER)
                .fullName("Test Teacher")
                .build();
        
        assertThat(user).isNotNull();
        assertThat(user.getAuthUserId()).isEqualTo(authUserId);
        assertThat(user.getUserId()).isEqualTo(userId);
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getRole()).isEqualTo(UserRole.TEACHER);
        assertThat(user.getFullName()).isEqualTo("Test Teacher");
    }

    @Test
    void getAuthorities_WithRole() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .authUserId(UUID.randomUUID())
                .email("test@example.com")
                .role(UserRole.TEACHER)
                .build();
        
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_TEACHER");
    }

    @Test
    void getAuthorities_WithoutRole() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .authUserId(UUID.randomUUID())
                .email("test@example.com")
                .build();
        
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        
        assertThat(authorities).isEmpty();
    }

    @Test
    void getUserDetails_Methods() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .authUserId(UUID.randomUUID())
                .email("test@example.com")
                .role(UserRole.STUDENT)
                .build();
        
        assertThat(user.getUsername()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isNull();
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void hasRole_ReturnsCorrectValue() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .authUserId(UUID.randomUUID())
                .email("test@example.com")
                .role(UserRole.TEACHER)
                .build();
        
        assertThat(user.hasRole(UserRole.TEACHER)).isTrue();
        assertThat(user.hasRole(UserRole.STUDENT)).isFalse();
    }

    @Test
    void isTeacher_ReturnsTrue() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .authUserId(UUID.randomUUID())
                .email("test@example.com")
                .role(UserRole.TEACHER)
                .build();
        
        assertThat(user.isTeacher()).isTrue();
        assertThat(user.isStudent()).isFalse();
    }

    @Test
    void isStudent_ReturnsTrue() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .authUserId(UUID.randomUUID())
                .email("test@example.com")
                .role(UserRole.STUDENT)
                .build();
        
        assertThat(user.isStudent()).isTrue();
        assertThat(user.isTeacher()).isFalse();
    }
}


