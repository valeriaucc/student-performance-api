package com.viveek.aiclass.service.impl;

import com.viveek.aiclass.domain.model.User;
import com.viveek.aiclass.domain.model.enums.UserRole;
import com.viveek.aiclass.domain.repository.UserRepository;
import com.viveek.aiclass.dto.request.CreateUserRequest;
import com.viveek.aiclass.dto.request.UpdateUserRequest;
import com.viveek.aiclass.dto.response.UserResponse;
import com.viveek.aiclass.exception.ResourceAlreadyExistsException;
import com.viveek.aiclass.exception.ResourceNotFoundException;
import com.viveek.aiclass.mapper.UserMapper;
import com.viveek.aiclass.service.UserService;
import com.viveek.aiclass.util.EmailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of UserService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        // Normalize email to lowercase and trim whitespace
        String normalizedEmail = EmailUtils.normalizeEmail(request.getEmail());
        request.setEmail(normalizedEmail);
        
        log.info("Creating user with email={}, role={}", normalizedEmail, request.getRole());
        
        if (userRepository.existsByEmail(normalizedEmail)) {
            log.warn("User creation failed: email already exists - {}", normalizedEmail);
            throw new ResourceAlreadyExistsException("User", "email", normalizedEmail);
        }

        if (userRepository.existsByAuthUserId(request.getAuthUserId())) {
            log.warn("User creation failed: authUserId already exists - {}", request.getAuthUserId());
            throw new ResourceAlreadyExistsException("User", "authUserId", request.getAuthUserId());
        }

        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);
        log.debug("User created successfully: id={}, email={}", savedUser.getId(), savedUser.getEmail());
        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        log.info("Updating user: id={}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            // Normalize email to lowercase and trim whitespace
            String normalizedEmail = EmailUtils.normalizeEmail(request.getEmail());
            
            if (!normalizedEmail.equals(user.getEmail()) && 
                userRepository.existsByEmail(normalizedEmail)) {
                log.warn("User update failed: email already exists - {}", normalizedEmail);
                throw new ResourceAlreadyExistsException("User", "email", normalizedEmail);
            }
            user.setEmail(normalizedEmail);
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getMetadata() != null) {
            user.setMetadata(request.getMetadata());
        }

        User updatedUser = userRepository.save(user);
        log.debug("User updated successfully: id={}", updatedUser.getId());
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByAuthUserId(UUID authUserId) {
        User user = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "authUserId", authUserId));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        String normalizedEmail = EmailUtils.normalizeEmail(email);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", normalizedEmail));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users (non-paginated)");
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Fetching users with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(UserRole role) {
        log.debug("Fetching users by role: role={} (non-paginated)", role);
        return userRepository.findByRole(role).stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByRole(UserRole role, Pageable pageable) {
        log.debug("Fetching users by role with pagination: role={}, page={}, size={}", 
                  role, pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findByRole(role, pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String search, UserRole role, Pageable pageable) {
        log.debug("Searching users: search={}, role={}, page={}, size={}", 
                  search, role, pageable.getPageNumber(), pageable.getPageSize());
        
        // Normalize search term: trim whitespace, set to null if empty
        String searchTerm = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        
        return userRepository.searchUsers(searchTerm, role, pageable)
                .map(userMapper::toResponse);
    }

    @Override
    public void deleteUser(UUID id) {
        log.info("Soft deleting user: id={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User deletion failed: user not found - id={}", id);
                    return new ResourceNotFoundException("User", "id", id);
                });
        // Use repository.delete() to trigger @SQLDelete annotation
        userRepository.delete(user);
        log.debug("User soft deleted successfully: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        String normalizedEmail = EmailUtils.normalizeEmail(email);
        return userRepository.existsByEmail(normalizedEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByAuthUserId(UUID authUserId) {
        return userRepository.existsByAuthUserId(authUserId);
    }
}

