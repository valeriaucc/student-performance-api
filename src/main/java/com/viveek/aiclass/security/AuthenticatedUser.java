package com.viveek.aiclass.security;

import com.viveek.aiclass.domain.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Custom UserDetails implementation representing an authenticated user from Supabase.
 * Contains the user's authentication information extracted from the JWT token.
 * 
 * This class serves as the principal in the Spring Security context and provides
 * access to the authenticated user's information throughout the application.
 * 
 * @author AIClass API Team
 */
@Getter
@Builder
@AllArgsConstructor
public class AuthenticatedUser implements UserDetails {

    /**
     * Supabase Auth UUID - the 'sub' claim from the JWT token.
     * This is the unique identifier from Supabase authentication system.
     */
    private final UUID authUserId;

    /**
     * Internal database user ID (if available).
     * This is populated after looking up the user in our database.
     */
    private UUID userId;

    /**
     * User's email address from the JWT token.
     */
    private final String email;

    /**
     * User's role (TEACHER or STUDENT).
     * Determines access permissions throughout the application.
     */
    private UserRole role;

    /**
     * User's full name (if available from database).
     */
    private String fullName;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            return Collections.emptyList();
        }
        // Return role with ROLE_ prefix as per Spring Security convention
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + role.name().toUpperCase())
        );
    }

    @Override
    public String getPassword() {
        // Password is managed by Supabase, not stored in our system
        return null;
    }

    @Override
    public String getUsername() {
        // Use email as the username
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Check if the user has a specific role.
     * 
     * @param checkRole the role to check
     * @return true if the user has the specified role
     */
    public boolean hasRole(UserRole checkRole) {
        return this.role == checkRole;
    }

    /**
     * Check if the user is a teacher.
     * 
     * @return true if the user has the TEACHER role
     */
    public boolean isTeacher() {
        return UserRole.TEACHER.equals(this.role);
    }

    /**
     * Check if the user is a student.
     * 
     * @return true if the user has the STUDENT role
     */
    public boolean isStudent() {
        return UserRole.STUDENT.equals(this.role);
    }
}

