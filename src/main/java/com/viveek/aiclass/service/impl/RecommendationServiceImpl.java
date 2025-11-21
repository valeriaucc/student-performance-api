package com.viveek.aiclass.service.impl;

import com.viveek.aiclass.domain.model.AiRecommendation;
import com.viveek.aiclass.domain.model.Class;
import com.viveek.aiclass.domain.model.Grade;
import com.viveek.aiclass.domain.model.User;
import com.viveek.aiclass.domain.model.enums.RecommendationAudience;
import com.viveek.aiclass.domain.repository.AiRecommendationRepository;
import com.viveek.aiclass.domain.repository.ClassRepository;
import com.viveek.aiclass.domain.repository.GradeRepository;
import com.viveek.aiclass.domain.repository.UserRepository;
import com.viveek.aiclass.dto.internal.ClassPerformanceData;
import com.viveek.aiclass.dto.internal.RecommendationPromptData;
import com.viveek.aiclass.dto.internal.StudentPerformanceData;
import com.viveek.aiclass.dto.request.CreateRecommendationRequest;
import com.viveek.aiclass.dto.response.RecommendationResponse;
import com.viveek.aiclass.domain.repository.EnrollmentRepository;
import com.viveek.aiclass.exception.ResourceNotFoundException;
import com.viveek.aiclass.mapper.RecommendationMapper;
import com.viveek.aiclass.security.AuthenticatedUser;
import com.viveek.aiclass.security.SecurityContextHelper;
import com.viveek.aiclass.service.OpenAIService;
import com.viveek.aiclass.service.RecommendationService;
import com.viveek.aiclass.util.MetadataParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of RecommendationService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationServiceImpl implements RecommendationService {

    private final AiRecommendationRepository recommendationRepository;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final RecommendationMapper recommendationMapper;
    private final OpenAIService openAIService;

    @Override
    public RecommendationResponse createRecommendation(CreateRecommendationRequest request) {
        log.info("Creating recommendation for classId={}, recipientId={}, audience={}", 
                 request.getClassId(), request.getRecipientId(), request.getAudience());
        
        Class classEntity = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", request.getClassId()));

        // ✅ AUTHORIZATION: Verify the current user is the teacher of this class
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        if (!classEntity.getTeacher().getId().equals(currentUser.getUserId())) {
            log.warn("Recommendation creation denied: user {} is not the teacher of class {}", 
                     currentUser.getUserId(), request.getClassId());
            throw new AccessDeniedException("You can only create recommendations for your classes");
        }

        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getRecipientId()));

        AiRecommendation recommendation = AiRecommendation.builder()
                .classEntity(classEntity)
                .recipient(recipient)
                .audience(request.getAudience())
                .message(request.getMessage())
                .metadata(request.getMetadata())
                .build();

        AiRecommendation savedRecommendation = recommendationRepository.save(recommendation);
        log.debug("Recommendation created successfully: id={}", savedRecommendation.getId());
        return recommendationMapper.toResponse(savedRecommendation);
    }

    @Override
    @Transactional(readOnly = true)
    public RecommendationResponse getRecommendationById(UUID id) {
        AiRecommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation", "id", id));
        
        // ✅ AUTHORIZATION: Verify access based on role
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        
        if (currentUser.isTeacher()) {
            // Teachers can only view recommendations for their own classes
            if (!recommendation.getClassEntity().getTeacher().getId().equals(currentUser.getUserId())) {
                log.warn("Recommendation access denied: teacher {} tried to access recommendation {} for class owned by teacher {}", 
                         currentUser.getUserId(), id, recommendation.getClassEntity().getTeacher().getId());
                throw new AccessDeniedException("You can only view recommendations for your classes");
            }
        } else if (currentUser.isStudent()) {
            // Students can only view their own recommendations
            if (!recommendation.getRecipient().getId().equals(currentUser.getUserId())) {
                log.warn("Recommendation access denied: student {} tried to access recommendation {} for recipient {}", 
                         currentUser.getUserId(), id, recommendation.getRecipient().getId());
                throw new AccessDeniedException("You can only view your own recommendations");
            }
        }
        
        return recommendationMapper.toResponse(recommendation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecommendationResponse> getRecommendationsByRecipientId(UUID recipientId, Pageable pageable) {
        log.debug("Fetching recommendations by recipient with pagination: recipientId={}, page={}, size={}", 
                  recipientId, pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            // ✅ AUTHORIZATION: Verify access rights
            AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
            
            if (currentUser.isStudent()) {
                // Students can only view their own recommendations
                if (!recipientId.equals(currentUser.getUserId())) {
                    log.warn("Recommendations access denied: student {} tried to access recommendations for recipient {}", 
                             currentUser.getUserId(), recipientId);
                    throw new AccessDeniedException("You can only view your own recommendations");
                }
                return recommendationRepository.findByRecipientId(recipientId, pageable)
                        .map(recommendationMapper::toResponse);
            } else if (currentUser.isTeacher()) {
                Page<AiRecommendation> recommendations = recommendationRepository.findByRecipientIdAndTeacherId(
                        recipientId,
                        currentUser.getUserId(),
                        pageable
                );
                log.debug("Found {} recommendations for recipient {} by teacher {}", 
                         recommendations.getTotalElements(), recipientId, currentUser.getUserId());
                return recommendations.map(recommendationMapper::toResponse);
            }
            
            // Fallback for other user types
            return recommendationRepository.findByRecipientId(recipientId, pageable)
                    .map(recommendationMapper::toResponse);
        } catch (Exception e) {
            log.error("Error fetching recommendations by recipientId {}: {}", recipientId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecommendationResponse> getRecommendationsByClassId(UUID classId, Pageable pageable) {
        log.debug("Fetching recommendations by class with pagination: classId={}, page={}, size={}", 
                  classId, pageable.getPageNumber(), pageable.getPageSize());
        
        // ✅ AUTHORIZATION: Verify the teacher owns this class or student is enrolled
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", classId));
        
        if (currentUser.isTeacher()) {
            if (!classEntity.getTeacher().getId().equals(currentUser.getUserId())) {
                log.warn("Recommendations access denied: teacher {} tried to access recommendations for class owned by teacher {}", 
                         currentUser.getUserId(), classEntity.getTeacher().getId());
                throw new AccessDeniedException("You can only view recommendations for your classes");
            }
        } else if (currentUser.isStudent()) {
            // Students can only view recommendations for classes they're enrolled in
            if (!enrollmentRepository.isStudentEnrolledInClass(currentUser.getUserId(), classId)) {
                log.warn("Recommendations access denied: student {} tried to access recommendations for class without enrollment", 
                         currentUser.getUserId());
                throw new AccessDeniedException("You can only view recommendations for classes you are enrolled in");
            }
            return recommendationRepository.findByClassEntityIdAndRecipientId(
                    classId,
                    currentUser.getUserId(),
                    pageable
            ).map(recommendationMapper::toResponse);
        }
        
        return recommendationRepository.findByClassEntityId(classId, pageable)
                .map(recommendationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecommendationResponse> getRecommendationsByAudience(RecommendationAudience audience, Pageable pageable) {
        log.debug("Fetching recommendations by audience with pagination: audience={}, page={}, size={}", 
                  audience, pageable.getPageNumber(), pageable.getPageSize());
        return recommendationRepository.findByAudience(audience, pageable)
                .map(recommendationMapper::toResponse);
    }

    @Override
    public RecommendationResponse generateRecommendationForGrade(UUID gradeId) {
        log.info("Generating recommendation for grade: id={}", gradeId);
        
        // ✅ STEP 1: Fetch data in read-only transaction
        Grade grade = fetchGradeWithRelations(gradeId);
        
        // ✅ STEP 2: Authorization check
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        verifyAuthorization(grade, currentUser);
        
        // ✅ STEP 3: Idempotency check - if recommendation exists, return it
        return recommendationRepository.findByGradeId(gradeId)
                .map(existing -> {
                    log.info("Recommendation already exists for grade {}, returning existing recommendation", gradeId);
                    return recommendationMapper.toResponse(existing);
                })
                .orElseGet(() -> {
                    // ✅ STEP 4: Generate recommendation (outside transaction)
                    String recommendationText = generateRecommendationText(grade);
                    
                    // ✅ STEP 5: Save in new transaction
                    return saveRecommendation(grade, recommendationText);
                });
    }

    /**
     * Fetches grade with all necessary relationships in a read-only transaction.
     */
    @Transactional(readOnly = true)
    private Grade fetchGradeWithRelations(UUID gradeId) {
        return gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade", "id", gradeId));
    }

    /**
     * Verifies that the current user is authorized to generate recommendations for this grade.
     */
    private void verifyAuthorization(Grade grade, AuthenticatedUser currentUser) {
        if (currentUser.isTeacher()) {
            // Teachers can generate recommendations for their classes
            if (!grade.getClassEntity().getTeacher().getId().equals(currentUser.getUserId())) {
                log.warn("Recommendation generation denied: teacher {} tried to generate for grade {} in class owned by teacher {}", 
                         currentUser.getUserId(), grade.getId(), grade.getClassEntity().getTeacher().getId());
                throw new AccessDeniedException("You can only generate recommendations for grades in your classes");
            }
        } else if (currentUser.isStudent()) {
            // Students can generate recommendations for their own grades
            if (!grade.getStudent().getId().equals(currentUser.getUserId())) {
                log.warn("Recommendation generation denied: student {} tried to generate for grade {} belonging to student {}", 
                         currentUser.getUserId(), grade.getId(), grade.getStudent().getId());
                throw new AccessDeniedException("You can only generate recommendations for your own grades");
            }
        }
    }

    /**
     * Generates recommendation text using OpenAI (outside transaction).
     */
    private String generateRecommendationText(Grade grade) {
        log.debug("Calling OpenAI to generate recommendation for grade {}", grade.getId());
        
        // Extract data from grade
        RecommendationPromptData promptData = RecommendationPromptData.builder()
                .subjectName(grade.getClassEntity().getSubject().getName())
                .assessmentContent(MetadataParser.extractAssessmentContent(grade.getMetadata()))
                .feedback(MetadataParser.extractFeedback(grade.getMetadata()))
                .score(grade.getScore())
                .maxScore(grade.getMaxScore())
                .assessmentName(grade.getAssessmentName())
                .assessmentKind(grade.getAssessmentKind())
                .build();
        
        return openAIService.generateRecommendation(promptData);
    }

    /**
     * Saves the recommendation in a new transaction.
     */
    @Transactional
    private RecommendationResponse saveRecommendation(Grade grade, String recommendationText) {
        log.debug("Saving recommendation for grade {}", grade.getId());
        
        AiRecommendation recommendation = AiRecommendation.builder()
                .classEntity(grade.getClassEntity())
                .recipient(grade.getStudent())
                .grade(grade)
                .audience(RecommendationAudience.STUDENT)
                .message(recommendationText)
                .build();
        
        AiRecommendation savedRecommendation = recommendationRepository.save(recommendation);
        log.info("Recommendation generated and saved successfully: id={}, gradeId={}", 
                 savedRecommendation.getId(), grade.getId());
        
        return recommendationMapper.toResponse(savedRecommendation);
    }

    @Override
    public RecommendationResponse generateTeacherRecommendationForClass(UUID classId, boolean forceRegenerate) {
        log.info("Generating teacher recommendation for class: id={}, forceRegenerate={}", classId, forceRegenerate);
        
        // ✅ STEP 1: Fetch class and verify authorization
        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", classId));
        
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        if (!currentUser.isTeacher() || !classEntity.getTeacher().getId().equals(currentUser.getUserId())) {
            log.warn("Teacher recommendation generation denied: user {} tried to generate for class owned by teacher {}", 
                     currentUser.getUserId(), classEntity.getTeacher().getId());
            throw new AccessDeniedException("You can only generate recommendations for your classes");
        }
        
        // ✅ STEP 2: Check idempotency (unless force regenerate)
        if (!forceRegenerate) {
            return recommendationRepository.findByClassAndAudience(classId, RecommendationAudience.TEACHER)
                    .stream()
                    .filter(rec -> rec.getRecipient().getId().equals(currentUser.getUserId()))
                    .findFirst()
                    .map(existing -> {
                        log.info("Teacher recommendation already exists for class {}, returning existing", classId);
                        return recommendationMapper.toResponse(existing);
                    })
                    .orElseGet(() -> generateAndSaveClassRecommendation(classEntity, currentUser));
        } else {
            // Force regeneration: delete existing recommendation first
            log.info("Force regeneration requested, deleting existing recommendation if any");
            recommendationRepository.findByClassAndAudience(classId, RecommendationAudience.TEACHER)
                    .stream()
                    .filter(rec -> rec.getRecipient().getId().equals(currentUser.getUserId()))
                    .findFirst()
                    .ifPresent(existing -> {
                        log.info("Deleting existing recommendation {} for regeneration", existing.getId());
                        recommendationRepository.delete(existing);
                    });
            
            return generateAndSaveClassRecommendation(classEntity, currentUser);
        }
    }

    /**
     * Generates and saves a new class recommendation.
     */
    private RecommendationResponse generateAndSaveClassRecommendation(Class classEntity, AuthenticatedUser currentUser) {
        // ✅ STEP 3: Aggregate class performance data
        ClassPerformanceData performanceData = aggregateClassPerformance(classEntity);
        
        // ✅ STEP 4: Generate recommendation (outside transaction)
        String recommendationText = openAIService.generateTeacherRecommendationForClass(performanceData);
        
        // ✅ STEP 5: Save in new transaction
        return saveTeacherRecommendation(classEntity, classEntity.getTeacher(), recommendationText, null);
    }

    @Override
    public RecommendationResponse generateTeacherRecommendationForStudent(UUID classId, UUID studentId, boolean forceRegenerate) {
        log.info("Generating teacher recommendation for student: classId={}, studentId={}, forceRegenerate={}", 
                 classId, studentId, forceRegenerate);
        
        // ✅ STEP 1: Fetch class and student, verify authorization
        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", classId));
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));
        
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        if (!currentUser.isTeacher() || !classEntity.getTeacher().getId().equals(currentUser.getUserId())) {
            log.warn("Teacher recommendation generation denied: user {} tried to generate for class owned by teacher {}", 
                     currentUser.getUserId(), classEntity.getTeacher().getId());
            throw new AccessDeniedException("You can only generate recommendations for students in your classes");
        }
        
        // Verify student is enrolled in the class
        if (!enrollmentRepository.isStudentEnrolledInClass(studentId, classId)) {
            throw new ResourceNotFoundException("Student is not enrolled in this class");
        }
        
        // ✅ STEP 2: Check idempotency (unless force regenerate)
        if (!forceRegenerate) {
            return recommendationRepository.findByRecipientAndClass(currentUser.getUserId(), classId)
                    .stream()
                    .filter(rec -> rec.getAudience() == RecommendationAudience.TEACHER)
                    .findFirst()
                    .map(existing -> {
                        log.info("Teacher recommendation already exists for student {} in class {}, returning existing", 
                                 studentId, classId);
                        return recommendationMapper.toResponse(existing);
                    })
                    .orElseGet(() -> generateAndSaveStudentRecommendation(classEntity, student, currentUser));
        } else {
            // Force regeneration: delete existing recommendation first
            log.info("Force regeneration requested, deleting existing recommendation if any");
            recommendationRepository.findByRecipientAndClass(currentUser.getUserId(), classId)
                    .stream()
                    .filter(rec -> rec.getAudience() == RecommendationAudience.TEACHER)
                    .findFirst()
                    .ifPresent(existing -> {
                        log.info("Deleting existing recommendation {} for regeneration", existing.getId());
                        recommendationRepository.delete(existing);
                    });
            
            return generateAndSaveStudentRecommendation(classEntity, student, currentUser);
        }
    }

    /**
     * Generates and saves a new student recommendation for teacher.
     */
    private RecommendationResponse generateAndSaveStudentRecommendation(Class classEntity, User student, AuthenticatedUser currentUser) {
        // ✅ STEP 3: Aggregate student performance data
        StudentPerformanceData performanceData = aggregateStudentPerformance(classEntity, student);
        
        // ✅ STEP 4: Generate recommendation (outside transaction)
        String recommendationText = openAIService.generateTeacherRecommendationForStudent(performanceData);
        
        // ✅ STEP 5: Save in new transaction
        return saveTeacherRecommendation(classEntity, classEntity.getTeacher(), recommendationText, student);
    }

    /**
     * Aggregates class performance data from all grades in the class.
     */
    @Transactional(readOnly = true)
    private ClassPerformanceData aggregateClassPerformance(Class classEntity) {
        List<Grade> grades = gradeRepository.findByClassEntityId(classEntity.getId());
        
        if (grades.isEmpty()) {
            throw new com.viveek.aiclass.exception.BusinessException("Cannot generate recommendation: No grades found for this class");
        }
        
        // Calculate aggregates
        int totalAssessments = grades.size();
        Set<UUID> uniqueStudents = grades.stream()
                .map(g -> g.getStudent().getId())
                .collect(Collectors.toSet());
        int totalStudents = uniqueStudents.size();
        
        // Group grades by assessment (assessmentKind + assessmentName) to calculate per-assessment averages
        // Filter out grades with invalid scores to avoid calculation errors
        Map<String, List<Grade>> gradesByAssessment = grades.stream()
                .filter(g -> g.getScore() != null && g.getMaxScore() != null && 
                            g.getMaxScore().compareTo(BigDecimal.ZERO) > 0 &&
                            g.getScore().compareTo(BigDecimal.ZERO) >= 0)
                .collect(Collectors.groupingBy(g -> 
                    (g.getAssessmentKind() != null ? g.getAssessmentKind() : "") + "|" + 
                    (g.getAssessmentName() != null ? g.getAssessmentName() : "")));
        
        log.debug("Grouped {} valid grades into {} assessment groups", 
                 grades.stream().filter(g -> g.getScore() != null && g.getMaxScore() != null).count(),
                 gradesByAssessment.size());
        
        // Calculate per-assessment averages (average of all students for each assessment)
        // This will give us the true class average: average of assessment averages
        List<ClassPerformanceData.AssessmentSummary> assessments = new ArrayList<>();
        Map<String, Integer> performanceDistribution = new HashMap<>();
        Set<String> weakAreas = new HashSet<>();
        Set<String> strongAreas = new HashSet<>();
        StringBuilder contentSummary = new StringBuilder();
        StringBuilder feedbackSummary = new StringBuilder();
        
        BigDecimal totalAssessmentAveragePercentage = BigDecimal.ZERO;
        BigDecimal totalAssessmentAverageScore = BigDecimal.ZERO;
        int validAssessments = 0;
        
        for (Map.Entry<String, List<Grade>> entry : gradesByAssessment.entrySet()) {
            List<Grade> assessmentGrades = entry.getValue();
            if (assessmentGrades.isEmpty()) continue;
            
            // Calculate average for this assessment (across all students who took it)
            BigDecimal assessmentTotalScore = BigDecimal.ZERO;
            BigDecimal assessmentTotalMaxScore = BigDecimal.ZERO;
            BigDecimal assessmentTotalPercentage = BigDecimal.ZERO;
            int studentsInAssessment = 0;
            String assessmentContent = null;
            String assessmentKind = null;
            String assessmentName = null;
            
            for (Grade grade : assessmentGrades) {
                if (grade.getScore() != null && grade.getMaxScore() != null) {
                    assessmentTotalScore = assessmentTotalScore.add(grade.getScore());
                    assessmentTotalMaxScore = assessmentTotalMaxScore.add(grade.getMaxScore());
                    
                    BigDecimal percentage = grade.getScore()
                            .divide(grade.getMaxScore(), 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));
                    assessmentTotalPercentage = assessmentTotalPercentage.add(percentage);
                    studentsInAssessment++;
                    
                    // Performance distribution (per individual grade)
                    String level = categorizePerformance(percentage);
                    performanceDistribution.merge(level, 1, Integer::sum);
                    
                    // Identify weak/strong areas based on performance
                    String content = MetadataParser.extractAssessmentContent(grade.getMetadata());
                    if (content != null && !content.isBlank()) {
                        if (assessmentContent == null) {
                            assessmentContent = content;
                        }
                        if (percentage.compareTo(new BigDecimal("60")) < 0) {
                            weakAreas.add(content);
                        } else if (percentage.compareTo(new BigDecimal("85")) >= 0) {
                            strongAreas.add(content);
                        }
                    }
                    
                    if (content != null && !content.isBlank()) {
                        contentSummary.append(content).append("; ");
                    }
                    
                    String feedback = MetadataParser.extractFeedback(grade.getMetadata());
                    if (feedback != null && !feedback.isBlank()) {
                        feedbackSummary.append(feedback).append("; ");
                    }
                    
                    if (assessmentKind == null) {
                        assessmentKind = grade.getAssessmentKind();
                        assessmentName = grade.getAssessmentName();
                    }
                }
            }
            
            // Calculate average for this assessment
            // Note: assessmentAveragePercentage is calculated from individual percentages, not from total scores
            // This ensures correct calculation regardless of maxScore values (e.g., 5 vs 100)
            if (studentsInAssessment > 0) {
                BigDecimal assessmentAverageScore = assessmentTotalScore.divide(
                    new BigDecimal(studentsInAssessment), 2, RoundingMode.HALF_UP);
                BigDecimal assessmentAveragePercentage = assessmentTotalPercentage.divide(
                    new BigDecimal(studentsInAssessment), 2, RoundingMode.HALF_UP);
                
                // Validate calculated values
                if (assessmentAveragePercentage.compareTo(BigDecimal.ZERO) < 0 || 
                    assessmentAveragePercentage.compareTo(new BigDecimal("100")) > 100) {
                    log.warn("Invalid average percentage calculated for assessment {}: {}. Scores: {}, MaxScores: {}, Students: {}", 
                            assessmentName, assessmentAveragePercentage, assessmentTotalScore, assessmentTotalMaxScore, studentsInAssessment);
                }
                
                log.debug("Assessment '{}': Average Score={}, Average Percentage={}%, Students={}", 
                         assessmentName, assessmentAverageScore, assessmentAveragePercentage, studentsInAssessment);
                
                // Add to assessment summaries
                ClassPerformanceData.AssessmentSummary summary = ClassPerformanceData.AssessmentSummary.builder()
                        .assessmentName(assessmentName)
                        .assessmentKind(assessmentKind)
                        .averageScore(assessmentAverageScore)
                        .averagePercentage(assessmentAveragePercentage)
                        .studentCount(studentsInAssessment)
                        .content(assessmentContent)
                        .build();
                assessments.add(summary);
                
                // Accumulate for overall class average (average of assessment averages)
                totalAssessmentAveragePercentage = totalAssessmentAveragePercentage.add(assessmentAveragePercentage);
                totalAssessmentAverageScore = totalAssessmentAverageScore.add(assessmentAverageScore);
                validAssessments++;
            }
        }
        
        // Calculate overall class average: average of all assessment averages
        // This is the true class average - the average of how the class performed across all assessments
        BigDecimal averagePercentage = validAssessments > 0
                ? totalAssessmentAveragePercentage.divide(new BigDecimal(validAssessments), 2, RoundingMode.HALF_UP)
                : null;
        
        BigDecimal averageScore = validAssessments > 0
                ? totalAssessmentAverageScore.divide(new BigDecimal(validAssessments), 2, RoundingMode.HALF_UP)
                : null;

        // 🔍 ADDITIONAL INSIGHTS: Calculate advanced statistics
        
        // Collect all individual percentages for statistical analysis
        List<BigDecimal> allPercentages = new ArrayList<>();
        for (Grade grade : grades) {
            if (grade.getScore() != null && grade.getMaxScore() != null) {
                BigDecimal percentage = grade.getScore()
                        .divide(grade.getMaxScore(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                allPercentages.add(percentage);
            }
        }
        
        // Calculate min, max, median, standard deviation
        BigDecimal minPercentage = null;
        BigDecimal maxPercentage = null;
        BigDecimal medianPercentage = null;
        BigDecimal standardDeviation = null;
        int studentsAtRisk = 0;
        int studentsPerformingWell = 0;
        
        if (!allPercentages.isEmpty()) {
            allPercentages.sort(BigDecimal::compareTo);
            minPercentage = allPercentages.get(0);
            maxPercentage = allPercentages.get(allPercentages.size() - 1);
            
            // Median
            int middle = allPercentages.size() / 2;
            if (allPercentages.size() % 2 == 0) {
                medianPercentage = allPercentages.get(middle - 1)
                        .add(allPercentages.get(middle))
                        .divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
            } else {
                medianPercentage = allPercentages.get(middle);
            }
            
            // Standard deviation
            if (averagePercentage != null && allPercentages.size() > 1) {
                BigDecimal variance = BigDecimal.ZERO;
                for (BigDecimal pct : allPercentages) {
                    BigDecimal diff = pct.subtract(averagePercentage);
                    variance = variance.add(diff.multiply(diff));
                }
                variance = variance.divide(new BigDecimal(allPercentages.size() - 1), 4, RoundingMode.HALF_UP);
                standardDeviation = new BigDecimal(Math.sqrt(variance.doubleValue()));
                standardDeviation = standardDeviation.setScale(2, RoundingMode.HALF_UP);
            }
            
            // Students at risk (< 60%) and performing well (>= 85%)
            // Count UNIQUE students, not individual grades
            Set<UUID> atRiskStudentIds = new HashSet<>();
            Set<UUID> wellPerformingStudentIds = new HashSet<>();
            
            for (Grade grade : grades) {
                if (grade.getScore() != null && grade.getMaxScore() != null) {
                    BigDecimal percentage = grade.getScore()
                            .divide(grade.getMaxScore(), 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));
                    
                    UUID studentId = grade.getStudent().getId();
                    if (percentage.compareTo(new BigDecimal("60")) < 0) {
                        atRiskStudentIds.add(studentId);
                    }
                    if (percentage.compareTo(new BigDecimal("85")) >= 0) {
                        wellPerformingStudentIds.add(studentId);
                    }
                }
            }
            
            studentsAtRisk = atRiskStudentIds.size();
            studentsPerformingWell = wellPerformingStudentIds.size();
        }
        
        BigDecimal atRiskPercentage = totalStudents > 0
                ? new BigDecimal(studentsAtRisk)
                        .divide(new BigDecimal(totalStudents), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(1, RoundingMode.HALF_UP)
                : null;
        
        // Class performance trend (comparing first half vs second half of assessments)
        String classPerformanceTrend = determineClassPerformanceTrend(assessments);
        
        // Best and worst assessments
        String bestAssessment = null;
        String worstAssessment = null;
        String bestAssessmentType = null;
        String worstAssessmentType = null;
        if (!assessments.isEmpty()) {
            assessments.sort((a1, a2) -> {
                if (a1.getAveragePercentage() == null || a2.getAveragePercentage() == null) return 0;
                return a2.getAveragePercentage().compareTo(a1.getAveragePercentage());
            });
            ClassPerformanceData.AssessmentSummary best = assessments.get(0);
            ClassPerformanceData.AssessmentSummary worst = assessments.get(assessments.size() - 1);
            
            bestAssessment = best.getAssessmentName() != null ? best.getAssessmentName() : best.getAssessmentKind();
            worstAssessment = worst.getAssessmentName() != null ? worst.getAssessmentName() : worst.getAssessmentKind();
            bestAssessmentType = best.getAssessmentKind();
            worstAssessmentType = worst.getAssessmentKind();
        }
        
        // Participation rate: average percentage of students evaluated per assessment
        BigDecimal participationRate = null;
        if (!assessments.isEmpty() && totalStudents > 0) {
            int totalPossibleEvaluations = assessments.size() * totalStudents;
            int actualEvaluations = grades.stream()
                    .filter(g -> g.getScore() != null && g.getMaxScore() != null)
                    .mapToInt(g -> 1)
                    .sum();
            if (totalPossibleEvaluations > 0) {
                participationRate = new BigDecimal(actualEvaluations)
                        .divide(new BigDecimal(totalPossibleEvaluations), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(1, RoundingMode.HALF_UP);
            }
        }
        
        return ClassPerformanceData.builder()
                .subjectName(classEntity.getSubject().getName())
                .className(classEntity.getSubject().getName() + " - " + classEntity.getGroupCode())
                .totalStudents(totalStudents)
                .totalAssessments(totalAssessments)
                .averageScore(averageScore)
                .averagePercentage(averagePercentage)
                .assessments(assessments)
                .performanceDistribution(performanceDistribution)
                .commonWeakAreas(new ArrayList<>(weakAreas))
                .commonStrongAreas(new ArrayList<>(strongAreas))
                .assessmentContentSummary(contentSummary.toString().trim())
                .feedbackSummary(feedbackSummary.toString().trim())
                .classPerformanceTrend(classPerformanceTrend)
                .minPercentage(minPercentage)
                .maxPercentage(maxPercentage)
                .medianPercentage(medianPercentage)
                .standardDeviation(standardDeviation)
                .studentsAtRisk(studentsAtRisk)
                .atRiskPercentage(atRiskPercentage)
                .studentsPerformingWell(studentsPerformingWell)
                .bestAssessment(bestAssessment)
                .worstAssessment(worstAssessment)
                .bestAssessmentType(bestAssessmentType)
                .worstAssessmentType(worstAssessmentType)
                .participationRate(participationRate)
                .build();
    }

    /**
     * Aggregates student performance data from all grades for a student in a class.
     */
    @Transactional(readOnly = true)
    private StudentPerformanceData aggregateStudentPerformance(Class classEntity, User student) {
        List<Grade> grades = gradeRepository.findByClassAndStudent(classEntity.getId(), student.getId());
        
        if (grades.isEmpty()) {
            throw new com.viveek.aiclass.exception.BusinessException("Cannot generate recommendation: No grades found for this student in this class");
        }
        
        // Sort by gradedAt (most recent first) for trend analysis
        grades.sort((g1, g2) -> {
            if (g1.getGradedAt() == null && g2.getGradedAt() == null) return 0;
            if (g1.getGradedAt() == null) return 1;
            if (g2.getGradedAt() == null) return -1;
            return g2.getGradedAt().compareTo(g1.getGradedAt());
        });
        
        int totalAssessments = grades.size();
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal totalMaxScore = BigDecimal.ZERO;
        List<StudentPerformanceData.StudentAssessmentSummary> assessmentSummaries = new ArrayList<>();
        Set<String> weakAreas = new HashSet<>();
        Set<String> strongAreas = new HashSet<>();
        StringBuilder feedbackSummary = new StringBuilder();
        List<BigDecimal> percentages = new ArrayList<>();
        
        for (Grade grade : grades) {
            if (grade.getScore() != null && grade.getMaxScore() != null) {
                totalScore = totalScore.add(grade.getScore());
                totalMaxScore = totalMaxScore.add(grade.getMaxScore());
                
                BigDecimal percentage = grade.getScore()
                        .divide(grade.getMaxScore(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                percentages.add(percentage);
                
                StudentPerformanceData.StudentAssessmentSummary summary = StudentPerformanceData.StudentAssessmentSummary.builder()
                        .assessmentName(grade.getAssessmentName())
                        .assessmentKind(grade.getAssessmentKind())
                        .score(grade.getScore())
                        .maxScore(grade.getMaxScore())
                        .percentage(percentage)
                        .content(MetadataParser.extractAssessmentContent(grade.getMetadata()))
                        .feedback(MetadataParser.extractFeedback(grade.getMetadata()))
                        .build();
                assessmentSummaries.add(summary);
                
                String content = MetadataParser.extractAssessmentContent(grade.getMetadata());
                if (content != null && !content.isBlank()) {
                    if (percentage.compareTo(new BigDecimal("60")) < 0) {
                        weakAreas.add(content);
                    } else if (percentage.compareTo(new BigDecimal("85")) >= 0) {
                        strongAreas.add(content);
                    }
                }
                
                String feedback = MetadataParser.extractFeedback(grade.getMetadata());
                if (feedback != null && !feedback.isBlank()) {
                    feedbackSummary.append(feedback).append("; ");
                }
            }
        }
        
        BigDecimal averageScore = totalMaxScore.compareTo(BigDecimal.ZERO) > 0 
                ? totalScore.divide(new BigDecimal(totalAssessments), 2, RoundingMode.HALF_UP)
                : null;
        BigDecimal averagePercentage = totalMaxScore.compareTo(BigDecimal.ZERO) > 0
                ? totalScore.divide(totalMaxScore, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                : null;
        
        // Determine performance trend
        String performanceTrend = determinePerformanceTrend(percentages);
        
        return StudentPerformanceData.builder()
                .studentName(student.getFullName())
                .subjectName(classEntity.getSubject().getName())
                .className(classEntity.getSubject().getName() + " - " + classEntity.getGroupCode())
                .totalAssessments(totalAssessments)
                .averageScore(averageScore)
                .averagePercentage(averagePercentage)
                .performanceTrend(performanceTrend)
                .assessments(assessmentSummaries)
                .weakAreas(new ArrayList<>(weakAreas))
                .strongAreas(new ArrayList<>(strongAreas))
                .feedbackSummary(feedbackSummary.toString().trim())
                .build();
    }

    /**
     * Categorizes performance into levels.
     */
    private String categorizePerformance(BigDecimal percentage) {
        if (percentage.compareTo(new BigDecimal("90")) >= 0) {
            return "Excellent";
        } else if (percentage.compareTo(new BigDecimal("75")) >= 0) {
            return "Good";
        } else if (percentage.compareTo(new BigDecimal("60")) >= 0) {
            return "Satisfactory";
        } else {
            return "Needs Improvement";
        }
    }

    /**
     * Determines performance trend from a list of percentages (most recent first).
     */
    private String determinePerformanceTrend(List<BigDecimal> percentages) {
        if (percentages.size() < 2) {
            return "Insufficient data";
        }
        
        // Compare first 2-3 assessments
        int comparisons = Math.min(3, percentages.size() - 1);
        int improving = 0;
        int declining = 0;
        
        for (int i = 0; i < comparisons; i++) {
            BigDecimal diff = percentages.get(i).subtract(percentages.get(i + 1));
            if (diff.compareTo(new BigDecimal("5")) > 0) {
                improving++;
            } else if (diff.compareTo(new BigDecimal("-5")) < 0) {
                declining++;
            }
        }
        
        if (improving > declining) {
            return "Improving";
        } else if (declining > improving) {
            return "Declining";
        } else {
            return "Stable";
        }
    }

    /**
     * Determines class performance trend by comparing early vs recent assessments.
     */
    private String determineClassPerformanceTrend(List<ClassPerformanceData.AssessmentSummary> assessments) {
        if (assessments.size() < 2) {
            return "Insufficient data";
        }
        
        // Sort assessments by name/kind to get chronological order (assuming naming convention)
        // Or we could use gradedAt if available, but for now we'll use list order
        // First half vs second half
        int midPoint = assessments.size() / 2;
        List<ClassPerformanceData.AssessmentSummary> earlyAssessments = assessments.subList(0, midPoint);
        List<ClassPerformanceData.AssessmentSummary> recentAssessments = assessments.subList(midPoint, assessments.size());
        
        BigDecimal earlyAverage = earlyAssessments.stream()
                .filter(a -> a.getAveragePercentage() != null)
                .map(ClassPerformanceData.AssessmentSummary::getAveragePercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(earlyAssessments.stream()
                        .filter(a -> a.getAveragePercentage() != null)
                        .count()), 2, RoundingMode.HALF_UP);
        
        BigDecimal recentAverage = recentAssessments.stream()
                .filter(a -> a.getAveragePercentage() != null)
                .map(ClassPerformanceData.AssessmentSummary::getAveragePercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(recentAssessments.stream()
                        .filter(a -> a.getAveragePercentage() != null)
                        .count()), 2, RoundingMode.HALF_UP);
        
        BigDecimal diff = recentAverage.subtract(earlyAverage);
        if (diff.compareTo(new BigDecimal("3")) > 0) {
            return "Improving";
        } else if (diff.compareTo(new BigDecimal("-3")) < 0) {
            return "Declining";
        } else {
            return "Stable";
        }
    }

    /**
     * Saves a teacher recommendation in a new transaction.
     */
    @Transactional
    private RecommendationResponse saveTeacherRecommendation(Class classEntity, User teacher, 
                                                             String recommendationText, User student) {
        log.debug("Saving teacher recommendation for class {}", classEntity.getId());
        
        AiRecommendation recommendation = AiRecommendation.builder()
                .classEntity(classEntity)
                .recipient(teacher) // Teacher receives the recommendation
                .grade(null) // Not linked to a specific grade
                .audience(RecommendationAudience.TEACHER)
                .message(recommendationText)
                .build();
        
        AiRecommendation savedRecommendation = recommendationRepository.save(recommendation);
        log.info("Teacher recommendation generated and saved successfully: id={}, classId={}", 
                 savedRecommendation.getId(), classEntity.getId());
        
        return recommendationMapper.toResponse(savedRecommendation);
    }

    @Override
    public void deleteRecommendation(UUID id) {
        log.info("Soft deleting recommendation: id={}", id);
        
        AiRecommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation", "id", id));
        
        // ✅ AUTHORIZATION: Verify the current user is the teacher of the class
        AuthenticatedUser currentUser = SecurityContextHelper.requireAuthentication();
        if (!recommendation.getClassEntity().getTeacher().getId().equals(currentUser.getUserId())) {
            log.warn("Recommendation deletion denied: user {} tried to delete recommendation {} for class owned by teacher {}", 
                     currentUser.getUserId(), id, recommendation.getClassEntity().getTeacher().getId());
            throw new AccessDeniedException("You can only delete recommendations for your classes");
        }
        
        // Use repository.delete() to trigger @SQLDelete annotation
        recommendationRepository.delete(recommendation);
        log.debug("Recommendation soft deleted successfully: id={}", id);
    }
}

