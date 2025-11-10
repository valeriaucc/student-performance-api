package com.viveek.aiclass.domain.repository;

import com.viveek.aiclass.domain.model.User;
import com.viveek.aiclass.domain.model.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    Page<User> findByRole(UserRole role, Pageable pageable);

    boolean existsByEmail(String email);

    boolean existsByAuthUserId(UUID authUserId);

    /**
     * Search users by full name or email (case-insensitive) with optional role filter.
     * 
     * @param searchTerm Search term to match against fullName or email (case-insensitive).
     *                   If null or empty, no search filtering is applied.
     * @param role Optional role filter (if null, searches all roles)
     * @param pageable Pagination parameters
     * @return Page of users matching the search criteria
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:searchTerm IS NULL OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:role IS NULL OR u.role = :role)")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, 
                           @Param("role") UserRole role, 
                           Pageable pageable);
}

