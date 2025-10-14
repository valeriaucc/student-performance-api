package com.viveek.aiclass.config.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * Custom health indicator for application-level metrics.
 * Monitors JVM memory usage and provides health status based on thresholds.
 */
@Slf4j
@Component
public class ApplicationHealthIndicator implements HealthIndicator {

    private static final double MEMORY_THRESHOLD_WARNING = 0.80; // 80%
    private static final double MEMORY_THRESHOLD_CRITICAL = 0.95; // 95%

    @Override
    public Health health() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            
            long used = heapUsage.getUsed();
            long max = heapUsage.getMax();
            double usageRatio = (double) used / max;
            
            // Calculate percentages for readability
            long usedMB = used / (1024 * 1024);
            long maxMB = max / (1024 * 1024);
            int usagePercent = (int) (usageRatio * 100);
            
            Health.Builder healthBuilder;
            String status;
            
            if (usageRatio >= MEMORY_THRESHOLD_CRITICAL) {
                healthBuilder = Health.down();
                status = "CRITICAL - Memory usage critically high";
            } else if (usageRatio >= MEMORY_THRESHOLD_WARNING) {
                healthBuilder = Health.status("WARNING");
                status = "WARNING - Memory usage high";
            } else {
                healthBuilder = Health.up();
                status = "OK";
            }
            
            return healthBuilder
                    .withDetail("memory", String.format("%dMB / %dMB (%d%%)", usedMB, maxMB, usagePercent))
                    .withDetail("status", status)
                    .withDetail("javaVersion", System.getProperty("java.version"))
                    .withDetail("jvmVendor", System.getProperty("java.vendor"))
                    .build();
                    
        } catch (Exception e) {
            log.error("Application health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}

