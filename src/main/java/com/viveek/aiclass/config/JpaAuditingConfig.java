package com.viveek.aiclass.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

/**
 * Configuration class to enable JPA auditing with ZonedDateTime support.
 * This enables automatic population of @CreatedDate and @LastModifiedDate fields using ZonedDateTime.
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "zonedDateTimeProvider")
public class JpaAuditingConfig {

    /**
     * Custom DateTimeProvider that returns ZonedDateTime instead of LocalDateTime.
     * This ensures @CreatedDate and @LastModifiedDate use ZonedDateTime type.
     */
    @Bean
    public DateTimeProvider zonedDateTimeProvider() {
        return () -> Optional.of(ZonedDateTime.now());
    }
}

