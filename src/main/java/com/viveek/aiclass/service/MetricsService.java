package com.viveek.aiclass.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for recording custom business metrics.
 * Provides methods to track API operations, errors, and performance.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    /**
     * Records a successful API operation.
     *
     * @param operation the operation name (e.g., "user.create", "grade.update")
     */
    public void recordOperation(String operation) {
        Counter.builder("api.operations")
                .tag("operation", operation)
                .tag("status", "success")
                .description("Count of successful API operations")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Records a failed API operation.
     *
     * @param operation the operation name
     * @param errorType the error type or exception class name
     */
    public void recordError(String operation, String errorType) {
        Counter.builder("api.operations")
                .tag("operation", operation)
                .tag("status", "error")
                .tag("error_type", errorType)
                .description("Count of failed API operations")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Records the execution time of an operation.
     *
     * @param operation the operation name
     * @param durationMs the duration in milliseconds
     */
    public void recordExecutionTime(String operation, long durationMs) {
        Timer.builder("api.operation.duration")
                .tag("operation", operation)
                .description("Duration of API operations in milliseconds")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Records a database query execution.
     *
     * @param queryType the type of query (e.g., "select", "insert", "update", "delete")
     * @param entity the entity being queried
     */
    public void recordDatabaseQuery(String queryType, String entity) {
        Counter.builder("database.queries")
                .tag("query_type", queryType)
                .tag("entity", entity)
                .description("Count of database queries")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Records cache hit or miss.
     *
     * @param cacheName the cache name
     * @param hit true if cache hit, false if cache miss
     */
    public void recordCacheAccess(String cacheName, boolean hit) {
        Counter.builder("cache.access")
                .tag("cache", cacheName)
                .tag("result", hit ? "hit" : "miss")
                .description("Count of cache accesses")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Records authentication attempts.
     *
     * @param success true if authentication successful, false otherwise
     */
    public void recordAuthAttempt(boolean success) {
        Counter.builder("auth.attempts")
                .tag("result", success ? "success" : "failure")
                .description("Count of authentication attempts")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Records soft delete operations.
     *
     * @param entity the entity type being soft-deleted
     */
    public void recordSoftDelete(String entity) {
        Counter.builder("soft.deletes")
                .tag("entity", entity)
                .description("Count of soft delete operations")
                .register(meterRegistry)
                .increment();
    }
}


