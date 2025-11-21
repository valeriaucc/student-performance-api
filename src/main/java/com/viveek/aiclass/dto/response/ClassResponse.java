package com.viveek.aiclass.dto.response;

import com.viveek.aiclass.domain.model.enums.Semester;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for Class entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Class section details response")
public class ClassResponse {

    @Schema(description = "Class unique identifier", example = "558fe999-dc20-4437-9b46-f7b32bbc9ea7")
    private UUID id;
    
    @Schema(description = "Subject UUID", example = "6711adec-edc0-45e6-8d55-b9f28e74cebd")
    private UUID subjectId;
    
    @Schema(description = "Subject name", example = "Introduction to Computer Science")
    private String subjectName;
    
    @Schema(description = "Subject code", example = "CS101")
    private String subjectCode;
    
    @Schema(description = "Teacher UUID", example = "b2ff3b86-5c6a-4c19-826e-61696649c4e8")
    private UUID teacherId;
    
    @Schema(description = "Teacher name", example = "Dr. Jane Smith")
    private String teacherName;
    
    @Schema(description = "Academic year", example = "2025")
    private Integer year;
    
    @Schema(description = "Academic semester", example = "SPRING")
    private Semester semester;
    
    @Schema(description = "Group code/section", example = "A")
    private String groupCode;
    
    @Schema(description = "Additional metadata", example = "{\"room\": \"Building C, Room 201\", \"capacity\": 30}")
    private Map<String, Object> metadata;
    
    @Schema(description = "AI-generated recommendation for the teacher based on class performance (if available)", example = "{\"id\": \"9c5e8d7f-4a3b-2c1d-0e9f-8a7b6c5d4e3f\", \"message\": \"Based on class performance, consider focusing on...\"}")
    private RecommendationSummary teacherRecommendation;
    
    @Schema(description = "List of students enrolled in this class (with ACTIVE status)", example = "[{\"id\": \"cd347c70-c0cf-4210-b4a9-fd4ceb821b0b\", \"name\": \"John Doe\", \"email\": \"john.doe@example.com\"}]")
    private List<StudentInfo> students;
    
    @Schema(description = "Creation timestamp", example = "2025-10-11T10:00:00-05:00")
    private ZonedDateTime createdAt;
    
    @Schema(description = "Last update timestamp", example = "2025-10-11T14:30:00-05:00")
    private ZonedDateTime updatedAt;
    
    /**
     * Inner class for student information in class response.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Student information")
    public static class StudentInfo {
        @Schema(description = "Student UUID", example = "cd347c70-c0cf-4210-b4a9-fd4ceb821b0b")
        private UUID id;
        
        @Schema(description = "Student name", example = "John Doe")
        private String name;
        
        @Schema(description = "Student email", example = "john.doe@example.com")
        private String email;
    }
}

