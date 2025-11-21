package com.viveek.aiclass.service;

import com.viveek.aiclass.domain.model.enums.RecommendationAudience;
import com.viveek.aiclass.dto.request.CreateRecommendationRequest;
import com.viveek.aiclass.dto.response.RecommendationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for AI Recommendation operations.
 * All list operations use pagination for better performance and consistency.
 */
public interface RecommendationService {

    RecommendationResponse createRecommendation(CreateRecommendationRequest request);

    RecommendationResponse getRecommendationById(UUID id);

    /**
     * Get recommendations by recipient ID with pagination and authorization.
     * Students can only view their own recommendations, teachers can view recommendations for students in their classes.
     */
    Page<RecommendationResponse> getRecommendationsByRecipientId(UUID recipientId, Pageable pageable);

    /**
     * Get recommendations by class ID with pagination and authorization.
     * Teachers must own the class, students can only see their own recommendations within the class.
     */
    Page<RecommendationResponse> getRecommendationsByClassId(UUID classId, Pageable pageable);

    /**
     * Get recommendations by audience with pagination.
     */
    Page<RecommendationResponse> getRecommendationsByAudience(RecommendationAudience audience, Pageable pageable);

    /**
     * Generate an AI-powered recommendation for a specific grade/assessment (for students).
     * 
     * This method:
     * - Checks for existing recommendation (idempotency)
     * - Extracts data from the grade (subject, assessment content, feedback, score)
     * - Calls OpenAI to generate a personalized recommendation
     * - Saves and returns the recommendation linked to the grade
     * 
     * Authorization: Teachers can generate for their classes, students can generate for their own grades.
     * 
     * @param gradeId the UUID of the grade/assessment
     * @return the generated recommendation response
     */
    RecommendationResponse generateRecommendationForGrade(UUID gradeId);

    /**
     * Generate an AI-powered recommendation for teachers based on class performance.
     * 
     * Analyzes all grades in a class to provide teaching strategies, intervention suggestions,
     * and areas to focus on for the entire class.
     * 
     * Authorization: Only teachers who own the class can generate recommendations.
     * 
     * @param classId the UUID of the class
     * @param forceRegenerate if true, regenerates even if a recommendation exists
     * @return the generated recommendation response for the teacher
     */
    RecommendationResponse generateTeacherRecommendationForClass(UUID classId, boolean forceRegenerate);

    /**
     * Generate an AI-powered recommendation for teachers based on a specific student's performance.
     * 
     * Analyzes all grades for a student to provide personalized teaching strategies,
     * intervention suggestions, and support recommendations.
     * 
     * Authorization: Only teachers who teach the student can generate recommendations.
     * 
     * @param classId the UUID of the class
     * @param studentId the UUID of the student
     * @param forceRegenerate if true, regenerates even if a recommendation exists
     * @return the generated recommendation response for the teacher
     */
    RecommendationResponse generateTeacherRecommendationForStudent(UUID classId, UUID studentId, boolean forceRegenerate);

    void deleteRecommendation(UUID id);
}

