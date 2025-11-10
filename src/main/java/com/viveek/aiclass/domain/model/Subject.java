package com.viveek.aiclass.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.List;

/**
 * Subject entity representing academic subjects/courses.
 * Maps to the 'subjects' table in the database.
 * 
 * Uses soft delete: DELETE operations will set deleted_at instead of removing the record.
 * Soft-deleted records are automatically filtered from queries via @Where annotation.
 */
@Entity
@Table(name = "subjects")
@SQLDelete(sql = "UPDATE subjects SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject extends BaseEntity {

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    // Relationships
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Class> classes;
}

