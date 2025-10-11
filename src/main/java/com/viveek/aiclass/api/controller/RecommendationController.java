package com.viveek.aiclass.api.controller;

import com.viveek.aiclass.domain.model.enums.RecommendationAudience;
import com.viveek.aiclass.dto.request.CreateRecommendationRequest;
import com.viveek.aiclass.dto.response.ApiResponse;
import com.viveek.aiclass.dto.response.RecommendationResponse;
import com.viveek.aiclass.service.RecommendationService;
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
 * REST controller for AI Recommendation management operations.
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "AI Recommendation management APIs")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping
    @Operation(summary = "Create a new recommendation", description = "Creates a new AI-generated recommendation")
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
    @Operation(summary = "Get recommendation by ID", description = "Retrieves a recommendation by its ID")
    public ResponseEntity<ApiResponse<RecommendationResponse>> getRecommendationById(
            @Parameter(description = "Recommendation ID") @PathVariable UUID id) {
        RecommendationResponse recommendation = recommendationService.getRecommendationById(id);
        return ResponseEntity.ok(ApiResponse.success(recommendation));
    }

    @GetMapping
    @Operation(summary = "Get recommendations", description = "Retrieves recommendations with optional filters")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> getRecommendations(
            @Parameter(description = "Filter by recipient ID") @RequestParam(required = false) UUID recipientId,
            @Parameter(description = "Filter by class ID") @RequestParam(required = false) UUID classId,
            @Parameter(description = "Filter by audience") @RequestParam(required = false) RecommendationAudience audience) {
        
        List<RecommendationResponse> recommendations;
        if (recipientId != null) {
            recommendations = recommendationService.getRecommendationsByRecipientId(recipientId);
        } else if (classId != null) {
            recommendations = recommendationService.getRecommendationsByClassId(classId);
        } else if (audience != null) {
            recommendations = recommendationService.getRecommendationsByAudience(audience);
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Please provide at least one filter parameter"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete recommendation", description = "Deletes a recommendation by its ID")
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

