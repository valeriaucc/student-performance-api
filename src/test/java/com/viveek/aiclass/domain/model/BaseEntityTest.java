package com.viveek.aiclass.domain.model;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BaseEntityTest {

    // Create a concrete implementation for testing
    private static class TestEntity extends BaseEntity {
    }

    @Test
    void onCreate_SetsTimestamps() {
        TestEntity entity = new TestEntity();
        
        entity.onCreate();
        
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    void onCreate_DoesNotOverrideExistingTimestamps() {
        TestEntity entity = new TestEntity();
        ZonedDateTime existingCreatedAt = ZonedDateTime.now().minusDays(1);
        entity.setCreatedAt(existingCreatedAt);
        
        entity.onCreate();
        
        assertThat(entity.getCreatedAt()).isEqualTo(existingCreatedAt);
    }

    @Test
    void onUpdate_UpdatesTimestamp() throws InterruptedException {
        TestEntity entity = new TestEntity();
        entity.onCreate();
        ZonedDateTime originalUpdatedAt = entity.getUpdatedAt();
        
        Thread.sleep(10); // Small delay to ensure different timestamp
        entity.onUpdate();
        
        assertThat(entity.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void softDelete_SetsDeletedAt() {
        TestEntity entity = new TestEntity();
        
        entity.softDelete();
        
        assertThat(entity.getDeletedAt()).isNotNull();
        assertThat(entity.isDeleted()).isTrue();
    }

    @Test
    void restore_ClearsDeletedAt() {
        TestEntity entity = new TestEntity();
        entity.softDelete();
        
        entity.restore();
        
        assertThat(entity.getDeletedAt()).isNull();
        assertThat(entity.isDeleted()).isFalse();
    }

    @Test
    void isDeleted_ReturnsFalseWhenNotDeleted() {
        TestEntity entity = new TestEntity();
        
        assertThat(entity.isDeleted()).isFalse();
    }

    @Test
    void equals_SameId_ReturnsTrue() {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        UUID id = UUID.randomUUID();
        
        entity1.setId(id);
        entity2.setId(id);
        
        assertThat(entity1).isEqualTo(entity2);
    }

    @Test
    void equals_DifferentId_ReturnsFalse() {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        
        entity1.setId(UUID.randomUUID());
        entity2.setId(UUID.randomUUID());
        
        assertThat(entity1).isNotEqualTo(entity2);
    }

    @Test
    void equals_NullId_ReturnsFalse() {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        
        assertThat(entity1).isNotEqualTo(entity2);
    }

    @Test
    void equals_SameObject_ReturnsTrue() {
        TestEntity entity = new TestEntity();
        
        assertThat(entity).isEqualTo(entity);
    }

    @Test
    void equals_Null_ReturnsFalse() {
        TestEntity entity = new TestEntity();
        
        assertThat(entity).isNotEqualTo(null);
    }

    @Test
    void equals_DifferentClass_ReturnsFalse() {
        TestEntity entity = new TestEntity();
        String other = "not an entity";
        
        assertThat(entity).isNotEqualTo(other);
    }

    @Test
    void hashCode_ConsistentForSameClass() {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
    }
}

