package com.viveek.aiclass.api.controller;

import com.viveek.aiclass.api.ApiVersions;
import com.viveek.aiclass.constants.SecurityRoles;
import com.viveek.aiclass.domain.model.enums.UserRole;
import com.viveek.aiclass.dto.request.CreateUserRequest;
import com.viveek.aiclass.dto.request.UpdateUserRequest;
import com.viveek.aiclass.dto.response.ApiResponse;
import com.viveek.aiclass.dto.response.PageResponse;
import com.viveek.aiclass.dto.response.UserResponse;
import com.viveek.aiclass.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for User management operations.
 */
@RestController
@RequestMapping(ApiVersions.V1 + "/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs (v1)")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user in the system")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update user", description = "Updates an existing user (users can update their own profile)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('" + SecurityRoles.TEACHER + "')")
    @Operation(summary = "Get user by ID", description = "Retrieves a user by their ID (TEACHER only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/auth/{authUserId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user by auth user ID", description = "Retrieves a user by their Supabase auth user ID (own profile)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByAuthUserId(
            @Parameter(description = "Auth User ID") @PathVariable UUID authUserId) {
        UserResponse user = userService.getUserByAuthUserId(authUserId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('" + SecurityRoles.TEACHER + "')")
    @Operation(summary = "Get user by email", description = "Retrieves a user by their email address (TEACHER only)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(
            @Parameter(description = "User email") @PathVariable String email) {
        UserResponse user = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping
    @PreAuthorize("hasRole('" + SecurityRoles.TEACHER + "')")
    @Operation(
        summary = "Get all users with pagination", 
        description = "Retrieves all users with optional search and role filters and pagination (TEACHER only). " +
                      "Search filters by fullName or email (case-insensitive). " +
                      "Filter by role is OPTIONAL - fetches all users if no role specified. " +
                      "Use page and size parameters for pagination (default: page=0, size=20)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - TEACHER role required")
    })
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @Parameter(description = "Search term to filter by fullName or email (case-insensitive, optional)", example = "va") 
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by role: 'teacher' or 'student' (optional - fetches all if not specified)", example = "student") 
            @RequestParam(required = false) UserRole role,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserResponse> users;
        
        // If search parameter is provided (even if empty), use search method
        // This allows combining search with role filter
        if (search != null) {
            users = userService.searchUsers(search, role, pageable);
        } else if (role != null) {
            // If only role is provided, use role filter
            users = userService.getUsersByRole(role, pageable);
        } else {
            // If neither search nor role, get all users
            users = userService.getAllUsers(pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(users)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete user", description = "Deletes a user by their ID (own profile)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

