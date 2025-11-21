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

    /**
     * Extracts all assessment content-related keys from metadata.
     * 
     * @param metadata the metadata map
     * @return a new map containing only assessment content keys
     */
    public static Map<String, Object> extractAssessmentContentKeys(Map<String, Object> metadata) {
        Map<String, Object> contentMap = new java.util.HashMap<>();
        if (metadata == null || metadata.isEmpty()) {
            return contentMap;
        }

        String[] contentKeys = {"assessmentContent", "content", "description", "assessmentDescription"};
        for (String key : contentKeys) {
            Object value = metadata.get(key);
            if (value != null) {
                contentMap.put(key, value);
            }
        }
        
        return contentMap;
    }

    /**
     * Extracts all feedback-related keys from metadata.
     * 
     * @param metadata the metadata map
     * @return a new map containing only feedback keys
     */
    public static Map<String, Object> extractFeedbackKeys(Map<String, Object> metadata) {
        Map<String, Object> feedbackMap = new java.util.HashMap<>();
        if (metadata == null || metadata.isEmpty()) {
            return feedbackMap;
        }

        String[] feedbackKeys = {"feedback", "teacherFeedback", "comments", "notes", "teacherComments"};
        for (String key : feedbackKeys) {
            Object value = metadata.get(key);
            if (value != null) {
                feedbackMap.put(key, value);
            }
        }
        
        return feedbackMap;
    }

    /**
     * Merges metadata by combining assessment content from existing grade with feedback from new grade.
     * Assessment content keys take precedence from existing metadata (first grade).
     * Feedback keys take precedence from new metadata (current grade).
     * Other keys are merged with new metadata taking precedence.
     * 
     * @param existingMetadata metadata from the first grade for this assessment (contains assessment content)
     * @param newMetadata metadata from the new grade being created (contains feedback)
     * @return merged metadata map
     */
    public static Map<String, Object> mergeMetadataForAssessment(
            Map<String, Object> existingMetadata, 
            Map<String, Object> newMetadata) {
        Map<String, Object> merged = new java.util.HashMap<>();
        
        // Start with existing metadata (assessment content)
        if (existingMetadata != null) {
            merged.putAll(existingMetadata);
        }
        
        // Extract assessment content keys from existing (these should not be overwritten)
        Map<String, Object> existingContent = extractAssessmentContentKeys(existingMetadata != null ? existingMetadata : new java.util.HashMap<>());
        
        // Extract feedback keys from new metadata (these should take precedence)
        Map<String, Object> newFeedback = extractFeedbackKeys(newMetadata != null ? newMetadata : new java.util.HashMap<>());
        
        // Add all new metadata
        if (newMetadata != null) {
            merged.putAll(newMetadata);
        }
        
        // Restore assessment content from existing (to ensure it's not overwritten)
        merged.putAll(existingContent);
        
        // Ensure feedback from new metadata is present (takes precedence)
        merged.putAll(newFeedback);
        
        return merged;
    }
}

