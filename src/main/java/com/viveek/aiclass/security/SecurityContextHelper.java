package com.viveek.aiclass.security;

import com.viveek.aiclass.domain.model.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Helper utility for accessing the currently authenticated user from the security context.
 * 
 * Provides convenient methods to retrieve the authenticated user's information
 * throughout the application without directly accessing the SecurityContext.
 * 
 * Usage examples:
 * <pre>
 * // Get current user ID
 * UUID userId = SecurityContextHelper.getCurrentUserId()
 *     .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
 * 
 * // Check if user is a teacher
 * if (SecurityContextHelper.isTeacher()) {
 *     // Teacher-specific logic
 * }
 * </pre>
 * 
 * @author AIClass API Team
 */
@Component
public class SecurityContextHelper {

    /**
     * Get the currently authenticated user from the security context.
     * 
     * @return Optional containing the AuthenticatedUser, or empty if not authenticated
     */
    public static Optional<AuthenticatedUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser) {
            return Optional.of((AuthenticatedUser) principal);
        }
        
        return Optional.empty();
    }

    /**
     * Get the current user's internal database ID.
     * 
     * @return Optional containing the user ID, or empty if not available
     */
    public static Optional<UUID> getCurrentUserId() {
        return getCurrentUser()
                .map(AuthenticatedUser::getUserId)
                .filter(id -> id != null);
    }

    /**
     * Get the current user's Supabase Auth UUID.
     * 
     * @return Optional containing the auth user ID, or empty if not authenticated
     */
    public static Optional<UUID> getCurrentAuthUserId() {
        return getCurrentUser()
                .map(AuthenticatedUser::getAuthUserId);
    }

    /**
     * Get the current user's email.
     * 
     * @return Optional containing the email, or empty if not authenticated
     */
    public static Optional<String> getCurrentUserEmail() {
        return getCurrentUser()
                .map(AuthenticatedUser::getEmail);
    }

    /**
     * Get the current user's role.
     * 
     * @return Optional containing the role, or empty if not available
     */
    public static Optional<UserRole> getCurrentUserRole() {
        return getCurrentUser()
                .map(AuthenticatedUser::getRole)
                .filter(role -> role != null);
    }

    /**
     * Check if the current user is authenticated.
     * 
     * @return true if a user is authenticated
     */
    public static boolean isAuthenticated() {
        return getCurrentUser().isPresent();
    }

    /**
     * Check if the current user has the TEACHER role.
     * 
     * @return true if the user is a teacher
     */
    public static boolean isTeacher() {
        return getCurrentUser()
                .map(AuthenticatedUser::isTeacher)
                .orElse(false);
    }

    /**
     * Check if the current user has the STUDENT role.
     * 
     * @return true if the user is a student
     */
    public static boolean isStudent() {
        return getCurrentUser()
                .map(AuthenticatedUser::isStudent)
                .orElse(false);
    }

    /**
     * Check if the current user has a specific role.
     * 
     * @param role the role to check
     * @return true if the user has the specified role
     */
    public static boolean hasRole(UserRole role) {
        return getCurrentUserRole()
                .map(userRole -> userRole.equals(role))
                .orElse(false);
    }

    /**
     * Require that a user is authenticated, throwing an exception if not.
     * 
     * @return the authenticated user
     * @throws IllegalStateException if no user is authenticated
     */
    public static AuthenticatedUser requireAuthentication() {
        return getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("No authenticated user in context"));
    }

    /**
     * Require that the current user has a specific role.
     * 
     * @param requiredRole the required role
     * @throws IllegalStateException if user doesn't have the required role
     */
    public static void requireRole(UserRole requiredRole) {
        AuthenticatedUser user = requireAuthentication();
        if (!user.hasRole(requiredRole)) {
            throw new IllegalStateException(
                String.format("User does not have required role: %s", requiredRole)
            );
        }
    }
}

