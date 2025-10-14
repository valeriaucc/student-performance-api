package com.viveek.aiclass.config.health;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationHealthIndicatorTest {

    private final ApplicationHealthIndicator healthIndicator = new ApplicationHealthIndicator();

    @Test
    void health_ReturnsHealthWithMemoryDetails() {
        Health health = healthIndicator.health();
        
        assertThat(health).isNotNull();
        assertThat(health.getDetails()).containsKey("memory");
        assertThat(health.getDetails()).containsKey("status");
        assertThat(health.getDetails()).containsKey("javaVersion");
        assertThat(health.getDetails()).containsKey("jvmVendor");
    }

    @Test
    void health_MemoryDetailsContainCorrectFormat() {
        Health health = healthIndicator.health();
        
        String memory = (String) health.getDetails().get("memory");
        assertThat(memory).matches("\\d+MB / \\d+MB \\(\\d+%\\)");
    }

    @Test
    void health_JavaVersionIsPresent() {
        Health health = healthIndicator.health();
        
        String javaVersion = (String) health.getDetails().get("javaVersion");
        assertThat(javaVersion).isNotNull();
        assertThat(javaVersion).isNotEmpty();
    }

    @Test
    void health_JvmVendorIsPresent() {
        Health health = healthIndicator.health();
        
        String jvmVendor = (String) health.getDetails().get("jvmVendor");
        assertThat(jvmVendor).isNotNull();
        assertThat(jvmVendor).isNotEmpty();
    }

    @Test
    void health_StatusIsNotNull() {
        Health health = healthIndicator.health();
        
        assertThat(health.getStatus()).isNotNull();
    }

    @Test
    void health_StatusDetailsIsPresent() {
        Health health = healthIndicator.health();
        
        String status = (String) health.getDetails().get("status");
        assertThat(status).isNotNull();
        assertThat(status).isIn("OK", "WARNING - Memory usage high", "CRITICAL - Memory usage critically high");
    }

    @Test
    void health_HealthObjectIsCreated() {
        Health health = healthIndicator.health();
        
        assertThat(health).isInstanceOf(Health.class);
    }

    @Test
    void health_StatusIsValid() {
        Health health = healthIndicator.health();
        
        Status status = health.getStatus();
        assertThat(status).isIn(Status.UP, Status.DOWN, Status.UNKNOWN, new Status("WARNING"));
    }
}

