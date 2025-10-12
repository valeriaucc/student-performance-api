package com.viveek.aiclass.config;

import com.viveek.aiclass.security.SupabaseJwtAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for the AIClass API.
 * 
 * Configures JWT-based authentication using Supabase Auth tokens, CORS policies,
 * and authorization rules for different endpoints.
 * 
 * Features:
 * - JWT validation using Supabase public key (JWK Set)
 * - Stateless session management (no server-side sessions)
 * - CORS configuration for frontend integration
 * - Role-based access control (RBAC)
 * - Public endpoints for documentation and health checks
 * 
 * @author AIClass API Team
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final SupabaseJwtAuthenticationConverter jwtAuthenticationConverter;

    @Value("${supabase.jwt.secret}")
    private String jwtSecret;

    @Value("${security.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Main security filter chain configuration.
     * 
     * @param http the HttpSecurity to configure
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless REST API
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/actuator/health",
                    "/actuator/info"
                ).permitAll()
                
                // User creation endpoint - allow authenticated users to create their profile
                .requestMatchers(HttpMethod.POST, "/api/users").authenticated()
                
                // All other API endpoints require authentication
                .requestMatchers("/api/**").authenticated()
                
                // Deny all other requests by default
                .anyRequest().denyAll()
            )
            
            // Configure JWT-based OAuth2 resource server
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter)
                )
            )
            
            // Stateless session - no server-side sessions
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    /**
     * JWT decoder bean that validates tokens using Supabase's JWT secret.
     * 
     * Supabase uses HS256 (HMAC with SHA-256) symmetric key algorithm,
     * which means we validate tokens using a shared secret rather than
     * fetching public keys from a JWK Set endpoint.
     * 
     * @return configured JwtDecoder
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Create secret key from the JWT secret string
        SecretKey secretKey = new SecretKeySpec(
            jwtSecret.getBytes(),
            "HmacSHA256"
        );
        
        // Build JWT decoder with HS256 algorithm and secret key
        return NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * CORS configuration to allow frontend applications to access the API.
     * 
     * Configuration includes:
     * - Allowed origins (configurable via properties)
     * - Allowed HTTP methods (GET, POST, PUT, DELETE, OPTIONS)
     * - Allowed headers (including Authorization)
     * - Credentials support
     * 
     * @return configured CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Parse allowed origins from comma-separated property
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        configuration.setAllowedOrigins(origins);
        
        // Allow common HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.OPTIONS.name()
        ));
        
        // Allow common headers including Authorization
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With",
            "Cache-Control"
        ));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);
        
        // Apply CORS configuration to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}

