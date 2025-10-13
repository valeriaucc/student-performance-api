package com.viveek.aiclass.api.controller;

import com.viveek.aiclass.dto.request.CreateSubjectRequest;
import com.viveek.aiclass.dto.request.UpdateSubjectRequest;
import com.viveek.aiclass.dto.response.ApiResponse;
import com.viveek.aiclass.dto.response.SubjectResponse;
import com.viveek.aiclass.service.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Subject management operations.
 */
@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Tag(name = "Subjects", description = "Subject management APIs")
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Create a new subject", description = "Creates a new academic subject (TEACHER only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Subject created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Subject code already exists")
    })
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(
            @Valid @RequestBody CreateSubjectRequest request) {
        SubjectResponse subject = subjectService.createSubject(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subject created successfully", subject));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Update subject", description = "Updates an existing subject (TEACHER only)")
    public ResponseEntity<ApiResponse<SubjectResponse>> updateSubject(
            @Parameter(description = "Subject ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateSubjectRequest request) {
        SubjectResponse subject = subjectService.updateSubject(id, request);
        return ResponseEntity.ok(ApiResponse.success("Subject updated successfully", subject));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get subject by ID", description = "Retrieves a subject by its ID (authenticated users)")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubjectById(
            @Parameter(description = "Subject ID") @PathVariable UUID id) {
        SubjectResponse subject = subjectService.getSubjectById(id);
        return ResponseEntity.ok(ApiResponse.success(subject));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get subject by code", description = "Retrieves a subject by its code (authenticated users)")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubjectByCode(
            @Parameter(description = "Subject code") @PathVariable String code) {
        SubjectResponse subject = subjectService.getSubjectByCode(code);
        return ResponseEntity.ok(ApiResponse.success(subject));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all subjects", description = "Retrieves all subjects (authenticated users)")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getAllSubjects() {
        List<SubjectResponse> subjects = subjectService.getAllSubjects();
        return ResponseEntity.ok(ApiResponse.success(subjects));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Delete subject", description = "Deletes a subject by its ID (TEACHER only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Subject deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Subject not found")
    })
    public ResponseEntity<Void> deleteSubject(
            @Parameter(description = "Subject ID") @PathVariable UUID id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }
}

