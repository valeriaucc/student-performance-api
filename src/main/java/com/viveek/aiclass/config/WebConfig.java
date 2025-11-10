package com.viveek.aiclass.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Web MVC configuration for CORS support.
 * 
 * This configuration provides CORS at the MVC level, complementing the
 * security-level CORS configuration in SecurityConfig. This ensures
 * CORS headers are properly set for all endpoints, including public ones.
 * 
 * The allowed origins are configurable via the `security.cors.allowed-origins`
 * property, which should include:
 * - Production frontend URL (e.g., Netlify deployment)
 * - Local development URLs (e.g., http://localhost:3000)
 * 
 * @author AIClass API Team
 */
@Configuration
public class WebConfig {

    @Value("${security.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * CORS configuration for MVC endpoints.
     * 
     * This configuration:
     * - Applies to all API endpoints (/api/**)
     * - Allows specified origins (configurable via properties)
     * - Allows all HTTP methods (GET, POST, PUT, DELETE, OPTIONS)
     * - Allows all headers (needed for Authorization, Content-Type, etc.)
     * - Allows credentials (cookies, authorization headers)
     * 
     * @return WebMvcConfigurer with CORS configuration
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Parse allowed origins from comma-separated property
                List<String> origins = Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
                
                registry.addMapping("/api/**") // Apply to all API endpoints
                        .allowedOrigins(origins.toArray(new String[0]))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders(
                            "Authorization",
                            "Content-Type",
                            "Accept",
                            "X-Requested-With",
                            "Cache-Control",
                            "Origin",
                            "Access-Control-Request-Method",
                            "Access-Control-Request-Headers",
                            "X-CSRF-TOKEN",
                            "X-Auth-Token"
                        ) // Explicit headers (needed when allowCredentials is true)
                        .allowCredentials(true) // Allow credentials (cookies, tokens)
                        .maxAge(3600); // Cache preflight response for 1 hour
            }
        };
    }
}

