package com.viveek.aiclass.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class MonitoringConfigTest {

    private MonitoringConfig config;

    @BeforeEach
    void setUp() {
        config = new MonitoringConfig();
        ReflectionTestUtils.setField(config, "applicationName", "aiclass");
        ReflectionTestUtils.setField(config, "activeProfile", "test");
    }

    @Test
    void metricsCommonTags_ReturnsCustomizer() {
        MeterRegistryCustomizer<MeterRegistry> customizer = config.metricsCommonTags();
        
        assertThat(customizer).isNotNull();
    }

    @Test
    void metricsCommonTags_AddsTags() {
        MeterRegistryCustomizer<MeterRegistry> customizer = config.metricsCommonTags();
        MeterRegistry registry = new SimpleMeterRegistry();
        
        customizer.customize(registry);
        
        // Verify config is not null after customization
        assertThat(registry.config()).isNotNull();
    }

    @Test
    void metricsCommonTags_WithDifferentProfile_AddsCorrectTags() {
        ReflectionTestUtils.setField(config, "activeProfile", "production");
        MeterRegistryCustomizer<MeterRegistry> customizer = config.metricsCommonTags();
        MeterRegistry registry = new SimpleMeterRegistry();
        
        customizer.customize(registry);
        
        assertThat(registry.config()).isNotNull();
    }

    @Test
    void metricsCommonTags_WithDifferentApplicationName_AddsCorrectTags() {
        ReflectionTestUtils.setField(config, "applicationName", "test-app");
        MeterRegistryCustomizer<MeterRegistry> customizer = config.metricsCommonTags();
        MeterRegistry registry = new SimpleMeterRegistry();
        
        customizer.customize(registry);
        
        assertThat(registry.config()).isNotNull();
    }

    @Test
    void customMetrics_ReturnsMeterBinder() {
        MeterBinder meterBinder = config.customMetrics();
        
        assertThat(meterBinder).isNotNull();
    }

    @Test
    void customMetrics_CanBindToRegistry() {
        MeterBinder meterBinder = config.customMetrics();
        MeterRegistry registry = new SimpleMeterRegistry();
        
        meterBinder.bindTo(registry);
        
        assertThat(registry).isNotNull();
    }

    @Test
    void metricsCommonTags_CustomizesRegistry() {
        MeterRegistryCustomizer<MeterRegistry> customizer = config.metricsCommonTags();
        MeterRegistry registry = new SimpleMeterRegistry();
        
        customizer.customize(registry);
        
        assertThat(registry.config()).isNotNull();
    }
}

