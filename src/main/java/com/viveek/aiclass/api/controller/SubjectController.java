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
    @Operation(summary = "Create a new subject", description = "Creates a new academic subject")
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
    @Operation(summary = "Update subject", description = "Updates an existing subject")
    public ResponseEntity<ApiResponse<SubjectResponse>> updateSubject(
            @Parameter(description = "Subject ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateSubjectRequest request) {
        SubjectResponse subject = subjectService.updateSubject(id, request);
        return ResponseEntity.ok(ApiResponse.success("Subject updated successfully", subject));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get subject by ID", description = "Retrieves a subject by its ID")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubjectById(
            @Parameter(description = "Subject ID") @PathVariable UUID id) {
        SubjectResponse subject = subjectService.getSubjectById(id);
        return ResponseEntity.ok(ApiResponse.success(subject));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get subject by code", description = "Retrieves a subject by its code")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubjectByCode(
            @Parameter(description = "Subject code") @PathVariable String code) {
        SubjectResponse subject = subjectService.getSubjectByCode(code);
        return ResponseEntity.ok(ApiResponse.success(subject));
    }

    @GetMapping
    @Operation(summary = "Get all subjects", description = "Retrieves all subjects")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getAllSubjects() {
        List<SubjectResponse> subjects = subjectService.getAllSubjects();
        return ResponseEntity.ok(ApiResponse.success(subjects));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete subject", description = "Deletes a subject by its ID")
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

