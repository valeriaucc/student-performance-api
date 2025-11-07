package com.viveek.aiclass.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Base entity class providing common fields for all domain entities.
 * Includes UUID primary key, audit timestamps, and soft delete support.
 * 
 * <p><strong>Soft Delete Implementation:</strong></p>
 * <ul>
 *   <li>DELETE operations execute UPDATE setting deleted_at timestamp (via @SQLDelete on child entities)</li>
 *   <li>All queries automatically filter WHERE deleted_at IS NULL (via @Where)</li>
 *   <li>Soft-deleted records remain in database but are invisible to application</li>
 *   <li>Use restore() method to undelete records</li>
 * </ul>
 * 
 * @see org.hibernate.annotations.SQLDelete
 * @see org.hibernate.annotations.Where
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
public abstract class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = ZonedDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }

    /**
     * Soft delete this entity by setting the deletedAt timestamp.
     */
    public void softDelete() {
        this.deletedAt = ZonedDateTime.now();
    }

    /**
     * Restore a soft-deleted entity by clearing the deletedAt timestamp.
     */
    public void restore() {
        this.deletedAt = null;
    }

    /**
     * Check if this entity has been soft-deleted.
     * 
     * @return true if entity is soft-deleted, false otherwise
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

