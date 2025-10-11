package com.viveek.aiclass.domain.repository;

import com.viveek.aiclass.domain.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Subject entity operations.
 */
@Repository
public interface SubjectRepository extends JpaRepository<Subject, UUID> {

    Optional<Subject> findByCode(String code);

    boolean existsByCode(String code);
}

