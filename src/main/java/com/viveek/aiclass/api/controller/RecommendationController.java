package com.viveek.aiclass.api.controller;

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
@RequestMapping("/api/recommendations")
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
    @Operation(summary = "Get recommendations with pagination", description = "Retrieves recommendations with optional filters and pagination (authenticated users)")
    public ResponseEntity<ApiResponse<PageResponse<RecommendationResponse>>> getRecommendations(
            @Parameter(description = "Filter by recipient ID") @RequestParam(required = false) UUID recipientId,
            @Parameter(description = "Filter by class ID") @RequestParam(required = false) UUID classId,
            @Parameter(description = "Filter by audience") @RequestParam(required = false) RecommendationAudience audience,
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

