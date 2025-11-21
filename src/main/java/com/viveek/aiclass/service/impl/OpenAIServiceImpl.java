package com.viveek.aiclass.service.impl;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import com.viveek.aiclass.dto.internal.ClassPerformanceData;
import com.viveek.aiclass.dto.internal.RecommendationPromptData;
import com.viveek.aiclass.dto.internal.StudentPerformanceData;
import com.viveek.aiclass.exception.OpenAIServiceException;
import com.viveek.aiclass.service.OpenAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of OpenAIService using the OpenAI Java client.
 */
@Slf4j
@Service
public class OpenAIServiceImpl implements OpenAIService {

    private final OpenAiService openAiService;
    private final String model;
    private final Integer maxTokens;

    public OpenAIServiceImpl(
            @Value("${openai.api.key}") String apiKey,
            @Value("${openai.api.timeout:30000}") Integer timeoutMs,
            @Value("${openai.model:gpt-4o-mini}") String model,
            @Value("${openai.max-tokens:500}") Integer maxTokens) {
        
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key is not configured. Set openai.api.key property.");
        }

        this.model = model;
        this.maxTokens = maxTokens;
        
        // Initialize OpenAI service with timeout
        this.openAiService = new OpenAiService(apiKey, Duration.ofMillis(timeoutMs));
        
