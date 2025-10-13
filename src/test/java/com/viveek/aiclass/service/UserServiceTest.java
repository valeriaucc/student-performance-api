package com.viveek.aiclass.service;

import com.viveek.aiclass.domain.model.User;
import com.viveek.aiclass.domain.model.enums.UserRole;
import com.viveek.aiclass.domain.repository.UserRepository;
import com.viveek.aiclass.dto.request.CreateUserRequest;
import com.viveek.aiclass.dto.request.UpdateUserRequest;
import com.viveek.aiclass.dto.response.UserResponse;
import com.viveek.aiclass.exception.ResourceNotFoundException;
import com.viveek.aiclass.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .authUserId(UUID.randomUUID())
                .email("test@example.com")
                .fullName("Test User")
                .role(UserRole.STUDENT)
                .build();
        ReflectionTestUtils.setField(testUser, "id", userId);
    }

    @Test
    void createUser_WhenValidRequest_ShouldCreateUser() {
        CreateUserRequest request = CreateUserRequest.builder()
                .authUserId(UUID.randomUUID())
                .email("new@example.com")
                .fullName("New User")
                .role(UserRole.STUDENT)
                .build();

        User savedUser = User.builder()
                .authUserId(request.getAuthUserId())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(request.getRole())
                .build();
        ReflectionTestUtils.setField(savedUser, "id", UUID.randomUUID());

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(savedUser.getId(), response.getId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WhenValidRequest_ShouldUpdateUser() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .fullName("Updated Name")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.updateUser(userId, request);

        assertNotNull(response);
        verify(userRepository).save(testUser);
    }

    @Test
    void getUserById_WhenExists_ShouldReturnUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getUserById(userId);

        assertNotNull(response);
        assertEquals(userId, response.getId());
    }

    @Test
    void getUserById_WhenNotFound_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(userId));
    }

    @Test
    void getUserByAuthUserId_WhenExists_ShouldReturnUser() {
        UUID authUserId = testUser.getAuthUserId();
        when(userRepository.findByAuthUserId(authUserId)).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getUserByAuthUserId(authUserId);

        assertNotNull(response);
        assertEquals(authUserId, response.getAuthUserId());
    }

    @Test
    void getAllUsers_WithPagination_ShouldReturnPagedUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);
        
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<UserResponse> response = userService.getAllUsers(pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void getUsersByRole_WithPagination_ShouldReturnPagedUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);
        
        when(userRepository.findByRole(UserRole.STUDENT, pageable)).thenReturn(userPage);

        Page<UserResponse> response = userService.getUsersByRole(UserRole.STUDENT, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void deleteUser_WhenExists_ShouldDeleteUser() {
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_WhenNotFound_ShouldThrowException() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteUser(userId));
        verify(userRepository, never()).deleteById(any());
    }
}
