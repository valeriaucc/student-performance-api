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
            @Value("${openai.api.timeout:120000}") Integer timeoutMs,
            @Value("${openai.model:gpt-4o-mini}") String model,
            @Value("${openai.max-tokens:1500}") Integer maxTokens) {
        
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
     * Builds a structured, comprehensive prompt from the prompt data for a SINGLE assessment.
     * This is used for student-facing recommendations based on one specific grade/evaluation.
     * Includes language-specific instructions based on detected language.
     */
    private String buildPrompt(RecommendationPromptData data, String detectedLanguage) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("SINGLE ASSESSMENT ANALYSIS REQUEST\n");
        prompt.append("==================================================\n\n");
        prompt.append("You are analyzing ONE specific assessment. Focus your recommendation ONLY on this assessment.\n\n");
        
        // Context Section
        prompt.append("ASSESSMENT INFORMATION:\n");
        prompt.append("- Subject: ").append(data.getSubjectName() != null ? data.getSubjectName() : "Not specified").append("\n");
        
        if (data.getAssessmentKind() != null && !data.getAssessmentKind().isBlank()) {
            prompt.append("- Type: ").append(data.getAssessmentKind()).append("\n");
        }
        
        if (data.getAssessmentName() != null && !data.getAssessmentName().isBlank()) {
            prompt.append("- Name: ").append(data.getAssessmentName()).append("\n");
        }
        
        // Performance Analysis
        if (data.getScore() != null && data.getMaxScore() != null) {
            BigDecimal percentage = data.getPercentage();
            prompt.append("\nPERFORMANCE ON THIS ASSESSMENT:\n");
            prompt.append("- Score: ").append(data.getScore()).append(" / ").append(data.getMaxScore());
            if (percentage != null) {
                BigDecimal pct = percentage.setScale(1, java.math.RoundingMode.HALF_UP);
                prompt.append(" (").append(pct).append("%)\n");
                
                // Add performance level context with specific guidance
                prompt.append("- Performance Level: ");
                String performanceGuidance = "";
                if (pct.compareTo(new BigDecimal("90")) >= 0) {
                    prompt.append("Excellent - Strong mastery demonstrated\n");
                    performanceGuidance = "Focus on advanced topics and deeper understanding to maintain excellence.";
                } else if (pct.compareTo(new BigDecimal("75")) >= 0) {
                    prompt.append("Good - Solid understanding with room for improvement\n");
                    performanceGuidance = "Identify specific weak areas and provide targeted practice strategies.";
                } else if (pct.compareTo(new BigDecimal("60")) >= 0) {
                    prompt.append("Satisfactory - Needs to strengthen foundational concepts\n");
                    performanceGuidance = "Emphasize foundational concepts, review basics, and structured study plans.";
                } else {
                    prompt.append("Needs Improvement - Requires focused support on core concepts\n");
                    performanceGuidance = "Prioritize core concepts, seek additional support, and break down complex topics.";
                }
                prompt.append("  → Recommendation focus: ").append(performanceGuidance).append("\n");
            } else {
                prompt.append("\n");
            }
        }
        
        // Assessment Content
        if (data.getAssessmentContent() != null && !data.getAssessmentContent().isBlank()) {
            prompt.append("\nTOPICS COVERED IN THIS ASSESSMENT:\n");
            prompt.append(data.getAssessmentContent()).append("\n");
        } else {
            prompt.append("\nNOTE: No specific assessment content provided. Base recommendations on the assessment type and performance level.\n");
        }
        
        // Teacher Feedback
        if (data.getFeedback() != null && !data.getFeedback().isBlank()) {
            prompt.append("\nTEACHER FEEDBACK ON THIS ASSESSMENT:\n");
            prompt.append("\"").append(data.getFeedback()).append("\"\n");
            prompt.append("→ Use this feedback to identify specific areas that need attention.\n");
        } else {
            prompt.append("\nNOTE: No teacher feedback provided. Base recommendations on performance level and assessment content.\n");
        }
        
        // Instructions
        prompt.append("\n==================================================\n");
        prompt.append("YOUR TASK: Generate a personalized recommendation for THIS SPECIFIC ASSESSMENT (150-200 words)\n\n");
        
        prompt.append("OUTPUT STRUCTURE:\n\n");
        
        prompt.append("1. PERFORMANCE ACKNOWLEDGMENT (1-2 sentences)\n");
        prompt.append("   Start with a brief, specific acknowledgment of their performance on THIS assessment.\n");
        prompt.append("   Example: 'You scored 78% on the Linear Algebra midterm, demonstrating solid understanding " +
                     "of matrix operations but showing gaps in eigenvalue calculations.'\n\n");
        
        prompt.append("2. SPECIFIC AREAS TO FOCUS (2-3 bullet points)\n");
        prompt.append("   Based on:\n");
        prompt.append("   - The assessment content/topics covered\n");
        prompt.append("   - The performance level (score and percentage)\n");
        prompt.append("   - Teacher feedback (if provided)\n");
        prompt.append("   Identify 2-3 specific topics or skills from THIS assessment that need attention.\n\n");
        
        prompt.append("3. ACTIONABLE STEPS (2-3 concrete actions)\n");
        prompt.append("   Provide 2-3 specific actions the student can take to improve in the identified areas.\n");
        prompt.append("   Each action MUST:\n");
        prompt.append("   - Be specific (mention exact chapters, topics, or exercises from THIS assessment)\n");
        prompt.append("   - Be measurable (include quantities: '5 problems daily', '30 minutes', 'review chapter 4')\n");
        prompt.append("   - Be time-bound ('over the next week', 'before the next assessment')\n");
        prompt.append("   - Reference the actual subject matter and topics from THIS assessment\n\n");
        
        prompt.append("4. ENCOURAGING CLOSING (1 sentence)\n");
        prompt.append("   End with a supportive, growth-oriented statement that motivates improvement.\n\n");
        
        prompt.append("CRITICAL REQUIREMENTS:\n");
        prompt.append("- Focus ONLY on THIS assessment - do not reference other assessments or general performance\n");
        prompt.append("- If teacher feedback is provided, ensure your recommendations align with and build upon it\n");
        prompt.append("- Reference specific topics from the assessment content when possible\n");
        prompt.append("- Make recommendations appropriate for the performance level identified above\n");
        prompt.append("- Keep response between 150-200 words - concise but comprehensive\n");
        prompt.append("- Write in a supportive, professional tone suitable for a student\n");
        prompt.append("- Avoid generic phrases like 'study harder' or 'pay more attention'\n");
        prompt.append("- Provide concrete, actionable steps the student can implement immediately\n");
        
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
            
            // Log key data for debugging
            log.debug("Class performance data - Average: {}, Total Students: {}, Total Assessments: {}", 
                     performanceData.getAveragePercentage(), 
                     performanceData.getTotalStudents(), 
                     performanceData.getTotalAssessments());
            if (performanceData.getAssessments() != null) {
                performanceData.getAssessments().forEach(assessment -> {
                    log.debug("Assessment: {} - Average: {}%, Students: {}", 
                             assessment.getAssessmentName(),
                             assessment.getAveragePercentage(),
                             assessment.getStudentCount());
                });
            }
            
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
     * Analyzes feedbacks, assessment descriptions, and statistics to determine language.
     */
    private String detectLanguageFromClassData(ClassPerformanceData data) {
        StringBuilder textToAnalyze = new StringBuilder();
        
        // Subject and class names (often bilingual, but can help)
        if (data.getSubjectName() != null) {
            textToAnalyze.append(data.getSubjectName()).append(" ");
        }
        if (data.getClassName() != null) {
            textToAnalyze.append(data.getClassName()).append(" ");
        }
        
        // Assessment content summary (critical for language detection)
        if (data.getAssessmentContentSummary() != null && !data.getAssessmentContentSummary().isBlank()) {
            textToAnalyze.append(data.getAssessmentContentSummary()).append(" ");
        }
        
        // Feedback summary (most reliable indicator of language)
        if (data.getFeedbackSummary() != null && !data.getFeedbackSummary().isBlank()) {
            textToAnalyze.append(data.getFeedbackSummary()).append(" ");
        }
        
        // Weak and strong areas
        if (data.getCommonWeakAreas() != null) {
            data.getCommonWeakAreas().forEach(area -> {
                if (area != null && !area.isBlank()) {
                    textToAnalyze.append(area).append(" ");
                }
            });
        }
        if (data.getCommonStrongAreas() != null) {
            data.getCommonStrongAreas().forEach(area -> {
                if (area != null && !area.isBlank()) {
                    textToAnalyze.append(area).append(" ");
                }
            });
        }
        
        // Assessment details (content from individual assessments)
        if (data.getAssessments() != null) {
            data.getAssessments().forEach(assessment -> {
                if (assessment.getContent() != null && !assessment.getContent().isBlank()) {
                    textToAnalyze.append(assessment.getContent()).append(" ");
                }
            });
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
     * Improved detection that considers more Spanish-specific patterns.
     */
    private String detectLanguageFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "English";
        }
        
        String combinedText = text.toLowerCase().trim();
        
        // Spanish-specific characters (strong indicators)
        String[] spanishChars = {"á", "é", "í", "ó", "ú", "ñ", "ü"};
        int spanishCharCount = 0;
        for (String ch : spanishChars) {
            if (combinedText.contains(ch)) {
                spanishCharCount++;
            }
        }
        
        // If we find Spanish characters, it's very likely Spanish
        if (spanishCharCount >= 1) {
            return "Spanish";
        }
        
        // Spanish common words and phrases
        String[] spanishIndicators = {
            " el ", " la ", " los ", " las ", " de ", " del ", " que ", " y ", " en ", " con ", " por ", " para ",
            " evaluación ", " evaluaciones ", " retroalimentación ", " contenido ", " materia ", " asignatura ",
            " examen ", " exámenes ", " prueba ", " pruebas ", " tarea ", " tareas ", 
            " calificación ", " calificaciones ", " estudiante ", " estudiantes ", " profesor ", " profesores ",
            " estructuras ", " datos ", " básicas ", " mejorar ", " entender ", " practicar ", " revisar ",
            " excelente ", " trabajo ", " considerar ", " necesita ", " debe ", " puede ", " debería "
        };
        
        int spanishCount = 0;
        for (String indicator : spanishIndicators) {
            if (combinedText.contains(indicator)) {
                spanishCount++;
            }
        }
        
        // If we find multiple Spanish indicators, it's Spanish
        if (spanishCount >= 2) {
            return "Spanish";
        }
        
        // Common Spanish educational words
        String[] commonSpanishWords = {
            "examen", "prueba", "tarea", "calificación", "estudiante", "profesor",
            "evaluación", "retroalimentación", "contenido", "materia", "asignatura",
            "estructuras", "datos", "básicas", "inteligencia", "artificial", "hashes"
        };
        
        for (String word : commonSpanishWords) {
            if (combinedText.contains(word)) {
                return "Spanish";
            }
        }
        
        return "English";
    }

    /**
     * Builds system message for teacher recommendations with enhanced analytical framework.
     * Tone: Conversational, human, colleague-to-colleague, not overly technical.
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
        
        return "You are an experienced educational colleague providing thoughtful, practical advice to a fellow teacher. " +
                "Write in a professional yet approachable tone - supportive and genuinely helpful, but not overly casual or familiar. " +
                "Your goal is to help them understand what's happening in their class and what they can do about it.\n\n" +
                
                "YOUR APPROACH:\n" +
                "1. OBSERVE: Look at the data holistically - what patterns do you see? What stands out?\n" +
                "2. DIAGNOSE: Think about WHY students might be struggling or excelling. Consider:\n" +
                "   - Conceptual gaps (they don't understand the concept)\n" +
                "   - Skill gaps (they understand but can't apply it)\n" +
                "   - Engagement issues (they're not participating or submitting work)\n" +
                "3. PRESCRIBE: Suggest specific, practical teaching strategies that make sense for this situation.\n\n" +
                
                "TONE AND STYLE:\n" +
                "- Write in a professional, collegial tone - respectful and supportive\n" +
                "- Be direct and clear, avoiding overly casual greetings or familiar language\n" +
                "- Use natural language, not overly technical jargon\n" +
                "- Show empathy - teaching is challenging\n" +
                "- Be specific but not robotic - explain the \"why\" behind recommendations\n" +
                "- Length: Up to 880 words - be thorough but concise\n" +
                "- IMPORTANT: Use specific words, phrases, and terminology from the feedbacks and assessment descriptions provided\n" +
                "- IMPORTANT: Reference the specific class name, group, and assessment names when discussing data\n\n" +
                
                "CRITICAL REQUIREMENTS:\n" +
                "- TEACHING-FOCUSED: All recommendations are for the TEACHER about how to teach, intervene, or support students.\n" +
                "- DATA-DRIVEN: Reference specific numbers and metrics from the data provided.\n" +
                "- CORRELATE METRICS: Connect the statistics (Standard Deviation, Participation Rate, Trends) " +
                "to what they mean for teaching strategies.\n" +
                "- INCLUDE DATA SUMMARY: At the end, include a clear section showing the key data points you analyzed.\n" +
                "- ANTI-HALLUCINATION: NEVER describe feedback as 'recurring' or a 'pattern' unless it appears multiple times. " +
                "NEVER report percentages over 100% for student counts. NEVER invent statistics not in the data.\n" +
                "- VERIFICATION: Before claiming a pattern, verify it appears multiple times in the feedback summary.\n" +
                languageInstruction +
                
                "OUTPUT STRUCTURE:\n" +
                "1. Opening (1-2 sentences acknowledging the class situation in a professional, supportive way - avoid casual greetings)\n" +
                "2. Main Analysis (2-3 paragraphs explaining what you see in the data and why it matters)\n" +
                "3. Recommendations (2-3 specific, practical teaching strategies with brief explanations)\n" +
                "4. Data Summary (A clear section titled \"Datos Analizados\" or \"Data Analyzed\" that lists:\n" +
                "   - Key statistics used (average, median, standard deviation, etc.) with brief explanations in parentheses\n" +
                "   - Assessment details (names, types, averages) with brief context\n" +
                "   - Student performance segments (at risk, high performers) with brief definitions\n" +
                "   - Trends and patterns identified with brief explanations)\n" +
                "   IMPORTANT: For each data point, add a brief explanation in parentheses (e.g., \"Promedio general: 53.3% (promedio de todos los estudiantes)\")\n\n" +
                
                "AVOID:\n" +
                "- Overly technical or robotic language\n" +
                "- Generic advice without context\n" +
                "- Student-facing recommendations\n" +
                "- Ignoring the data you were given\n" +
                "- Being too brief - this is a thoughtful analysis, not a quick tip\n\n" +
                
                "Remember: You're helping a colleague understand their students better. Be thorough, be human, and be helpful.";
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
            prompt.append("→ IMPORTANT: Use the EXACT terminology and phrases from this content when discussing topics in your analysis.\n");
            prompt.append(data.getAssessmentContentSummary()).append("\n");
        }
        
        if (data.getFeedbackSummary() != null && !data.getFeedbackSummary().isBlank()) {
            prompt.append("\nFEEDBACK SUMMARY:\n");
            prompt.append("→ CRITICAL: This is a concatenated summary of ALL feedbacks. " +
                        "Before describing something as a 'pattern' or 'recurring', you MUST verify it appears multiple times. " +
                        "If a phrase appears only once in this summary, describe it as a single occurrence, NOT as a pattern. " +
                        "Quote or reference SPECIFIC phrases from these feedbacks when discussing them. " +
                        "Use the exact words the teacher used. DO NOT invent patterns that don't exist.\n");
            prompt.append(data.getFeedbackSummary()).append("\n");
        }
        
        // Limit assessments to Top 5 most recent or worst performing to avoid data overload
        if (data.getAssessments() != null && !data.getAssessments().isEmpty()) {
            List<ClassPerformanceData.AssessmentSummary> assessmentsToShow = data.getAssessments();
            if (assessmentsToShow.size() > 5) {
                // Show worst 3 and best 2, or last 5 if trend is available
                assessmentsToShow = assessmentsToShow.stream()
                        .sorted((a1, a2) -> {
                            if (a1.getAveragePercentage() == null || a2.getAveragePercentage() == null) return 0;
                            return a1.getAveragePercentage().compareTo(a2.getAveragePercentage());
                        })
                        .limit(5)
                        .collect(java.util.stream.Collectors.toList());
            }
            
            prompt.append("\nKEY ASSESSMENT SUMMARY (Top 5 by performance):\n");
            prompt.append("→ IMPORTANT: Use the EXACT assessment names and content descriptions below when discussing performance in your analysis.\n\n");
            assessmentsToShow.forEach(assessment -> {
                prompt.append("Assessment: ");
                if (assessment.getAssessmentName() != null && !assessment.getAssessmentName().isBlank()) {
                    prompt.append("\"").append(assessment.getAssessmentName()).append("\"");
                } else {
                    prompt.append("(No name specified)");
                }
                if (assessment.getAssessmentKind() != null && !assessment.getAssessmentKind().isBlank()) {
                    prompt.append(" (Type: ").append(assessment.getAssessmentKind()).append(")");
                }
                prompt.append("\n");
                // Show both average score and percentage for clarity
                if (assessment.getAverageScore() != null) {
                    prompt.append("  - Average Score: ").append(assessment.getAverageScore().setScale(1, java.math.RoundingMode.HALF_UP));
                }
                if (assessment.getAveragePercentage() != null) {
                    prompt.append(" | Average Percentage: ").append(assessment.getAveragePercentage().setScale(1, java.math.RoundingMode.HALF_UP)).append("%");
                } else {
                    prompt.append(" | Average Percentage: N/A (calculation error)");
                }
                if (assessment.getStudentCount() != null) {
                    prompt.append(" | Students Evaluated: ").append(assessment.getStudentCount());
                }
                if (assessment.getContent() != null && !assessment.getContent().isBlank()) {
                    prompt.append("\n  - Content/Topics Covered: \"").append(assessment.getContent()).append("\"");
                    prompt.append(" → Use this exact content description when discussing this assessment");
                }
                prompt.append("\n\n");
            });
            if (data.getAssessments().size() > 5) {
                prompt.append("(Total assessments analyzed: ").append(data.getAssessments().size()).append(")\n");
            }
        }
        
        // Add comprehensive statistics with contextual interpretation
        prompt.append("\nDATA SNAPSHOT:\n");
        if (data.getAveragePercentage() != null) {
            prompt.append("- Average Class Performance: ").append(data.getAveragePercentage().setScale(1, java.math.RoundingMode.HALF_UP)).append("%\n");
        }
        if (data.getMedianPercentage() != null) {
            prompt.append("- Median: ").append(data.getMedianPercentage().setScale(1, java.math.RoundingMode.HALF_UP)).append("%\n");
        }
        if (data.getStandardDeviation() != null) {
            String variabilityNote = data.getStandardDeviation().compareTo(new BigDecimal("15")) > 0
                ? " (HIGH VARIABILITY - implies a divided class; consider differentiation strategies)"
                : " (LOW VARIABILITY - implies uniform understanding; consider whole-class re-teaching)";
            prompt.append("- Variability (Std Dev): ").append(data.getStandardDeviation().setScale(1, java.math.RoundingMode.HALF_UP))
                  .append("%").append(variabilityNote).append("\n");
        }
        if (data.getParticipationRate() != null) {
            String participationNote = data.getParticipationRate().compareTo(new BigDecimal("80")) < 0
                ? " (LOW - focus on engagement and intervention)"
                : " (GOOD - focus on content improvement)";
            prompt.append("- Participation Rate: ").append(data.getParticipationRate().setScale(1, java.math.RoundingMode.HALF_UP))
                  .append("%").append(participationNote).append("\n");
        }
        
        prompt.append("\nPERFORMANCE SEGMENTS:\n");
        prompt.append("→ IMPORTANT: These counts represent UNIQUE STUDENTS, not individual grades. " +
                     "A student is counted as 'at risk' if they have at least one grade below 60%. " +
                     "The percentage is calculated as: (unique students at risk / total students) × 100. " +
                     "This percentage should NEVER exceed 100%.\n");
        if (data.getStudentsAtRisk() != null && data.getAtRiskPercentage() != null) {
            prompt.append("- At Risk (<60%): ").append(data.getStudentsAtRisk())
                  .append(" unique students (").append(data.getAtRiskPercentage().setScale(1, java.math.RoundingMode.HALF_UP)).append("% of total students)\n");
        }
        if (data.getStudentsPerformingWell() != null && data.getTotalStudents() != null) {
            BigDecimal wellPercentage = new BigDecimal(data.getStudentsPerformingWell())
                    .divide(new BigDecimal(data.getTotalStudents()), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(1, java.math.RoundingMode.HALF_UP);
            prompt.append("- High Performers (>=85%): ").append(data.getStudentsPerformingWell())
                  .append(" students (").append(wellPercentage).append("%)\n");
        }
        if (data.getClassPerformanceTrend() != null && !data.getClassPerformanceTrend().isBlank()) {
            String trendNote = data.getClassPerformanceTrend().equals("Declining")
                ? " (CRITICAL - identify if recent topics are harder or if student fatigue is a factor)"
                : data.getClassPerformanceTrend().equals("Improving")
                    ? " (POSITIVE - maintain current teaching strategies)"
                    : "";
            prompt.append("- Trend: ").append(data.getClassPerformanceTrend()).append(trendNote).append("\n");
        }
        
        prompt.append("\nCONTENT ANALYSIS:\n");
        if (data.getCommonStrongAreas() != null && !data.getCommonStrongAreas().isEmpty()) {
            prompt.append("- Strongest Concept: ").append(String.join(", ", data.getCommonStrongAreas()));
            if (data.getBestAssessment() != null) {
                // Find the best assessment's percentage for context
                String bestAssessmentPct = "N/A";
                if (data.getAssessments() != null) {
                    bestAssessmentPct = data.getAssessments().stream()
                        .filter(a -> data.getBestAssessment().equals(a.getAssessmentName()))
                        .findFirst()
                        .map(a -> a.getAveragePercentage() != null ? 
                            a.getAveragePercentage().setScale(1, java.math.RoundingMode.HALF_UP) + "%" : "N/A")
                        .orElse("N/A");
                }
                prompt.append(" (Best Assessment: ").append(data.getBestAssessment())
                      .append(" - Average: ").append(bestAssessmentPct);
                if (data.getBestAssessmentType() != null) {
                    prompt.append(", Type: ").append(data.getBestAssessmentType());
                }
                prompt.append(")");
            }
            prompt.append("\n");
        }
        if (data.getCommonWeakAreas() != null && !data.getCommonWeakAreas().isEmpty()) {
            prompt.append("- Weakest Concept: ").append(String.join(", ", data.getCommonWeakAreas()));
            if (data.getWorstAssessment() != null) {
                // Find the worst assessment's percentage for context
                String worstAssessmentPct = "N/A";
                if (data.getAssessments() != null) {
                    worstAssessmentPct = data.getAssessments().stream()
                        .filter(a -> data.getWorstAssessment().equals(a.getAssessmentName()))
                        .findFirst()
                        .map(a -> a.getAveragePercentage() != null ? 
                            a.getAveragePercentage().setScale(1, java.math.RoundingMode.HALF_UP) + "%" : "N/A")
                        .orElse("N/A");
                }
                prompt.append(" (Hardest Assessment: ").append(data.getWorstAssessment())
                      .append(" - Average: ").append(worstAssessmentPct);
                if (data.getWorstAssessmentType() != null) {
                    prompt.append(", Type: ").append(data.getWorstAssessmentType());
                }
                prompt.append(")");
            }
            prompt.append("\n");
        }
        if (data.getFeedbackSummary() != null && !data.getFeedbackSummary().isBlank()) {
            prompt.append("- Key Feedback Pattern: \"").append(data.getFeedbackSummary().length() > 100 
                ? data.getFeedbackSummary().substring(0, 100) + "..." 
                : data.getFeedbackSummary()).append("\"\n");
        }
        
        prompt.append("\n==================================================\n");
        prompt.append("TASK: Write a thoughtful, professional analysis for your colleague (the teacher). " +
                     "Be comprehensive but concise (up to 880 words), supportive, and genuinely helpful. " +
                     "Avoid overly casual greetings - be direct and professional.\n\n");
        
        prompt.append("CRITICAL REQUIREMENTS FOR YOUR ANALYSIS:\n");
        prompt.append("1. USE SPECIFIC TERMINOLOGY: When discussing feedback or assessment content, use the EXACT words and phrases " +
                     "from the feedbacks and assessment descriptions provided above. Don't paraphrase - quote or reference the specific " +
                     "language used by the teacher in feedbacks.\n");
        prompt.append("2. REFERENCE CLASS AND ASSESSMENT NAMES: Always mention the specific class name (\"").append(data.getClassName() != null ? data.getClassName() : "this class").append("\") " +
                     "and assessment names when discussing performance. For example: \"In the ").append(data.getClassName() != null ? data.getClassName() : "class").append(", " +
                     "students performed well on [Assessment Name] but struggled with [Assessment Name].\"\n");
        prompt.append("3. INCLUDE ASSESSMENT CONTENT: When discussing weak or strong areas, reference the specific assessment content/topics " +
                     "mentioned in the assessment descriptions. Use the exact terminology from the assessment content summaries.\n");
        prompt.append("4. ACCURACY IN FEEDBACK PATTERNS: CRITICAL - Only describe feedback as \"recurring\" or a \"pattern\" if it appears in MULTIPLE feedbacks. " +
                     "If a feedback phrase appears only once, say \"one feedback mentions...\" or \"a feedback noted...\" - NEVER say \"recurring phrases\" or \"patterns\" " +
                     "unless you can verify it appears multiple times. Check the feedback summary carefully before making claims about patterns.\n");
        prompt.append("5. VERIFY ALL STATISTICS: Use ONLY the exact numbers provided in the data. Do not calculate or estimate percentages yourself. " +
                     "If the data shows \"4 students (X%)\", use those exact numbers. Never report percentages over 100% for student counts - this indicates an error.\n");
        prompt.append("6. ASSESSMENT AVERAGE CLARITY: When discussing assessment averages, understand that: " +
                     "The average percentage for an assessment is calculated as the average of individual student percentages for that assessment. " +
                     "If an assessment shows 0.0%, it means the average of all student percentages for that assessment was 0.0% (all students scored 0%). " +
                     "This is a valid calculation even if there are many assessments - each assessment's average is independent.\n\n");
        
        prompt.append("ANALYSIS GUIDELINES:\n");
        prompt.append("Think about what these numbers mean for teaching:\n");
        if (data.getStandardDeviation() != null) {
            prompt.append("- High Standard Deviation (>15%) suggests a divided class - some students are doing well while others struggle. " +
                        "This might call for differentiation strategies.\n");
            prompt.append("- Low Standard Deviation (<10%) with low average suggests most students are struggling similarly - " +
                        "this might indicate a need for whole-class re-teaching with a different approach.\n");
        }
        if (data.getParticipationRate() != null) {
            prompt.append("- Low Participation Rate (<80%) suggests engagement issues - students might not be submitting work or attending. " +
                        "Focus on engagement strategies, not just content review.\n");
        }
        if (data.getClassPerformanceTrend() != null && data.getClassPerformanceTrend().equals("Declining")) {
            prompt.append("- Declining Trend could mean recent topics are harder, or students are experiencing fatigue. " +
                        "Consider the timing and difficulty of recent assessments.\n");
        }
        prompt.append("- Look at which assessment types students struggle with (Exams vs Quizzes) - this tells you if it's a " +
                     "conceptual issue (they don't understand) or a procedural issue (they understand but can't apply it under pressure).\n\n");
        
        prompt.append("OUTPUT STRUCTURE:\n\n");
        
        // Use language-appropriate section titles
        if (detectedLanguage.equals("Spanish")) {
            prompt.append("1. **Apertura** (1-2 oraciones profesionales reconociendo la situación de la clase - evita saludos casuales como \"Hola, amigo\")\n\n");
            prompt.append("2. **Análisis Principal** (1-2 párrafos CONCISOS explicando qué ves en los datos y por qué importa. " +
                        "Sé profesional y claro, como si estuvieras hablando con un colega. Explica SOLO los patrones que realmente observas en los datos proporcionados. " +
                        "NO inventes patrones que no estén explícitamente en los datos. Sé breve y directo.)\n\n");
            prompt.append("3. **Recomendaciones** (2-3 estrategias de enseñanza específicas y prácticas, con una breve explicación " +
                        "del por qué cada una podría ayudar. Sé específico y directo.)\n\n");
            prompt.append("4. **Datos Analizados** (Una sección clara y detallada que muestre al profesor exactamente qué datos usaste para tu análisis. " +
                        "Esta sección debe ser COMPLETA y ESPECÍFICA. Para CADA dato, agrega una explicación breve entre paréntesis. Incluye:\n");
            prompt.append("   - Clase analizada: nombre completo de la clase y grupo\n");
            prompt.append("   - Estadísticas clave: promedio general (promedio de todos los estudiantes), mediana (valor central), " +
                        "desviación estándar (variabilidad en el rendimiento), rango (min-max) (diferencia entre mejor y peor rendimiento)\n");
            prompt.append("   - Detalles de CADA evaluación: nombre completo, tipo (EXAM/QUIZ/etc.), promedio de porcentaje (promedio de los porcentajes individuales de todos los estudiantes que tomaron esta evaluación), " +
                        "promedio de puntuación (promedio de puntos obtenidos), número de estudiantes evaluados, contenido/temas cubiertos\n");
            prompt.append("   - Segmentos de rendimiento: estudiantes en riesgo (<60%) (estudiantes con bajo rendimiento), " +
                        "estudiantes destacados (>=85%) (estudiantes con alto rendimiento), porcentajes exactos\n");
            prompt.append("   - Tendencias identificadas: mejora/declive/estable (dirección del rendimiento a lo largo del tiempo), con contexto\n");
            prompt.append("   - Áreas débiles y fuertes: lista completa con los nombres exactos de los temas/contenidos\n");
            prompt.append("   - Tasa de participación: porcentaje exacto (porcentaje de estudiantes que completaron evaluaciones)\n");
            prompt.append("   - Patrones de retroalimentación: SOLO menciona frases o temas que aparecen REALMENTE en múltiples feedbacks. " +
                        "Si un feedback aparece solo una vez, NO lo describas como \"recurrente\" o \"patrón\". " +
                        "Sé preciso: si solo hay un feedback con una frase, di \"un feedback menciona...\" no \"frases recurrentes como...\"\n");
            prompt.append("   - Mejor y peor evaluación: nombres completos, tipos, y promedios (evaluación con mejor/peor rendimiento promedio)\n");
            prompt.append("   - Cualquier otro dato relevante del análisis)\n");
            prompt.append("   IMPORTANTE: Usa los nombres exactos de las evaluaciones, contenidos, y frases de feedback que se proporcionaron. " +
                        "Agrega explicaciones breves entre paréntesis para cada métrica o dato.\n\n");
            prompt.append("CRITICAL: Generate your ENTIRE response in Spanish (Español), including all section titles and content. " +
                        "Write in a warm, conversational, colleague-to-colleague tone.\n");
        } else {
            prompt.append("1. **Opening** (1-2 professional sentences acknowledging the class situation - avoid casual greetings)\n\n");
            prompt.append("2. **Main Analysis** (1-2 CONCISE paragraphs explaining what you see in the data and why it matters. " +
                        "Be professional and clear, like you're talking to a colleague. Explain ONLY the patterns that are actually present in the provided data. " +
                        "DO NOT invent patterns that are not explicitly in the data. Be brief and direct.)\n\n");
            prompt.append("3. **Recommendations** (2-3 specific, practical teaching strategies with brief explanations " +
                        "of why each might help. Be specific and direct.)\n\n");
            prompt.append("4. **Data Analyzed** (A clear and detailed section showing the teacher exactly what data you used for your analysis. " +
                        "This section must be COMPLETE and SPECIFIC. For EACH data point, add a brief explanation in parentheses. Include:\n");
            prompt.append("   - Class analyzed: full class name and group\n");
            prompt.append("   - Key statistics: overall average (average of all students), median (middle value), " +
                        "standard deviation (variability in performance), range (min-max) (difference between best and worst performance)\n");
            prompt.append("   - Details of EACH assessment: full name, type (EXAM/QUIZ/etc.), average percentage (average of individual percentages from all students who took this assessment), " +
                        "average score (average points obtained), number of students evaluated, content/topics covered\n");
            prompt.append("   - Performance segments: students at risk (<60%) (students with low performance), " +
                        "high performers (>=85%) (students with high performance), exact percentages\n");
            prompt.append("   - Trends identified: improving/declining/stable (direction of performance over time), with context\n");
            prompt.append("   - Weak and strong areas: complete list with exact names of topics/content\n");
            prompt.append("   - Participation rate: exact percentage (percentage of students who completed assessments)\n");
            prompt.append("   - Feedback patterns: ONLY mention phrases or themes that ACTUALLY appear in multiple feedbacks. " +
                        "If a feedback appears only once, DO NOT describe it as \"recurring\" or a \"pattern\". " +
                        "Be precise: if there's only one feedback with a phrase, say \"one feedback mentions...\" not \"recurring phrases like...\"\n");
            prompt.append("   - Best and worst assessment: full names, types, and averages (assessment with best/worst average performance)\n");
            prompt.append("   - Any other relevant data from the analysis)\n");
            prompt.append("   IMPORTANT: Use the exact names of assessments, content, and feedback phrases that were provided. " +
                        "Add brief explanations in parentheses for each metric or data point.\n\n");
            prompt.append("LANGUAGE: Generate your response in English. Write in a warm, conversational, colleague-to-colleague tone.\n");
        }
        
        prompt.append("\nRemember: This is a thoughtful, comprehensive analysis for a colleague. Be thorough but concise (up to 880 words), be professional, and be helpful. " +
                     "The teacher should feel supported and understand both what's happening and what they can do about it.\n");
        prompt.append("Most importantly: Use the SPECIFIC words, assessment names, class names, and feedback phrases from the data provided. " +
                     "This makes your analysis authentic and shows you're truly analyzing their specific situation, not giving generic advice.\n");
        prompt.append("\nCRITICAL ANTI-HALLUCINATION RULES:\n");
        prompt.append("- NEVER describe a feedback as 'recurring' or a 'pattern' unless you can verify it appears multiple times in the feedback summary\n");
        prompt.append("- NEVER report percentages over 100% for student counts - if you see this, it's an error in the data\n");
        prompt.append("- NEVER invent statistics or calculations not explicitly provided\n");
        prompt.append("- ALWAYS verify feedback patterns by checking if phrases appear multiple times before calling them 'recurring'\n");
        prompt.append("- If you're unsure about a pattern, say 'some feedbacks mention...' or 'one feedback noted...' instead of 'recurring phrases'\n");
        prompt.append("- When discussing assessment averages, explain that 0.0% means the average of all student percentages for that assessment was 0.0%\n\n");
        prompt.append("Generate the comprehensive recommendation now:");
        
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
        prompt.append("- Student Name: ").append(data.getStudentName() != null ? data.getStudentName() : "Not specified").append("\n");
        prompt.append("  → IMPORTANT: Always use this student's name when discussing their performance\n");
        prompt.append("- Subject: ").append(data.getSubjectName() != null ? data.getSubjectName() : "Not specified").append("\n");
        prompt.append("- Class Name (Subject + Group): ").append(data.getClassName() != null ? data.getClassName() : "Not specified").append("\n");
        prompt.append("  → IMPORTANT: Always reference this class name when discussing assessments\n");
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
            prompt.append("→ IMPORTANT: Quote or reference SPECIFIC phrases from these feedbacks when discussing patterns. " +
                        "Use the exact words the teacher used in feedbacks.\n");
            prompt.append(data.getFeedbackSummary()).append("\n");
        }
        
        // Show chronological assessment history (last 5) with clear structure
        if (data.getAssessments() != null && !data.getAssessments().isEmpty()) {
            List<StudentPerformanceData.StudentAssessmentSummary> assessmentsToShow = data.getAssessments();
            if (assessmentsToShow.size() > 5) {
                assessmentsToShow = assessmentsToShow.subList(0, Math.min(5, assessmentsToShow.size()));
            }
            
            prompt.append("\nCHRONOLOGICAL ASSESSMENT HISTORY (Last ").append(assessmentsToShow.size()).append(", most recent first):\n");
            prompt.append("→ IMPORTANT: Use the EXACT assessment names, content descriptions, and feedback quotes below in your analysis.\n\n");
            int index = 1;
            for (StudentPerformanceData.StudentAssessmentSummary assessment : assessmentsToShow) {
                prompt.append(index).append(". Assessment: ");
                if (assessment.getAssessmentName() != null && !assessment.getAssessmentName().isBlank()) {
                    prompt.append("\"").append(assessment.getAssessmentName()).append("\"");
                } else {
                    prompt.append("(No name specified)");
                }
                if (assessment.getAssessmentKind() != null && !assessment.getAssessmentKind().isBlank()) {
                    prompt.append(" (Type: ").append(assessment.getAssessmentKind()).append(")");
                }
                prompt.append("\n");
                if (assessment.getPercentage() != null) {
                    prompt.append("   - Performance: ").append(assessment.getPercentage().setScale(1, java.math.RoundingMode.HALF_UP)).append("%");
                }
                if (assessment.getScore() != null && assessment.getMaxScore() != null) {
                    prompt.append(" | Score: ").append(assessment.getScore().setScale(1, java.math.RoundingMode.HALF_UP))
                          .append("/").append(assessment.getMaxScore().setScale(1, java.math.RoundingMode.HALF_UP));
                }
                if (assessment.getContent() != null && !assessment.getContent().isBlank()) {
                    prompt.append("\n   - Content/Topics: \"").append(assessment.getContent()).append("\"");
                    prompt.append(" → Use this exact content description when discussing this assessment");
                }
                if (assessment.getFeedback() != null && !assessment.getFeedback().isBlank()) {
                    prompt.append("\n   - Teacher Feedback: \"").append(assessment.getFeedback()).append("\"");
                    prompt.append(" → Quote or reference this exact feedback when discussing patterns");
                }
                prompt.append("\n\n");
                index++;
            }
            if (data.getAssessments().size() > 5) {
                prompt.append("(Total assessments: ").append(data.getAssessments().size()).append(")\n");
            }
            
            // Add consistency analysis instruction
            prompt.append("\nCONSISTENCY ANALYSIS:\n");
            prompt.append("Look for patterns: Does the student fail specifically on EXAMS but pass QUIZZES? " +
                         "If so, suggest test-taking strategies or anxiety support. " +
                         "Does the student perform well on certain topics but poorly on others? " +
                         "Identify the specific content gaps.\n");
        }
        
        // Add summary statistics
        if (data.getAverageScore() != null && data.getAveragePercentage() != null) {
            prompt.append("\nRESUMEN ESTADÍSTICO DEL ESTUDIANTE:\n");
            prompt.append("- Promedio general: ").append(data.getAveragePercentage().setScale(1, java.math.RoundingMode.HALF_UP)).append("%\n");
            prompt.append("- Puntuación promedio: ").append(data.getAverageScore().setScale(1, java.math.RoundingMode.HALF_UP)).append("\n");
            prompt.append("- Total de evaluaciones: ").append(data.getTotalAssessments()).append("\n");
            if (data.getPerformanceTrend() != null && !data.getPerformanceTrend().isBlank()) {
                prompt.append("- Tendencia de rendimiento: ").append(data.getPerformanceTrend()).append("\n");
            }
        }
        
        prompt.append("\n==================================================\n");
        prompt.append("TASK: Write a thoughtful, professional analysis for your colleague (the teacher) about this specific student. " +
                     "Be comprehensive but concise (up to 880 words), supportive, and genuinely helpful. " +
                     "Avoid overly casual greetings - be direct and professional.\n\n");
        
        prompt.append("CRITICAL REQUIREMENTS FOR YOUR ANALYSIS:\n");
        prompt.append("1. USE SPECIFIC TERMINOLOGY: When discussing feedback or assessment content, use the EXACT words and phrases " +
                     "from the feedbacks and assessment descriptions provided above. Quote specific feedback when relevant.\n");
        prompt.append("2. REFERENCE CLASS AND ASSESSMENT NAMES: Always mention the specific class name (\"").append(data.getClassName() != null ? data.getClassName() : "this class").append("\") " +
                     "and assessment names when discussing this student's performance. For example: \"In ").append(data.getClassName() != null ? data.getClassName() : "the class").append(", " +
                     "[Student Name] performed well on [Assessment Name] but struggled with [Assessment Name].\"\n");
        prompt.append("3. INCLUDE ASSESSMENT CONTENT: When discussing weak or strong areas, reference the specific assessment content/topics " +
                     "mentioned. Use the exact terminology from the assessment content.\n");
        prompt.append("4. QUOTE FEEDBACK: When discussing patterns, quote or reference specific feedback this student received. " +
                     "This shows you're analyzing the actual teacher feedback, not just numbers.\n\n");
        
        prompt.append("ANALYSIS GUIDELINES:\n");
        prompt.append("Think about what this student's pattern tells you:\n");
        prompt.append("- If the student fails EXAMS but passes QUIZZES, this suggests test-taking anxiety or time management issues, " +
                     "not necessarily content gaps. They might understand the material but struggle under pressure.\n");
        prompt.append("- If Performance Trend is \"Declining\", consider: Are recent topics harder? Is there a pattern of disengagement? " +
                     "Has something changed in the student's circumstances?\n");
        prompt.append("- If Weak Areas are consistent across multiple assessments, this indicates a conceptual gap that needs " +
                     "targeted re-teaching, not just more practice.\n");
        prompt.append("- If Strong Areas exist, leverage them: This student could help peers or take on advanced challenges to stay engaged.\n\n");
        
        prompt.append("OUTPUT STRUCTURE:\n\n");
        
        // Use language-appropriate section titles
        if (detectedLanguage.equals("Spanish")) {
            prompt.append("1. **Apertura** (1-2 oraciones profesionales reconociendo la situación del estudiante - evita saludos casuales)\n\n");
            prompt.append("2. **Análisis Principal** (2-3 párrafos explicando qué ves en los datos de este estudiante y por qué importa. " +
                        "Sé profesional y claro, como si estuvieras hablando con un colega. Explica los patrones que observas.)\n\n");
            prompt.append("3. **Recomendaciones** (2-3 estrategias de enseñanza específicas y prácticas para este estudiante, " +
                        "con una breve explicación del por qué cada una podría ayudar. Sé específico y directo.)\n\n");
            prompt.append("4. **Datos Analizados** (Una sección clara y detallada que muestre al profesor exactamente qué datos usaste para tu análisis. " +
                        "Esta sección debe ser COMPLETA y ESPECÍFICA. Para CADA dato, agrega una explicación breve entre paréntesis. Incluye:\n");
            prompt.append("   - Estudiante analizado: nombre completo\n");
            prompt.append("   - Clase: nombre completo de la clase y grupo\n");
            prompt.append("   - Estadísticas del estudiante: promedio general (promedio de todas las calificaciones), " +
                        "puntuación promedio (puntos promedio), tendencia de rendimiento (dirección del rendimiento a lo largo del tiempo)\n");
            prompt.append("   - Detalles de CADA evaluación: nombre completo, tipo (EXAM/QUIZ/etc.), puntuación exacta (score/maxScore) (puntos obtenidos/máximo posible), " +
                        "porcentaje (calificación en porcentaje), contenido/temas cubiertos, retroalimentación recibida (citar frases específicas)\n");
            prompt.append("   - Áreas débiles y fuertes: lista completa con los nombres exactos de los temas/contenidos\n");
            prompt.append("   - Patrones de retroalimentación: menciones específicas de frases o temas recurrentes en los feedbacks recibidos\n");
            prompt.append("   - Total de evaluaciones analizadas (número de evaluaciones consideradas)\n");
            prompt.append("   - Cualquier otro dato relevante del análisis)\n");
            prompt.append("   IMPORTANTE: Usa los nombres exactos de las evaluaciones, contenidos, y frases de feedback que se proporcionaron. " +
                        "Agrega explicaciones breves entre paréntesis para cada métrica o dato.\n\n");
            prompt.append("CRITICAL: Generate your ENTIRE response in Spanish (Español), including all section titles and content. " +
                        "Write in a professional, collegial tone.\n");
        } else {
            prompt.append("1. **Opening** (1-2 professional sentences acknowledging this student's situation - avoid casual greetings)\n\n");
            prompt.append("2. **Main Analysis** (2-3 paragraphs explaining what you see in this student's data and why it matters. " +
                        "Be professional and clear, like you're talking to a colleague. Explain the patterns you observe.)\n\n");
            prompt.append("3. **Recommendations** (2-3 specific, practical teaching strategies for this student with brief explanations " +
                        "of why each might help. Be specific and direct.)\n\n");
            prompt.append("4. **Data Analyzed** (A clear and detailed section showing the teacher exactly what data you used for your analysis. " +
                        "This section must be COMPLETE and SPECIFIC. For EACH data point, add a brief explanation in parentheses. Include:\n");
            prompt.append("   - Student analyzed: full name\n");
            prompt.append("   - Class: full class name and group\n");
            prompt.append("   - Student statistics: overall average (average of all grades), average score (average points), " +
                        "performance trend (direction of performance over time)\n");
            prompt.append("   - Details of EACH assessment: full name, type (EXAM/QUIZ/etc.), exact score (score/maxScore) (points obtained/maximum possible), " +
                        "percentage (grade as percentage), content/topics covered, feedback received (quote specific phrases)\n");
            prompt.append("   - Weak and strong areas: complete list with exact names of topics/content\n");
            prompt.append("   - Feedback patterns: specific mentions of recurring phrases or themes in received feedbacks\n");
            prompt.append("   - Total assessments analyzed (number of assessments considered)\n");
            prompt.append("   - Any other relevant data from the analysis)\n");
            prompt.append("   IMPORTANT: Use the exact names of assessments, content, and feedback phrases that were provided. " +
                        "Add brief explanations in parentheses for each metric or data point.\n\n");
            prompt.append("LANGUAGE: Generate your response in English. Write in a professional, collegial tone.\n");
        }
        
        prompt.append("\nRemember: This is a thoughtful, comprehensive analysis for a colleague about a specific student. " +
                     "Be thorough but concise (up to 880 words), be professional, and be helpful. " +
                     "The teacher should feel supported and understand both what's happening with this student and what they can do to help.\n");
        prompt.append("Most importantly: Use the SPECIFIC words, assessment names, class names, student name, and feedback phrases from the data provided. " +
                     "This makes your analysis authentic and shows you're truly analyzing this specific student's situation.\n\n");
        prompt.append("Generate the comprehensive recommendation now:");
        
        return prompt.toString();
    }
}