        log.info("OpenAI service initialized with model: {}, maxTokens: {}, timeout: {}ms", 
                 model, maxTokens, timeoutMs);
    }

    @Override
    public String generateRecommendation(RecommendationPromptData promptData) {
        log.debug("Generating recommendation for assessment: {}", promptData.getAssessmentName());
        
        try {
            // Detect language from input data
            String detectedLanguage = detectLanguage(promptData);
            log.debug("Detected language: {}", detectedLanguage);
            
            String prompt = buildPrompt(promptData, detectedLanguage);
            
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), buildSystemMessage(detectedLanguage)));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt));
            
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .maxTokens(maxTokens)
                    .temperature(0.6) // Slightly lower for more consistent, focused responses
                    .build();
            
            String response = openAiService.createChatCompletion(request)
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();
            
            log.debug("Successfully generated recommendation (length: {})", response.length());
            return response.trim();
            
        } catch (Exception e) {
            log.error("Failed to generate recommendation via OpenAI API", e);
            throw new OpenAIServiceException("Failed to generate recommendation: " + e.getMessage(), e);
        }
    }

    /**
     * Detects the language of the input data by analyzing text fields.
     * Checks for Spanish indicators (common Spanish words, characters, etc.).
     * Returns "Spanish" if Spanish is detected, "English" otherwise.
     */
    private String detectLanguage(RecommendationPromptData data) {
        // Collect all text fields that might contain language indicators
        StringBuilder textToAnalyze = new StringBuilder();
        
        if (data.getSubjectName() != null) {
            textToAnalyze.append(data.getSubjectName()).append(" ");
        }
        if (data.getAssessmentName() != null) {
            textToAnalyze.append(data.getAssessmentName()).append(" ");
        }
        if (data.getAssessmentKind() != null) {
            textToAnalyze.append(data.getAssessmentKind()).append(" ");
        }
        if (data.getAssessmentContent() != null) {
            textToAnalyze.append(data.getAssessmentContent()).append(" ");
        }
        if (data.getFeedback() != null) {
            textToAnalyze.append(data.getFeedback()).append(" ");
        }
        
        String combinedText = textToAnalyze.toString().toLowerCase().trim();
        
        if (combinedText.isEmpty()) {
            return "English"; // Default to English if no text available
        }
        
        // Spanish indicators: common Spanish words, characters, and patterns
        String[] spanishIndicators = {
            "á", "é", "í", "ó", "ú", "ñ", "ü", // Spanish-specific characters
            " el ", " la ", " los ", " las ", " de ", " del ", " que ", " y ", " en ", " un ", " una ",
            " con ", " por ", " para ", " sobre ", " entre ", " durante ", " según ", " mediante ",
            " evaluación ", " retroalimentación ", " contenido ", " materia ", " asignatura ",
            " examen ", " prueba ", " tarea ", " proyecto ", " calificación ", " puntaje ",
            " estudiante ", " profesor ", " maestro ", " mejorar ", " entender ", " practicar "
        };
        
        // Count Spanish indicators
        int spanishCount = 0;
        for (String indicator : spanishIndicators) {
            if (combinedText.contains(indicator)) {
                spanishCount++;
            }
        }
        
        // If we find multiple Spanish indicators, it's likely Spanish
        // Threshold: at least 2 indicators suggests Spanish content
        if (spanishCount >= 2) {
            return "Spanish";
        }
        
        // Also check for common Spanish words that appear frequently
        String[] commonSpanishWords = {
            "examen", "prueba", "tarea", "proyecto", "calificación", "puntaje",
            "estudiante", "profesor", "maestro", "mejorar", "entender", "practicar",
            "evaluación", "retroalimentación", "contenido", "materia", "asignatura"
        };
        
        for (String word : commonSpanishWords) {
            if (combinedText.contains(word)) {
                return "Spanish";
            }
        }
        
        return "English"; // Default to English
    }

    /**
     * Builds a comprehensive system message with detailed guidelines for generating high-quality recommendations.
     * Includes language-specific instructions.
     */
    private String buildSystemMessage(String detectedLanguage) {
        String languageInstruction = detectedLanguage.equals("Spanish") 
            ? "\n\nCRITICAL LANGUAGE REQUIREMENT:\n" +
              "You MUST generate your entire response in Spanish (Español). All recommendations, explanations, " +
              "and text must be written in Spanish, matching the language of the input data provided. " +
              "Use proper Spanish grammar, vocabulary, and educational terminology.\n"
            : "\n\nLANGUAGE REQUIREMENT:\n" +
              "Generate your response in English. If the input data is in a different language, " +
              "still respond in English unless explicitly instructed otherwise.\n";
        
        return "You are an expert educational advisor specializing in personalized learning recommendations. " +
                "Your role is to analyze student assessment performance and provide highly specific, actionable, " +
                "and evidence-based recommendations that will genuinely help students improve.\n\n" +
                
                "CRITICAL GUIDELINES FOR RECOMMENDATIONS:\n" +
                "1. SPECIFICITY: Provide concrete, measurable actions - not vague advice. Instead of 'study more', " +
                "say 'dedicate 30 minutes daily to practicing quadratic equations, focusing on completing 5 problems from chapter 4'.\n" +
                "2. EVIDENCE-BASED: Base recommendations on the actual assessment content, performance gaps, and teacher feedback provided.\n" +
                "3. PERFORMANCE-APPROPRIATE: Tailor recommendations to the student's performance level:\n" +
                "   - 90-100%: Focus on advanced topics, deeper understanding, and maintaining excellence\n" +
                "   - 75-89%: Identify specific weak areas and provide targeted practice strategies\n" +
                "   - 60-74%: Emphasize foundational concepts, review basics, and structured study plans\n" +
                "   - Below 60%: Prioritize core concepts, seek additional support, and break down complex topics\n" +
                "4. ACTIONABLE STEPS: Include 2-4 specific, sequential actions the student can take immediately.\n" +
                "5. ENCOURAGING TONE: Be supportive and growth-oriented, acknowledging effort while identifying improvement areas.\n" +
                "6. SUBJECT-SPECIFIC: Reference the actual subject matter and assessment topics when providing recommendations.\n" +
                "7. TIME-BOUND: Suggest realistic timelines (e.g., 'over the next 2 weeks', 'before the next assessment').\n" +
                languageInstruction +
                
                "RECOMMENDATION STRUCTURE:\n" +
                "Your response should follow this format:\n" +
                "1. Brief performance acknowledgment (1-2 sentences)\n" +
                "2. Key strengths identified (if applicable, 1-2 points)\n" +
                "3. Specific areas for improvement (2-3 points based on assessment content and feedback)\n" +
                "4. Actionable recommendations (2-4 concrete steps with specific resources/topics)\n" +
                "5. Encouraging closing statement\n\n" +
                
                "AVOID:\n" +
                "- Generic phrases like 'study harder' or 'pay more attention'\n" +
                "- Recommendations not tied to the specific assessment content\n" +
                "- Overly negative or discouraging language\n" +
                "- Vague suggestions without specific actions or resources\n" +
                "- Recommendations that ignore the teacher's feedback\n\n" +
                
                "Your recommendations should be professional, empathetic, and designed to genuinely help the student " +
                "understand their performance and take concrete steps toward improvement.";
    }

    /**
     * Builds a structured, comprehensive prompt from the prompt data.
     * Includes language-specific instructions based on detected language.
     */
    private String buildPrompt(RecommendationPromptData data, String detectedLanguage) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("STUDENT ASSESSMENT ANALYSIS REQUEST\n");
        prompt.append("==================================================\n\n");
        
        // Context Section
        prompt.append("ASSESSMENT CONTEXT:\n");
        prompt.append("- Subject: ").append(data.getSubjectName() != null ? data.getSubjectName() : "Not specified").append("\n");
        
        if (data.getAssessmentKind() != null && !data.getAssessmentKind().isBlank()) {
            prompt.append("- Assessment Type: ").append(data.getAssessmentKind()).append("\n");
        }
        
        if (data.getAssessmentName() != null && !data.getAssessmentName().isBlank()) {
            prompt.append("- Assessment Name: ").append(data.getAssessmentName()).append("\n");
        }
        
        // Performance Analysis
        if (data.getScore() != null && data.getMaxScore() != null) {
            BigDecimal percentage = data.getPercentage();
            prompt.append("\nPERFORMANCE METRICS:\n");
            prompt.append("- Score: ").append(data.getScore()).append(" / ").append(data.getMaxScore());
            if (percentage != null) {
                BigDecimal pct = percentage.setScale(1, java.math.RoundingMode.HALF_UP);
                prompt.append(" (").append(pct).append("%)\n");
                
                // Add performance level context
                prompt.append("- Performance Level: ");
                if (pct.compareTo(new BigDecimal("90")) >= 0) {
                    prompt.append("Excellent - Student demonstrates strong mastery\n");
                } else if (pct.compareTo(new BigDecimal("75")) >= 0) {
                    prompt.append("Good - Student shows solid understanding with room for improvement\n");
                } else if (pct.compareTo(new BigDecimal("60")) >= 0) {
                    prompt.append("Satisfactory - Student needs to strengthen foundational concepts\n");
                } else {
                    prompt.append("Needs Improvement - Student requires focused support on core concepts\n");
                }
            } else {
                prompt.append("\n");
            }
        }
        
        // Assessment Content
        if (data.getAssessmentContent() != null && !data.getAssessmentContent().isBlank()) {
            prompt.append("\nASSESSMENT CONTENT/TOPICS COVERED:\n");
            prompt.append(data.getAssessmentContent()).append("\n");
        }
        
        // Teacher Feedback
        if (data.getFeedback() != null && !data.getFeedback().isBlank()) {
            prompt.append("\nTEACHER FEEDBACK:\n");
            prompt.append(data.getFeedback()).append("\n");
        }
        
        // Instructions
        prompt.append("\n==================================================\n");
        prompt.append("YOUR TASK:\n\n");
        prompt.append("Based on the information above, generate a comprehensive, personalized recommendation that:\n\n");
        
        prompt.append("1. ACKNOWLEDGES PERFORMANCE: Start with a brief, specific acknowledgment of their performance level " +
                     "and effort (e.g., 'You scored 78% on the Linear Algebra midterm, demonstrating solid understanding " +
                     "of matrix operations but showing gaps in eigenvalue calculations.')\n\n");
        
        prompt.append("2. IDENTIFIES SPECIFIC AREAS: Based on the assessment content, score breakdown, and teacher feedback, " +
                     "identify 2-3 specific topics or skills that need attention. Reference actual concepts from the assessment.\n\n");
        
        prompt.append("3. PROVIDES ACTIONABLE STEPS: Give 2-4 concrete, specific actions the student can take. Each action should:\n");
        prompt.append("   - Be specific (mention exact chapters, topics, or exercises)\n");
        prompt.append("   - Be measurable (include quantities like '5 problems daily' or '30 minutes')\n");
        prompt.append("   - Be time-bound (suggest when to complete it)\n");
        prompt.append("   - Reference the actual subject matter and assessment topics\n\n");
        
        prompt.append("4. SUGGESTS RESOURCES/STRATEGIES: Recommend specific study strategies, practice methods, or resources " +
                     "relevant to the subject and identified weak areas.\n\n");
        
        prompt.append("5. CLOSES ENCOURAGINGLY: End with a supportive, growth-oriented statement that motivates improvement.\n\n");
        
        prompt.append("IMPORTANT:\n");
        prompt.append("- If teacher feedback is provided, ensure your recommendations align with and build upon it\n");
        prompt.append("- Reference specific topics from the assessment content when possible\n");
        prompt.append("- Make recommendations appropriate for the performance level identified\n");
        prompt.append("- Keep the total response between 150-250 words - comprehensive but concise\n");
        prompt.append("- Write in a supportive, professional tone suitable for a student\n");
        prompt.append("- Focus on actionable steps the student can implement immediately\n");
        
        // Add language-specific instruction
        if (detectedLanguage.equals("Spanish")) {
            prompt.append("\n");
            prompt.append("CRITICAL: You MUST generate your entire response in Spanish (Español). " +
                         "The input data (subject name, assessment content, teacher feedback) is in Spanish, " +
                         "so your recommendation must be written entirely in Spanish. " +
                         "Use proper Spanish grammar, vocabulary, and educational terminology. " +
                         "Do not mix languages - write everything in Spanish.\n");
        } else {
            prompt.append("\n");
            prompt.append("LANGUAGE: Generate your response in English.\n");
        }
        
        prompt.append("\nGenerate the recommendation now:");
        
        return prompt.toString();
    }

    @Override
    public String generateTeacherRecommendationForClass(ClassPerformanceData performanceData) {
        log.debug("Generating teacher recommendation for class: {}", performanceData.getClassName());
        
        try {
            String detectedLanguage = detectLanguageFromClassData(performanceData);
            log.debug("Detected language: {}", detectedLanguage);
            
            String prompt = buildTeacherClassPrompt(performanceData, detectedLanguage);
            
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), buildTeacherSystemMessage(detectedLanguage)));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt));
            
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .maxTokens(maxTokens)
                    .temperature(0.6)
                    .build();
            
            String response = openAiService.createChatCompletion(request)
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();
            
            log.debug("Successfully generated teacher recommendation for class (length: {})", response.length());
            return response.trim();
            
        } catch (Exception e) {
            log.error("Failed to generate teacher recommendation for class via OpenAI API", e);
            throw new OpenAIServiceException("Failed to generate teacher recommendation: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateTeacherRecommendationForStudent(StudentPerformanceData performanceData) {
        log.debug("Generating teacher recommendation for student: {}", performanceData.getStudentName());
        
        try {
            String detectedLanguage = detectLanguageFromStudentData(performanceData);
            log.debug("Detected language: {}", detectedLanguage);
            
            String prompt = buildTeacherStudentPrompt(performanceData, detectedLanguage);
            
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), buildTeacherSystemMessage(detectedLanguage)));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt));
            
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .maxTokens(maxTokens)
                    .temperature(0.6)
                    .build();
            
            String response = openAiService.createChatCompletion(request)
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();
            
            log.debug("Successfully generated teacher recommendation for student (length: {})", response.length());
            return response.trim();
            
        } catch (Exception e) {
            log.error("Failed to generate teacher recommendation for student via OpenAI API", e);
            throw new OpenAIServiceException("Failed to generate teacher recommendation: " + e.getMessage(), e);
        }
    }

    /**
     * Detects language from class performance data.
     */
    private String detectLanguageFromClassData(ClassPerformanceData data) {
        StringBuilder textToAnalyze = new StringBuilder();
        
        if (data.getSubjectName() != null) {
            textToAnalyze.append(data.getSubjectName()).append(" ");
        }
        if (data.getClassName() != null) {
            textToAnalyze.append(data.getClassName()).append(" ");
        }
        if (data.getAssessmentContentSummary() != null) {
            textToAnalyze.append(data.getAssessmentContentSummary()).append(" ");
        }
        if (data.getFeedbackSummary() != null) {
            textToAnalyze.append(data.getFeedbackSummary()).append(" ");
        }
        if (data.getCommonWeakAreas() != null) {
            data.getCommonWeakAreas().forEach(area -> textToAnalyze.append(area).append(" "));
        }
        
        return detectLanguageFromText(textToAnalyze.toString());
    }

    /**
     * Detects language from student performance data.
     */
    private String detectLanguageFromStudentData(StudentPerformanceData data) {
        StringBuilder textToAnalyze = new StringBuilder();
        
        if (data.getSubjectName() != null) {
            textToAnalyze.append(data.getSubjectName()).append(" ");
        }
        if (data.getClassName() != null) {
            textToAnalyze.append(data.getClassName()).append(" ");
        }
        if (data.getFeedbackSummary() != null) {
            textToAnalyze.append(data.getFeedbackSummary()).append(" ");
        }
        if (data.getWeakAreas() != null) {
            data.getWeakAreas().forEach(area -> textToAnalyze.append(area).append(" "));
        }
        if (data.getAssessments() != null) {
            data.getAssessments().forEach(assessment -> {
                if (assessment.getContent() != null) {
                    textToAnalyze.append(assessment.getContent()).append(" ");
                }
                if (assessment.getFeedback() != null) {
                    textToAnalyze.append(assessment.getFeedback()).append(" ");
                }
            });
        }
        
        return detectLanguageFromText(textToAnalyze.toString());
    }

    /**
     * Detects language from text using Spanish indicators.
     */
    private String detectLanguageFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "English";
        }
        
        String combinedText = text.toLowerCase().trim();
        
        String[] spanishIndicators = {
            "á", "é", "í", "ó", "ú", "ñ", "ü",
            " el ", " la ", " los ", " las ", " de ", " del ", " que ", " y ", " en ",
            " evaluación ", " retroalimentación ", " contenido ", " materia ", " asignatura ",
            " examen ", " prueba ", " tarea ", " calificación ", " estudiante ", " profesor "
        };
        
        int spanishCount = 0;
        for (String indicator : spanishIndicators) {
            if (combinedText.contains(indicator)) {
                spanishCount++;
            }
        }
        
        if (spanishCount >= 2) {
            return "Spanish";
        }
        
        String[] commonSpanishWords = {
            "examen", "prueba", "tarea", "calificación", "estudiante", "profesor",
            "evaluación", "retroalimentación", "contenido", "materia", "asignatura"
        };
        
        for (String word : commonSpanishWords) {
            if (combinedText.contains(word)) {
                return "Spanish";
            }
        }
        
        return "English";
    }

    /**
     * Builds system message for teacher recommendations.
     */
    private String buildTeacherSystemMessage(String detectedLanguage) {
        String languageInstruction = detectedLanguage.equals("Spanish") 
            ? "\n\nCRITICAL LANGUAGE REQUIREMENT:\n" +
              "You MUST generate your entire response in Spanish (Español). All recommendations, explanations, " +
              "and text must be written in Spanish, matching the language of the input data provided. " +
              "Use proper Spanish grammar, vocabulary, and educational terminology.\n"
            : "\n\nLANGUAGE REQUIREMENT:\n" +
              "Generate your response in English. If the input data is in a different language, " +
              "still respond in English unless explicitly instructed otherwise.\n";
        
        return "You are an expert educational consultant specializing in providing actionable teaching strategies " +
                "and intervention recommendations to educators. Your role is to analyze student and class performance data " +
                "and provide specific, evidence-based recommendations that help teachers improve their instruction and " +
                "better support their students.\n\n" +
                
                "CRITICAL GUIDELINES FOR TEACHER RECOMMENDATIONS:\n" +
                "1. TEACHING-FOCUSED: Provide recommendations for the TEACHER about how to teach, intervene, or support students.\n" +
                "2. EVIDENCE-BASED: Base recommendations on actual performance data, patterns, and trends provided.\n" +
                "3. ACTIONABLE: Give specific teaching strategies, intervention methods, or instructional approaches.\n" +
                "4. CLASS-LEVEL INSIGHTS: For class recommendations, identify common patterns, group needs, and class-wide strategies.\n" +
                "5. STUDENT-SPECIFIC: For student recommendations, provide personalized teaching approaches and support strategies.\n" +
                "6. PRACTICAL: Suggest concrete actions teachers can implement in their classroom.\n" +
                "7. DATA-DRIVEN: Reference specific performance metrics, trends, and patterns from the data.\n" +
                languageInstruction +
                
                "RECOMMENDATION STRUCTURE:\n" +
                "1. Performance summary (brief overview of class/student performance)\n" +
                "2. Key patterns identified (2-3 main patterns or trends)\n" +
                "3. Teaching strategies (2-4 specific instructional approaches)\n" +
                "4. Intervention recommendations (specific actions for struggling areas)\n" +
                "5. Next steps (concrete actions to take)\n\n" +
                
                "AVOID:\n" +
                "- Generic advice like 'review the material' or 'provide more practice'\n" +
                "- Student-facing recommendations (these are for teachers, not students)\n" +
                "- Vague suggestions without specific teaching methods\n" +
                "- Recommendations not tied to the performance data provided\n\n" +
                
                "Your recommendations should be professional, practical, and designed to help teachers make " +
                "informed instructional decisions based on student performance data.";
    }

    /**
     * Builds prompt for teacher class recommendations.
     */
    private String buildTeacherClassPrompt(ClassPerformanceData data, String detectedLanguage) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("CLASS PERFORMANCE ANALYSIS REQUEST\n");
        prompt.append("==================================================\n\n");
        
        prompt.append("CLASS INFORMATION:\n");
        prompt.append("- Subject: ").append(data.getSubjectName() != null ? data.getSubjectName() : "Not specified").append("\n");
        prompt.append("- Class: ").append(data.getClassName() != null ? data.getClassName() : "Not specified").append("\n");
        prompt.append("- Total Students: ").append(data.getTotalStudents() != null ? data.getTotalStudents() : "Unknown").append("\n");
        prompt.append("- Total Assessments: ").append(data.getTotalAssessments() != null ? data.getTotalAssessments() : "Unknown").append("\n");
        
        if (data.getAveragePercentage() != null) {
            prompt.append("- Average Class Performance: ").append(data.getAveragePercentage().setScale(1, java.math.RoundingMode.HALF_UP)).append("%\n");
        }
        
        if (data.getPerformanceDistribution() != null && !data.getPerformanceDistribution().isEmpty()) {
            prompt.append("\nPERFORMANCE DISTRIBUTION:\n");
            data.getPerformanceDistribution().forEach((level, count) -> 
                prompt.append("- ").append(level).append(": ").append(count).append(" students\n"));
        }
        
        if (data.getCommonWeakAreas() != null && !data.getCommonWeakAreas().isEmpty()) {
            prompt.append("\nCOMMON WEAK AREAS ACROSS CLASS:\n");
            data.getCommonWeakAreas().forEach(area -> prompt.append("- ").append(area).append("\n"));
        }
        
        if (data.getCommonStrongAreas() != null && !data.getCommonStrongAreas().isEmpty()) {
            prompt.append("\nCOMMON STRONG AREAS ACROSS CLASS:\n");
            data.getCommonStrongAreas().forEach(area -> prompt.append("- ").append(area).append("\n"));
        }
        
        if (data.getAssessmentContentSummary() != null && !data.getAssessmentContentSummary().isBlank()) {
            prompt.append("\nASSESSMENT CONTENT SUMMARY:\n");
            prompt.append(data.getAssessmentContentSummary()).append("\n");
        }
        
        if (data.getFeedbackSummary() != null && !data.getFeedbackSummary().isBlank()) {
            prompt.append("\nFEEDBACK PATTERNS:\n");
            prompt.append(data.getFeedbackSummary()).append("\n");
        }
        
        if (data.getAssessments() != null && !data.getAssessments().isEmpty()) {
            prompt.append("\nASSESSMENT DETAILS:\n");
            data.getAssessments().forEach(assessment -> {
                prompt.append("- ").append(assessment.getAssessmentName() != null ? assessment.getAssessmentName() : assessment.getAssessmentKind());
                if (assessment.getAveragePercentage() != null) {
                    prompt.append(": Average ").append(assessment.getAveragePercentage().setScale(1, java.math.RoundingMode.HALF_UP)).append("%");
                }
                prompt.append("\n");
            });
        }
        
        prompt.append("\n==================================================\n");
        prompt.append("YOUR TASK:\n\n");
        prompt.append("Based on the class performance data above, generate comprehensive teaching recommendations that:\n\n");
        
        prompt.append("1. ANALYZE PERFORMANCE: Identify key patterns, trends, and areas of concern for the class as a whole.\n\n");
        prompt.append("2. SUGGEST TEACHING STRATEGIES: Provide 2-4 specific instructional approaches to address identified needs.\n\n");
        prompt.append("3. RECOMMEND INTERVENTIONS: Suggest concrete interventions for struggling areas (e.g., review sessions, " +
                     "additional practice, alternative teaching methods).\n\n");
        prompt.append("4. IDENTIFY FOCUS AREAS: Recommend specific topics or concepts to emphasize in upcoming lessons.\n\n");
        prompt.append("5. PROVIDE NEXT STEPS: Give actionable steps the teacher can take immediately.\n\n");
        
        if (detectedLanguage.equals("Spanish")) {
            prompt.append("CRITICAL: You MUST generate your entire response in Spanish (Español). " +
                         "The input data is in Spanish, so your recommendation must be written entirely in Spanish.\n");
        } else {
            prompt.append("LANGUAGE: Generate your response in English.\n");
        }
        
        prompt.append("\nGenerate the teacher recommendation now:");
        
        return prompt.toString();
    }

    /**
     * Builds prompt for teacher student recommendations.
     */
    private String buildTeacherStudentPrompt(StudentPerformanceData data, String detectedLanguage) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("STUDENT PERFORMANCE ANALYSIS REQUEST\n");
        prompt.append("==================================================\n\n");
        
        prompt.append("STUDENT INFORMATION:\n");
        prompt.append("- Student: ").append(data.getStudentName() != null ? data.getStudentName() : "Not specified").append("\n");
        prompt.append("- Subject: ").append(data.getSubjectName() != null ? data.getSubjectName() : "Not specified").append("\n");
        prompt.append("- Class: ").append(data.getClassName() != null ? data.getClassName() : "Not specified").append("\n");
        prompt.append("- Total Assessments: ").append(data.getTotalAssessments() != null ? data.getTotalAssessments() : "Unknown").append("\n");
        
        if (data.getAveragePercentage() != null) {
            prompt.append("- Average Performance: ").append(data.getAveragePercentage().setScale(1, java.math.RoundingMode.HALF_UP)).append("%\n");
        }
        
        if (data.getPerformanceTrend() != null && !data.getPerformanceTrend().isBlank()) {
            prompt.append("- Performance Trend: ").append(data.getPerformanceTrend()).append("\n");
        }
        
        if (data.getWeakAreas() != null && !data.getWeakAreas().isEmpty()) {
            prompt.append("\nIDENTIFIED WEAK AREAS:\n");
            data.getWeakAreas().forEach(area -> prompt.append("- ").append(area).append("\n"));
        }
        
        if (data.getStrongAreas() != null && !data.getStrongAreas().isEmpty()) {
            prompt.append("\nIDENTIFIED STRONG AREAS:\n");
            data.getStrongAreas().forEach(area -> prompt.append("- ").append(area).append("\n"));
        }
        
        if (data.getFeedbackSummary() != null && !data.getFeedbackSummary().isBlank()) {
            prompt.append("\nFEEDBACK PATTERNS:\n");
            prompt.append(data.getFeedbackSummary()).append("\n");
        }
        
        if (data.getAssessments() != null && !data.getAssessments().isEmpty()) {
            prompt.append("\nRECENT ASSESSMENT PERFORMANCE:\n");
            data.getAssessments().forEach(assessment -> {
                prompt.append("- ").append(assessment.getAssessmentName() != null ? assessment.getAssessmentName() : assessment.getAssessmentKind());
                if (assessment.getPercentage() != null) {
                    prompt.append(": ").append(assessment.getPercentage().setScale(1, java.math.RoundingMode.HALF_UP)).append("%");
                }
                prompt.append("\n");
            });
        }
        
        prompt.append("\n==================================================\n");
        prompt.append("YOUR TASK:\n\n");
        prompt.append("Based on this student's performance data, generate personalized teaching recommendations that:\n\n");
        
        prompt.append("1. ANALYZE STUDENT PERFORMANCE: Identify specific patterns, strengths, and areas needing support.\n\n");
        prompt.append("2. SUGGEST PERSONALIZED STRATEGIES: Provide 2-4 specific teaching approaches tailored to this student's needs.\n\n");
        prompt.append("3. RECOMMEND INTERVENTIONS: Suggest concrete support strategies (e.g., one-on-one sessions, " +
                     "modified assignments, additional resources).\n\n");
        prompt.append("4. IDENTIFY SUPPORT AREAS: Recommend specific topics or skills to focus on with this student.\n\n");
        prompt.append("5. PROVIDE ACTIONABLE STEPS: Give specific actions the teacher can take to support this student.\n\n");
        
        if (detectedLanguage.equals("Spanish")) {
            prompt.append("CRITICAL: You MUST generate your entire response in Spanish (Español). " +
                         "The input data is in Spanish, so your recommendation must be written entirely in Spanish.\n");
        } else {
            prompt.append("LANGUAGE: Generate your response in English.\n");
        }
        
        prompt.append("\nGenerate the teacher recommendation now:");
        
        return prompt.toString();
    }
}

