package com.viveek.aiclass.service.impl;

import com.viveek.aiclass.domain.model.User;
import com.viveek.aiclass.domain.model.enums.UserRole;
import com.viveek.aiclass.domain.repository.UserRepository;
import com.viveek.aiclass.dto.request.CreateUserRequest;
import com.viveek.aiclass.dto.request.UpdateUserRequest;
import com.viveek.aiclass.dto.response.UserResponse;
import com.viveek.aiclass.exception.ResourceAlreadyExistsException;
import com.viveek.aiclass.exception.ResourceNotFoundException;
import com.viveek.aiclass.mapper.EntityMapper;
import com.viveek.aiclass.service.UserService;
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

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user with email={}, role={}", request.getEmail(), request.getRole());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("User creation failed: email already exists - {}", request.getEmail());
            throw new ResourceAlreadyExistsException("User", "email", request.getEmail());
        }

        if (userRepository.existsByAuthUserId(request.getAuthUserId())) {
            log.warn("User creation failed: authUserId already exists - {}", request.getAuthUserId());
            throw new ResourceAlreadyExistsException("User", "authUserId", request.getAuthUserId());
        }

        User user = EntityMapper.toUser(request);
        User savedUser = userRepository.save(user);
        log.debug("User created successfully: id={}, email={}", savedUser.getId(), savedUser.getEmail());
        return EntityMapper.toUserResponse(savedUser);
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
            if (!request.getEmail().equals(user.getEmail()) && 
                userRepository.existsByEmail(request.getEmail())) {
                log.warn("User update failed: email already exists - {}", request.getEmail());
                throw new ResourceAlreadyExistsException("User", "email", request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getMetadata() != null) {
            user.setMetadata(request.getMetadata());
        }

        User updatedUser = userRepository.save(user);
        log.debug("User updated successfully: id={}", updatedUser.getId());
        return EntityMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return EntityMapper.toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByAuthUserId(UUID authUserId) {
        User user = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "authUserId", authUserId));
        return EntityMapper.toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return EntityMapper.toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users (non-paginated)");
        return userRepository.findAll().stream()
                .map(EntityMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Fetching users with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable)
                .map(EntityMapper::toUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(UserRole role) {
        log.debug("Fetching users by role: role={} (non-paginated)", role);
        return userRepository.findByRole(role).stream()
                .map(EntityMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByRole(UserRole role, Pageable pageable) {
        log.debug("Fetching users by role with pagination: role={}, page={}, size={}", 
                  role, pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findByRole(role, pageable)
                .map(EntityMapper::toUserResponse);
    }

    @Override
    public void deleteUser(UUID id) {
        log.info("Deleting user: id={}", id);
        if (!userRepository.existsById(id)) {
            log.warn("User deletion failed: user not found - id={}", id);
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
        log.debug("User deleted successfully: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByAuthUserId(UUID authUserId) {
        return userRepository.existsByAuthUserId(authUserId);
    }
}

