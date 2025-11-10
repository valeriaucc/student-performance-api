package com.viveek.aiclass.config.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class SupabaseAuthHealthIndicatorTest {

    private SupabaseAuthHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new SupabaseAuthHealthIndicator();
    }

    @Test
    void health_NoSupabaseUrl_ReturnsUnknown() {
        ReflectionTestUtils.setField(healthIndicator, "supabaseUrl", null);
        
        Health health = healthIndicator.health();
        
        assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
        assertThat(health.getDetails()).containsEntry("auth", "Supabase");
        assertThat(health.getDetails()).containsEntry("status", "Not configured");
    }

    @Test
    void health_EmptySupabaseUrl_ReturnsUnknown() {
        ReflectionTestUtils.setField(healthIndicator, "supabaseUrl", "");
        
        Health health = healthIndicator.health();
        
        assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
        assertThat(health.getDetails()).containsEntry("auth", "Supabase");
        assertThat(health.getDetails()).containsEntry("status", "Not configured");
    }

    @Test
    void health_InvalidSupabaseUrl_ReturnsDown() {
        ReflectionTestUtils.setField(healthIndicator, "supabaseUrl", "http://invalid-url-that-does-not-exist.local");
        
        Health health = healthIndicator.health();
        
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("auth", "Supabase");
        assertThat(health.getDetails()).containsKey("error");
    }

    @Test
    void health_WithSupabaseUrl_ContainsAuthDetail() {
        ReflectionTestUtils.setField(healthIndicator, "supabaseUrl", "http://localhost:54321");
        
        Health health = healthIndicator.health();
        
        assertThat(health.getDetails()).containsEntry("auth", "Supabase");
    }

    @Test
    void health_WithSupabaseUrl_ContainsUrlDetail() {
        String url = "http://localhost:54321";
        ReflectionTestUtils.setField(healthIndicator, "supabaseUrl", url);
        
        Health health = healthIndicator.health();
        
        assertThat(health.getDetails()).containsEntry("url", url);
    }

    @Test
    void health_ConnectionFailure_ContainsErrorDetail() {
        ReflectionTestUtils.setField(healthIndicator, "supabaseUrl", "http://unreachable.local");
        
        Health health = healthIndicator.health();
        
        assertThat(health.getDetails()).containsKey("error");
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void health_ReturnsHealthObject() {
        Health health = healthIndicator.health();
        
        assertThat(health).isInstanceOf(Health.class);
        assertThat(health).isNotNull();
    }
}


