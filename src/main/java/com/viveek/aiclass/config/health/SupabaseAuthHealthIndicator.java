package com.viveek.aiclass.config.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Custom health indicator for Supabase authentication service.
 * Checks if the Supabase Auth service is reachable and responsive.
 */
@Slf4j
@Component
public class SupabaseAuthHealthIndicator implements HealthIndicator {

    @Value("${supabase.url:#{null}}")
    private String supabaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Health health() {
        if (supabaseUrl == null || supabaseUrl.isEmpty()) {
            return Health.unknown()
                    .withDetail("auth", "Supabase")
                    .withDetail("status", "Not configured")
                    .build();
        }

        try {
            // Simple connectivity check - attempting to reach Supabase endpoint
            // In production, you might want a more specific health endpoint
            String healthUrl = supabaseUrl + "/auth/v1/health";
            
            restTemplate.getForObject(healthUrl, String.class);
            
            return Health.up()
                    .withDetail("auth", "Supabase")
                    .withDetail("url", supabaseUrl)
                    .withDetail("status", "Service reachable")
                    .build();
        } catch (Exception e) {
            log.warn("Supabase auth health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("auth", "Supabase")
                    .withDetail("url", supabaseUrl)
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}


