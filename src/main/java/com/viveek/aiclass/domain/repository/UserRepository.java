package com.viveek.aiclass.domain.repository;

import com.viveek.aiclass.domain.model.User;
import com.viveek.aiclass.domain.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByAuthUserId(UUID authUserId);

    List<User> findByRole(UserRole role);

    boolean existsByEmail(String email);

    boolean existsByAuthUserId(UUID authUserId);
}

