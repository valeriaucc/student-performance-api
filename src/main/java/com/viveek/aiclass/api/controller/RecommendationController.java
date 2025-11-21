package com.viveek.aiclass.api.controller;

import com.viveek.aiclass.api.ApiVersions;
import com.viveek.aiclass.constants.SecurityRoles;
import com.viveek.aiclass.domain.model.enums.RecommendationAudience;
import com.viveek.aiclass.dto.request.CreateRecommendationRequest;
import com.viveek.aiclass.dto.response.ApiResponse;
import com.viveek.aiclass.dto.response.PageResponse;
import com.viveek.aiclass.dto.response.RecommendationResponse;
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

import java.util.List;
import java.util.UUID;

/**
 * REST controller for AI Recommendation management operations.
 */
@RestController
@RequestMapping(ApiVersions.V1 + "/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "AI Recommendation management APIs")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new recommendation", description = "Creates a new AI-generated recommendation (authenticated users)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Recommendation created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Class or Recipient not found")
    })
    public ResponseEntity<ApiResponse<RecommendationResponse>> createRecommendation(
            @Valid @RequestBody CreateRecommendationRequest request) {
        RecommendationResponse recommendation = recommendationService.createRecommendation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recommendation created successfully", recommendation));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get recommendation by ID", description = "Retrieves a recommendation by its ID (authenticated users)")
    public ResponseEntity<ApiResponse<RecommendationResponse>> getRecommendationById(
            @Parameter(description = "Recommendation ID") @PathVariable UUID id) {
        RecommendationResponse recommendation = recommendationService.getRecommendationById(id);
        return ResponseEntity.ok(ApiResponse.success(recommendation));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get recommendations with pagination", 
        description = "Retrieves recommendations with filters and pagination (authenticated users). " +
                      "⚠️ At least ONE filter parameter is REQUIRED: recipientId, classId, or audience. " +
                      "Use page and size parameters for pagination (default: page=0, size=20)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recommendations retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - At least one filter parameter is required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public ResponseEntity<ApiResponse<PageResponse<RecommendationResponse>>> getRecommendations(
            @Parameter(description = "Filter by recipient user ID (at least one filter required)") 
            @RequestParam(required = false) UUID recipientId,
            @Parameter(description = "Filter by class ID (at least one filter required)") 
            @RequestParam(required = false) UUID classId,
            @Parameter(description = "Filter by audience: INDIVIDUAL, CLASS, or GENERAL (at least one filter required)", example = "INDIVIDUAL") 
            @RequestParam(required = false) RecommendationAudience audience,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<RecommendationResponse> recommendations;
        if (recipientId != null) {
            recommendations = recommendationService.getRecommendationsByRecipientId(recipientId, pageable);
        } else if (classId != null) {
            recommendations = recommendationService.getRecommendationsByClassId(classId, pageable);
        } else if (audience != null) {
            recommendations = recommendationService.getRecommendationsByAudience(audience, pageable);
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Please provide at least one filter parameter"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(recommendations)));
    }

    @PostMapping("/classes/{classId}/generate-teacher-recommendation")
    @PreAuthorize("hasRole('" + SecurityRoles.TEACHER + "')")
    @Operation(
        summary = "Generate AI recommendation for teacher based on class performance",
        description = "Generates an AI-powered recommendation for the teacher based on overall class performance. " +
                      "Analyzes all grades in the class to provide teaching strategies, intervention suggestions, " +
                      "and areas to focus on for the entire class. " +
                      "If a recommendation already exists for this class, it will be returned instead of generating a new one (idempotent). " +
                      "Set forceRegenerate=true to regenerate the recommendation even if one exists (useful after updating grades). " +
                      "Only teachers who own the class can generate recommendations."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Teacher recommendation generated successfully (or existing recommendation returned)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Class not found or no grades available"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "OpenAI service unavailable")
    })
    public ResponseEntity<ApiResponse<RecommendationResponse>> generateTeacherRecommendationForClass(
            @Parameter(description = "Class ID for which to generate teacher recommendation", example = "558fe999-dc20-4437-9b46-f7b32bbc9ea7")
            @PathVariable UUID classId,
            @Parameter(description = "Force regeneration - if true, regenerates recommendation even if one exists", example = "false")
            @RequestParam(required = false, defaultValue = "false") boolean forceRegenerate) {
        RecommendationResponse recommendation = recommendationService.generateTeacherRecommendationForClass(classId, forceRegenerate);
        return ResponseEntity.ok(ApiResponse.success("Teacher recommendation generated successfully", recommendation));
    }

    @PostMapping("/classes/{classId}/students/{studentId}/generate-teacher-recommendation")
    @PreAuthorize("hasRole('" + SecurityRoles.TEACHER + "')")
    @Operation(
        summary = "Generate AI recommendation for teacher based on student performance",
        description = "Generates an AI-powered recommendation for the teacher based on a specific student's performance " +
                      "across all assessments in the class. Analyzes all grades for the student to provide personalized " +
                      "teaching strategies, intervention suggestions, and support recommendations. " +
                      "If a recommendation already exists for this student in this class, it will be returned instead of generating a new one (idempotent). " +
                      "Set forceRegenerate=true to regenerate the recommendation even if one exists (useful after updating grades). " +
                      "Only teachers who teach the class can generate recommendations."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Teacher recommendation generated successfully (or existing recommendation returned)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Class, Student not found, or student not enrolled in class"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "OpenAI service unavailable")
    })
    public ResponseEntity<ApiResponse<RecommendationResponse>> generateTeacherRecommendationForStudent(
            @Parameter(description = "Class ID", example = "558fe999-dc20-4437-9b46-f7b32bbc9ea7")
            @PathVariable UUID classId,
            @Parameter(description = "Student ID for which to generate teacher recommendation", example = "cd347c70-c0cf-4210-b4a9-fd4ceb821b0b")
            @PathVariable UUID studentId,
            @Parameter(description = "Force regeneration - if true, regenerates recommendation even if one exists", example = "false")
            @RequestParam(required = false, defaultValue = "false") boolean forceRegenerate) {
        RecommendationResponse recommendation = recommendationService.generateTeacherRecommendationForStudent(classId, studentId, forceRegenerate);
        return ResponseEntity.ok(ApiResponse.success("Teacher recommendation generated successfully", recommendation));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete recommendation", description = "Deletes a recommendation by its ID (authenticated users)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Recommendation deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recommendation not found")
    })
    public ResponseEntity<Void> deleteRecommendation(
            @Parameter(description = "Recommendation ID") @PathVariable UUID id) {
        recommendationService.deleteRecommendation(id);
        return ResponseEntity.noContent().build();
    }
}

