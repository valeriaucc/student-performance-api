package com.viveek.aiclass.api.controller;

import com.viveek.aiclass.api.ApiVersions;
import com.viveek.aiclass.constants.SecurityRoles;
import com.viveek.aiclass.domain.model.enums.EnrollmentStatus;
import com.viveek.aiclass.dto.request.CreateEnrollmentRequest;
import com.viveek.aiclass.dto.request.UpdateEnrollmentRequest;
import com.viveek.aiclass.dto.response.ApiResponse;
import com.viveek.aiclass.dto.response.EnrollmentResponse;
import com.viveek.aiclass.dto.response.PageResponse;
import com.viveek.aiclass.service.EnrollmentService;
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

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Enrollment management operations.
 */
@RestController
@RequestMapping(ApiVersions.V1 + "/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollments", description = "Enrollment management APIs")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("hasRole('" + SecurityRoles.TEACHER + "')")
    @Operation(summary = "Enroll a student in a class", description = "Creates a new enrollment (TEACHER only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Student enrolled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Class or Student not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Student already enrolled")
    })
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollStudent(
            @Valid @RequestBody CreateEnrollmentRequest request) {
        EnrollmentResponse enrollment = enrollmentService.enrollStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Student enrolled successfully", enrollment));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('" + SecurityRoles.TEACHER + "')")
    @Operation(summary = "Update enrollment status", description = "Updates the status of an enrollment (TEACHER only)")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> updateEnrollment(
            @Parameter(description = "Enrollment ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateEnrollmentRequest request) {
        EnrollmentResponse enrollment = enrollmentService.updateEnrollment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Enrollment updated successfully", enrollment));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get enrollment by ID", description = "Retrieves an enrollment by its ID (authenticated users)")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getEnrollmentById(
            @Parameter(description = "Enrollment ID") @PathVariable UUID id) {
        EnrollmentResponse enrollment = enrollmentService.getEnrollmentById(id);
        return ResponseEntity.ok(ApiResponse.success(enrollment));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get enrollments with pagination", 
        description = "Retrieves enrollments with filters and pagination (authenticated users). " +
                      "⚠️ At least ONE filter parameter is REQUIRED: classId, studentId, or status. " +
                      "Use page and size parameters for pagination (default: page=0, size=20)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Enrollments retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - At least one filter parameter is required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public ResponseEntity<ApiResponse<PageResponse<EnrollmentResponse>>> getEnrollments(
            @Parameter(description = "Filter by class ID (at least one filter required)", example = "4689b828-0cdb-4333-a9c8-48dce64e2409") 
            @RequestParam(required = false) UUID classId,
            @Parameter(description = "Filter by student ID (at least one filter required)", example = "1bb9ce94-66b2-4431-8b2b-724d569e2f24") 
            @RequestParam(required = false) UUID studentId,
            @Parameter(description = "Filter by status: ACTIVE, DROPPED, or COMPLETED (at least one filter required)", example = "ACTIVE") 
            @RequestParam(required = false) EnrollmentStatus status,
            @PageableDefault(size = 20, sort = "enrolledAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<EnrollmentResponse> enrollments;
        if (classId != null) {
            enrollments = enrollmentService.getEnrollmentsByClassId(classId, pageable);
        } else if (studentId != null) {
            enrollments = enrollmentService.getEnrollmentsByStudentId(studentId, pageable);
        } else if (status != null) {
            enrollments = enrollmentService.getEnrollmentsByStatus(status, pageable);
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Please provide at least one filter parameter"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(enrollments)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('" + SecurityRoles.TEACHER + "')")
    @Operation(summary = "Delete enrollment", description = "Deletes an enrollment by its ID (TEACHER only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Enrollment deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Enrollment not found")
    })
    public ResponseEntity<Void> deleteEnrollment(
            @Parameter(description = "Enrollment ID") @PathVariable UUID id) {
        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.noContent().build();
    }
}

