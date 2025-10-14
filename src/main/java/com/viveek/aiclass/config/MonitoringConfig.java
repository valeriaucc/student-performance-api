package com.viveek.aiclass.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Configuration for application monitoring and observability.
 * Sets up Prometheus metrics, custom tags, and meter registry customizations.
 */
@Configuration
public class MonitoringConfig {

    @Value("${spring.application.name:aiclass}")
    private String applicationName;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    /**
     * Customizes the MeterRegistry with common tags for all metrics.
     * These tags help identify metrics by application and environment.
     *
     * @return MeterRegistryCustomizer
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
                Arrays.asList(
                        Tag.of("application", applicationName),
                        Tag.of("environment", activeProfile)
                )
        );
    }

    /**
     * Custom meter binder for business-specific metrics.
     * Registers custom gauges and counters for application-specific monitoring.
     *
     * @return MeterBinder
     */
    @Bean
    public MeterBinder customMetrics() {
        return registry -> {
            // These will be populated by service classes as needed
            // Example: Counter for API calls, Gauge for active sessions, etc.
        };
    }
}

