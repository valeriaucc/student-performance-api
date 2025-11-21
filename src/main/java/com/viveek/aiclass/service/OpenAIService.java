package com.viveek.aiclass.service;

import com.viveek.aiclass.dto.internal.ClassPerformanceData;
import com.viveek.aiclass.dto.internal.RecommendationPromptData;
import com.viveek.aiclass.dto.internal.StudentPerformanceData;

/**
 * Service interface for OpenAI API operations.
 * Handles communication with OpenAI to generate AI-powered recommendations.
 */
public interface OpenAIService {

    /**
     * Generates a recommendation text using OpenAI based on assessment data (for students).
     * 
     * @param promptData the data extracted from the grade/assessment
     * @return generated recommendation text
     * @throws com.viveek.aiclass.exception.OpenAIServiceException if OpenAI API call fails
     */
    String generateRecommendation(RecommendationPromptData promptData);

    /**
     * Generates a teacher recommendation based on class performance analysis.
     * 
     * @param performanceData aggregated class performance data
     * @return generated recommendation text for the teacher
     * @throws com.viveek.aiclass.exception.OpenAIServiceException if OpenAI API call fails
     */
    String generateTeacherRecommendationForClass(ClassPerformanceData performanceData);

    /**
     * Generates a teacher recommendation based on student performance analysis.
     * 
     * @param performanceData student performance data
     * @return generated recommendation text for the teacher
     * @throws com.viveek.aiclass.exception.OpenAIServiceException if OpenAI API call fails
     */
    String generateTeacherRecommendationForStudent(StudentPerformanceData performanceData);
}

