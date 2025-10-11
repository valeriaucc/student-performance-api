package com.viveek.aiclass.mapper;

import com.viveek.aiclass.domain.model.*;
import com.viveek.aiclass.dto.request.*;
import com.viveek.aiclass.dto.response.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for mapping between entities and DTOs.
 */
public class EntityMapper {

    // User mappings
    public static UserResponse toUserResponse(User user) {
        if (user == null) return null;
        
        return UserResponse.builder()
                .id(user.getId())
                .authUserId(user.getAuthUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .metadata(user.getMetadata())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public static User toUser(CreateUserRequest request) {
        if (request == null) return null;
        
        return User.builder()
                .authUserId(request.getAuthUserId())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(request.getRole())
                .metadata(request.getMetadata())
                .build();
    }

    // Subject mappings
    public static SubjectResponse toSubjectResponse(Subject subject) {
        if (subject == null) return null;
        
        return SubjectResponse.builder()
                .id(subject.getId())
                .code(subject.getCode())
                .name(subject.getName())
                .description(subject.getDescription())
                .createdAt(subject.getCreatedAt())
                .updatedAt(subject.getUpdatedAt())
                .build();
    }

    public static Subject toSubject(CreateSubjectRequest request) {
        if (request == null) return null;
        
        return Subject.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    // Class mappings
    public static ClassResponse toClassResponse(com.viveek.aiclass.domain.model.Class classEntity) {
        if (classEntity == null) return null;
        
        return ClassResponse.builder()
                .id(classEntity.getId())
                .subjectId(classEntity.getSubject() != null ? classEntity.getSubject().getId() : null)
                .subjectName(classEntity.getSubject() != null ? classEntity.getSubject().getName() : null)
                .subjectCode(classEntity.getSubject() != null ? classEntity.getSubject().getCode() : null)
                .teacherId(classEntity.getTeacher() != null ? classEntity.getTeacher().getId() : null)
                .teacherName(classEntity.getTeacher() != null ? classEntity.getTeacher().getFullName() : null)
                .year(classEntity.getYear())
                .semester(classEntity.getSemester())
                .groupCode(classEntity.getGroupCode())
                .metadata(classEntity.getMetadata())
                .createdAt(classEntity.getCreatedAt())
                .updatedAt(classEntity.getUpdatedAt())
                .build();
    }

    // Enrollment mappings
    public static EnrollmentResponse toEnrollmentResponse(Enrollment enrollment) {
        if (enrollment == null) return null;
        
        String className = null;
        if (enrollment.getClassEntity() != null && enrollment.getClassEntity().getSubject() != null) {
            className = enrollment.getClassEntity().getSubject().getName() + " - " + 
                       enrollment.getClassEntity().getGroupCode();
        }
        
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .classId(enrollment.getClassEntity() != null ? enrollment.getClassEntity().getId() : null)
                .className(className)
                .studentId(enrollment.getStudent() != null ? enrollment.getStudent().getId() : null)
                .studentName(enrollment.getStudent() != null ? enrollment.getStudent().getFullName() : null)
                .status(enrollment.getStatus())
                .enrolledAt(enrollment.getEnrolledAt())
                .createdAt(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .build();
    }

    // Grade mappings
    public static GradeResponse toGradeResponse(Grade grade) {
        if (grade == null) return null;
        
        BigDecimal percentage = null;
        if (grade.getScore() != null && grade.getMaxScore() != null && 
            grade.getMaxScore().compareTo(BigDecimal.ZERO) > 0) {
            percentage = grade.getScore()
                    .divide(grade.getMaxScore(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        
        String className = null;
        if (grade.getClassEntity() != null && grade.getClassEntity().getSubject() != null) {
            className = grade.getClassEntity().getSubject().getName() + " - " + 
                       grade.getClassEntity().getGroupCode();
        }
        
        return GradeResponse.builder()
                .id(grade.getId())
                .classId(grade.getClassEntity() != null ? grade.getClassEntity().getId() : null)
                .className(className)
                .studentId(grade.getStudent() != null ? grade.getStudent().getId() : null)
                .studentName(grade.getStudent() != null ? grade.getStudent().getFullName() : null)
                .assessmentKind(grade.getAssessmentKind())
                .assessmentName(grade.getAssessmentName())
                .score(grade.getScore())
                .maxScore(grade.getMaxScore())
                .percentage(percentage)
                .gradedAt(grade.getGradedAt())
                .createdAt(grade.getCreatedAt())
                .updatedAt(grade.getUpdatedAt())
                .build();
    }

    // Recommendation mappings
    public static RecommendationResponse toRecommendationResponse(AiRecommendation recommendation) {
        if (recommendation == null) return null;
        
        String className = null;
        if (recommendation.getClassEntity() != null && recommendation.getClassEntity().getSubject() != null) {
            className = recommendation.getClassEntity().getSubject().getName() + " - " + 
                       recommendation.getClassEntity().getGroupCode();
        }
        
        return RecommendationResponse.builder()
                .id(recommendation.getId())
                .classId(recommendation.getClassEntity() != null ? recommendation.getClassEntity().getId() : null)
                .className(className)
                .recipientId(recommendation.getRecipient() != null ? recommendation.getRecipient().getId() : null)
                .recipientName(recommendation.getRecipient() != null ? recommendation.getRecipient().getFullName() : null)
                .audience(recommendation.getAudience())
                .message(recommendation.getMessage())
                .metadata(recommendation.getMetadata())
                .createdAt(recommendation.getCreatedAt())
                .updatedAt(recommendation.getUpdatedAt())
                .build();
    }
}

