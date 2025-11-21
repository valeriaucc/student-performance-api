package com.viveek.aiclass.api.controller;

import com.viveek.aiclass.api.ApiVersions;
import com.viveek.aiclass.constants.SecurityRoles;
import com.viveek.aiclass.dto.request.CreateGradeRequest;
import com.viveek.aiclass.dto.request.UpdateGradeRequest;
import com.viveek.aiclass.dto.response.ApiResponse;
import com.viveek.aiclass.dto.response.GradeResponse;
import com.viveek.aiclass.dto.response.PageResponse;
import com.viveek.aiclass.dto.response.RecommendationResponse;
import com.viveek.aiclass.service.GradeService;
import com.viveek.aiclass.service.RecommendationService;
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
 * REST controller for Grade management operations.
 */
@RestController
@RequestMapping(ApiVersions.V1 + "/grades")
@RequiredArgsConstructor
@Tag(name = "Grades", description = "Grade management APIs")
public class GradeController {

    private final GradeService gradeService;
    private final RecommendationService recommendationService;

    @PostMapping
    @PreAuthorize("hasRole('" + SecurityRoles.TEACHER + "')")
    @Operation(summary = "Create a new grade", description = "Creates a new grade for a student (TEACHER only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Grade created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Class or Student not found")
    })
    public ResponseEntity<ApiResponse<GradeResponse>> createGrade(
            @Valid @RequestBody CreateGradeRequest request) {
        GradeResponse grade = gradeService.createGrade(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Grade created successfully", grade));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('" + SecurityRoles.TEACHER + "')")
    @Operation(summary = "Update grade", description = "Updates an existing grade (TEACHER only)")
    public ResponseEntity<ApiResponse<GradeResponse>> updateGrade(
            @Parameter(description = "Grade ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateGradeRequest request) {
        GradeResponse grade = gradeService.updateGrade(id, request);
        return ResponseEntity.ok(ApiResponse.success("Grade updated successfully", grade));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get grade by ID", description = "Retrieves a grade by its ID (authenticated users)")
    public ResponseEntity<ApiResponse<GradeResponse>> getGradeById(
            @Parameter(description = "Grade ID") @PathVariable UUID id) {
        GradeResponse grade = gradeService.getGradeById(id);
        return ResponseEntity.ok(ApiResponse.success(grade));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get grades with pagination", 
        description = "Retrieves grades with filters and pagination (authenticated users). " +
                      "⚠️ Exactly ONE filter parameter is REQUIRED: either classId OR studentId (not both). " +
                      "Use page and size parameters for pagination (default: page=0, size=20)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Grades retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - Provide exactly one filter (classId or studentId, not both)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public ResponseEntity<ApiResponse<PageResponse<GradeResponse>>> getGrades(
            @Parameter(description = "Filter by class ID - retrieves all grades for a class (use classId OR studentId, not both)", example = "4689b828-0cdb-4333-a9c8-48dce64e2409") 
            @RequestParam(required = false) UUID classId,
            @Parameter(description = "Filter by student ID - retrieves all grades for a student (use classId OR studentId, not both)", example = "1bb9ce94-66b2-4431-8b2b-724d569e2f24") 
            @RequestParam(required = false) UUID studentId,
            @PageableDefault(size = 20, sort = "gradedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<GradeResponse> grades;
        if (classId != null && studentId != null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Please provide only one filter parameter (classId or studentId, not both)"));
        } else if (classId != null) {
            grades = gradeService.getGradesByClassId(classId, pageable);
        } else if (studentId != null) {
            grades = gradeService.getGradesByStudentId(studentId, pageable);
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Please provide at least one filter parameter (classId or studentId)"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(grades)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('" + SecurityRoles.TEACHER + "')")
    @Operation(summary = "Delete grade", description = "Deletes a grade by its ID (TEACHER only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Grade deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Grade not found")
    })
    public ResponseEntity<Void> deleteGrade(
            @Parameter(description = "Grade ID") @PathVariable UUID id) {
        gradeService.deleteGrade(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{gradeId}/generate-recommendation")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Generate AI recommendation for grade",
        description = "Generates an AI-powered recommendation for a specific assessment/grade. " +
                      "The recommendation is based on the assessment content, subject, teacher feedback, and score. " +
                      "If a recommendation already exists for this grade, it will be returned instead of generating a new one (idempotent). " +
                      "Teachers can generate recommendations for grades in their classes. Students can generate recommendations for their own grades."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recommendation generated successfully (or existing recommendation returned)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Grade not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "OpenAI service unavailable")
    })
    public ResponseEntity<ApiResponse<RecommendationResponse>> generateRecommendation(
            @Parameter(description = "Grade ID for which to generate recommendation", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
            @PathVariable UUID gradeId) {
        RecommendationResponse recommendation = recommendationService.generateRecommendationForGrade(gradeId);
        return ResponseEntity.ok(ApiResponse.success("Recommendation generated successfully", recommendation));
    }
}

