package com.viveek.aiclass.util;

import java.util.Map;

/**
 * Utility class for parsing JSONB metadata from Grade entities.
 * 
 * Extracts structured data from the metadata map for use in AI recommendation generation.
 * Handles missing keys gracefully by returning empty strings or null values.
 */
public final class MetadataParser {

    private MetadataParser() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Extracts assessment content/description from metadata.
     * 
     * Looks for keys: "assessmentContent", "content", "description", "assessmentDescription"
     * Returns the first non-null value found, or empty string if none found.
     * 
     * @param metadata the metadata map from Grade entity
     * @return assessment content as string, or empty string if not found
     */
    public static String extractAssessmentContent(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "";
        }

        // Try different possible keys
        String[] possibleKeys = {"assessmentContent", "content", "description", "assessmentDescription"};
        
        for (String key : possibleKeys) {
            Object value = metadata.get(key);
            if (value != null) {
                String content = value.toString().trim();
                if (!content.isEmpty()) {
                    return content;
                }
            }
        }
        
        return "";
    }

    /**
     * Extracts teacher feedback from metadata.
     * 
     * Looks for keys: "feedback", "teacherFeedback", "comments", "notes"
     * Returns the first non-null value found, or empty string if none found.
     * 
     * @param metadata the metadata map from Grade entity
     * @return teacher feedback as string, or empty string if not found
     */
    public static String extractFeedback(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "";
        }

        // Try different possible keys
        String[] possibleKeys = {"feedback", "teacherFeedback", "comments", "notes", "teacherComments"};
        
        for (String key : possibleKeys) {
            Object value = metadata.get(key);
            if (value != null) {
                String feedback = value.toString().trim();
                if (!feedback.isEmpty()) {
                    return feedback;
                }
            }
        }
        
        return "";
    }

    /**
     * Formats metadata into a clean, readable string for AI prompts.
     * 
     * Extracts assessment content and feedback, formats them nicely.
     * Returns a formatted string suitable for inclusion in prompts.
     * 
     * @param metadata the metadata map from Grade entity
     * @return formatted string with assessment content and feedback, or empty string if none found
     */
    public static String formatMetadataForPrompt(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        
        String content = extractAssessmentContent(metadata);
        if (!content.isEmpty()) {
            sb.append("Assessment Content: ").append(content).append("\n");
        }
        
        String feedback = extractFeedback(metadata);
        if (!feedback.isEmpty()) {
            sb.append("Teacher Feedback: ").append(feedback).append("\n");
        }
        
        return sb.toString().trim();
    }
}

