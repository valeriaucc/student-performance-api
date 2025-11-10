package com.viveek.aiclass.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsServiceTest {

    private MeterRegistry meterRegistry;
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new MetricsService(meterRegistry);
    }

    @Test
    void recordOperation_Success_IncrementsCounter() {
        metricsService.recordOperation("user.create");
        
        Counter counter = meterRegistry.find("api.operations")
                .tag("operation", "user.create")
                .tag("status", "success")
                .counter();
        
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void recordOperation_MultipleOperations_IncrementsCounters() {
        metricsService.recordOperation("user.create");
        metricsService.recordOperation("user.create");
        metricsService.recordOperation("grade.update");
        
        Counter userCounter = meterRegistry.find("api.operations")
                .tag("operation", "user.create")
                .tag("status", "success")
                .counter();
        
        Counter gradeCounter = meterRegistry.find("api.operations")
                .tag("operation", "grade.update")
                .tag("status", "success")
                .counter();
        
        assertThat(userCounter.count()).isEqualTo(2.0);
        assertThat(gradeCounter.count()).isEqualTo(1.0);
    }

    @Test
    void recordError_Error_IncrementsCounterWithErrorTag() {
        metricsService.recordError("user.create", "ValidationException");
        
        Counter counter = meterRegistry.find("api.operations")
                .tag("operation", "user.create")
                .tag("status", "error")
                .tag("error_type", "ValidationException")
                .counter();
        
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void recordExecutionTime_RecordsTimer() {
        metricsService.recordExecutionTime("user.create", 150L);
        
        Timer timer = meterRegistry.find("api.operation.duration")
                .tag("operation", "user.create")
                .timer();
        
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(150.0);
    }

    @Test
    void recordDatabaseQuery_IncrementsQueryCounter() {
        metricsService.recordDatabaseQuery("select", "User");
        
        Counter counter = meterRegistry.find("database.queries")
                .tag("query_type", "select")
                .tag("entity", "User")
                .counter();
        
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void recordDatabaseQuery_MultipleQueryTypes_IncrementsRespectiveCounters() {
        metricsService.recordDatabaseQuery("select", "User");
        metricsService.recordDatabaseQuery("insert", "User");
        metricsService.recordDatabaseQuery("select", "User");
        
        Counter selectCounter = meterRegistry.find("database.queries")
                .tag("query_type", "select")
                .tag("entity", "User")
                .counter();
        
        Counter insertCounter = meterRegistry.find("database.queries")
                .tag("query_type", "insert")
                .tag("entity", "User")
                .counter();
        
        assertThat(selectCounter.count()).isEqualTo(2.0);
        assertThat(insertCounter.count()).isEqualTo(1.0);
    }

    @Test
    void recordCacheAccess_Hit_IncrementsCounterWithHitTag() {
        metricsService.recordCacheAccess("userCache", true);
        
        Counter counter = meterRegistry.find("cache.access")
                .tag("cache", "userCache")
                .tag("result", "hit")
                .counter();
        
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void recordCacheAccess_Miss_IncrementsCounterWithMissTag() {
        metricsService.recordCacheAccess("userCache", false);
        
        Counter counter = meterRegistry.find("cache.access")
                .tag("cache", "userCache")
                .tag("result", "miss")
                .counter();
        
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void recordAuthAttempt_Success_IncrementsSuccessCounter() {
        metricsService.recordAuthAttempt(true);
        
        Counter counter = meterRegistry.find("auth.attempts")
                .tag("result", "success")
                .counter();
        
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void recordAuthAttempt_Failure_IncrementsFailureCounter() {
        metricsService.recordAuthAttempt(false);
        
        Counter counter = meterRegistry.find("auth.attempts")
                .tag("result", "failure")
                .counter();
        
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void recordSoftDelete_IncrementsCounter() {
        metricsService.recordSoftDelete("User");
        
        Counter counter = meterRegistry.find("soft.deletes")
                .tag("entity", "User")
                .counter();
        
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void recordSoftDelete_MultipleEntities_IncrementsRespectiveCounters() {
        metricsService.recordSoftDelete("User");
        metricsService.recordSoftDelete("Class");
        metricsService.recordSoftDelete("User");
        
        Counter userCounter = meterRegistry.find("soft.deletes")
                .tag("entity", "User")
                .counter();
        
        Counter classCounter = meterRegistry.find("soft.deletes")
                .tag("entity", "Class")
                .counter();
        
        assertThat(userCounter.count()).isEqualTo(2.0);
        assertThat(classCounter.count()).isEqualTo(1.0);
    }
}


