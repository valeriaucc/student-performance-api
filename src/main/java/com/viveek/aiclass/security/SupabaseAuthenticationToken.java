package com.viveek.aiclass.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Custom authentication token for Supabase JWT authentication.
 * Wraps an AuthenticatedUser as the principal.
 * 
 * This token is created after successful JWT validation and contains
 * the authenticated user's information.
 * 
 * @author AIClass API Team
 */
public class SupabaseAuthenticationToken extends AbstractAuthenticationToken {

    private final AuthenticatedUser principal;

    /**
     * Creates an authenticated token with the given user details.
     * 
     * @param principal the authenticated user
     * @param authorities the user's granted authorities
     */
    public SupabaseAuthenticationToken(
            AuthenticatedUser principal,
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        // Credentials (JWT token) are not stored after authentication
        return null;
    }

    @Override
    public AuthenticatedUser getPrincipal() {
        return principal;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        if (!authenticated) {
            super.setAuthenticated(false);
        } else {
            throw new IllegalArgumentException(
                "Cannot set this token to trusted - use constructor which takes authorities");
        }
    }
}

