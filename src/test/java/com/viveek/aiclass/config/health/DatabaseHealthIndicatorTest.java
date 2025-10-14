package com.viveek.aiclass.config.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseHealthIndicatorTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private DatabaseHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new DatabaseHealthIndicator(jdbcTemplate);
    }

    @Test
    void health_DatabaseUp_ReturnsUp() {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        
        Health health = healthIndicator.health();
        
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("database", "PostgreSQL");
        assertThat(health.getDetails()).containsKey("responseTime");
        assertThat(health.getDetails()).containsEntry("status", "Connection successful");
    }

    @Test
    void health_UnexpectedResult_ReturnsDown() {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(999);
        
        Health health = healthIndicator.health();
        
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("error", "Unexpected query result");
    }

    @Test
    void health_NullResult_ReturnsDown() {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(null);
        
        Health health = healthIndicator.health();
        
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void health_DatabaseException_ReturnsDown() {
        RuntimeException exception = new RuntimeException("Connection failed");
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenThrow(exception);
        
        Health health = healthIndicator.health();
        
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("database", "PostgreSQL");
        assertThat(health.getDetails()).containsKey("error");
        assertThat(health.getDetails().get("error").toString()).contains("Connection failed");
    }
}

