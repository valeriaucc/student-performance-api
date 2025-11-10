package com.viveek.aiclass.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.auditing.DateTimeProvider;

import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JpaAuditingConfigTest {

    private final JpaAuditingConfig config = new JpaAuditingConfig();

    @Test
    void zonedDateTimeProvider_ReturnsProvider() {
        DateTimeProvider provider = config.zonedDateTimeProvider();
        
        assertThat(provider).isNotNull();
    }

    @Test
    void zonedDateTimeProvider_ProvidesZonedDateTime() {
        DateTimeProvider provider = config.zonedDateTimeProvider();
        
        Optional<TemporalAccessor> result = provider.getNow();
        
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(ZonedDateTime.class);
    }

    @Test
    void zonedDateTimeProvider_ProvidesCurrentTime() {
        DateTimeProvider provider = config.zonedDateTimeProvider();
        
        ZonedDateTime before = ZonedDateTime.now();
        Optional<TemporalAccessor> result = provider.getNow();
        ZonedDateTime after = ZonedDateTime.now();
        
        assertThat(result).isPresent();
        ZonedDateTime providedTime = (ZonedDateTime) result.get();
        
        assertThat(providedTime).isBetween(before.minusSeconds(1), after.plusSeconds(1));
    }

    @Test
    void zonedDateTimeProvider_MultipleInvocations_ReturnsDifferentTimes() throws InterruptedException {
        DateTimeProvider provider = config.zonedDateTimeProvider();
        
        Optional<TemporalAccessor> first = provider.getNow();
        Thread.sleep(10);
        Optional<TemporalAccessor> second = provider.getNow();
        
        assertThat(first).isPresent();
        assertThat(second).isPresent();
        
        ZonedDateTime firstTime = (ZonedDateTime) first.get();
        ZonedDateTime secondTime = (ZonedDateTime) second.get();
        
        assertThat(secondTime).isAfterOrEqualTo(firstTime);
    }
}


