package com.viveek.aiclass.service;

import com.viveek.aiclass.domain.model.enums.UserRole;
import com.viveek.aiclass.dto.request.CreateUserRequest;
import com.viveek.aiclass.dto.request.UpdateUserRequest;
import com.viveek.aiclass.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for User operations.
 */
public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(UUID id, UpdateUserRequest request);

    UserResponse getUserById(UUID id);

    UserResponse getUserByAuthUserId(UUID authUserId);

    UserResponse getUserByEmail(String email);

    List<UserResponse> getAllUsers();

    Page<UserResponse> getAllUsers(Pageable pageable);

    List<UserResponse> getUsersByRole(UserRole role);

    Page<UserResponse> getUsersByRole(UserRole role, Pageable pageable);

    /**
     * Search users by search term (name or email) with optional role filter.
     * 
     * @param search Search term to match against fullName or email (case-insensitive)
     * @param role Optional role filter
     * @param pageable Pagination parameters
     * @return Page of users matching the search criteria
     */
    Page<UserResponse> searchUsers(String search, UserRole role, Pageable pageable);

    void deleteUser(UUID id);

    boolean existsByEmail(String email);

    boolean existsByAuthUserId(UUID authUserId);
}

