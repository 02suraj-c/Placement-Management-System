package com.placement.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.placement.ai.dto.ResumeAnalysisRequest;
import com.placement.ai.dto.ResumeAnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Orchestrates resume analysis:
 *  1. Extract text via ResumeTextExtractor (PDFBox / POI)
 *  2. Build a structured AI prompt
 *  3. Call AIService → strict JSON response
 *  4. Map the AI JSON → ResumeAnalysisResponse DTO
 *
 * The AI is instructed to return a single JSON object whose schema
 * matches {@link AiResumeResult} exactly; any deviation is caught by
 * AIService's safe-parse layer and surfaces as a 422 to the client.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeAnalyzer {

    private final AIService            aiService;
    private final ResumeTextExtractor  textExtractor;

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Analyse a resume file and return an ATS report.
     *
     * @param file    PDF or DOCX upload (max 5 MB)
     * @param request optional metadata (role, company, extra keywords)
     * @return structured analysis response
     */
    public ResumeAnalysisResponse analyze(MultipartFile file,
                                          ResumeAnalysisRequest request) {

        // Step 1 — extract text
        ResumeTextExtractor.ExtractionResult extracted = textExtractor.extract(file);

        log.info("Resume analysis: format={}, chars={}, targetRole={}",
                extracted.format(), extracted.text().length(),
                request != null ? request.getTargetRole() : "none");

        // Step 2 — call AI
        String systemPrompt = buildSystemPrompt();
        String userPrompt   = buildUserPrompt(extracted.text(), request);

        AiResumeResult aiResult = aiService.call(systemPrompt, userPrompt,
                AiResumeResult.class);

        // Step 3 — map to response DTO
        return toResponse(aiResult, extracted);
    }

    // ── Prompt templates ───────────────────────────────────────────────────────

    private static String buildSystemPrompt() {
        return """
            You are an expert ATS (Applicant Tracking System) resume analyser \
            for a college placement portal.

            You MUST respond with a single, valid JSON object — no markdown, \
            no prose, no extra keys.

            Required JSON schema (all fields mandatory):
            {
              "atsScore": <integer 0-100>,
              "scoreBand": <"Excellent"|"Good"|"Needs Work"|"Poor">,
              "matchedKeywords": [<string>, ...],
              "missingKeywords": [<string>, ...],
              "sectionFeedback": {
                "Contact":        <string>,
                "Summary":        <string>,
                "Education":      <string>,
                "Experience":     <string>,
                "Skills":         <string>,
                "Projects":       <string>,
                "Certifications": <string>,
                "Achievements":   <string>
              },
              "sectionPresence": {
                "Contact":        <true|false>,
                "Summary":        <true|false>,
                "Education":      <true|false>,
                "Experience":     <true|false>,
                "Skills":         <true|false>,
                "Projects":       <true|false>,
                "Certifications": <true|false>,
                "Achievements":   <true|false>
              },
              "topImprovements": [<5 actionable strings, most impactful first>]
            }

            Scoring rules:
            - atsScore reflects keyword match %, formatting clarity, section \
              completeness, and quantified achievements.
            - scoreBand: Excellent=80-100, Good=60-79, Needs Work=40-59, Poor=0-39.
            - matchedKeywords: keywords from the target role that appear in the resume.
            - missingKeywords: important keywords that are absent.
            - sectionFeedback: 1-2 sentence actionable note per section; \
              if absent write "Section not found — add this section."
            - topImprovements: exactly 5 items, ordered by impact, \
              starting with an action verb.
            """;
    }

    private static String buildUserPrompt(String resumeText,
                                          ResumeAnalysisRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RESUME TEXT ===\n").append(resumeText).append("\n\n");

        sb.append("=== ANALYSIS PARAMETERS ===\n");
        if (request != null) {
            if (request.getTargetRole() != null && !request.getTargetRole().isBlank()) {
                sb.append("Target Role    : ").append(request.getTargetRole()).append('\n');
            }
            if (request.getTargetCompany() != null && !request.getTargetCompany().isBlank()) {
                sb.append("Target Company : ").append(request.getTargetCompany()).append('\n');
            }
            if (request.getJobDescriptionKeywords() != null
                    && !request.getJobDescriptionKeywords().isBlank()) {
                sb.append("JD Keywords    : ").append(request.getJobDescriptionKeywords())
                  .append('\n');
            }
        } else {
            sb.append("No specific role or company provided — perform general ATS analysis.\n");
        }

        sb.append("\nReturn only the JSON object. No explanation.");
        return sb.toString();
    }

    // ── Mapping ────────────────────────────────────────────────────────────────

    private static ResumeAnalysisResponse toResponse(
            AiResumeResult ai,
            ResumeTextExtractor.ExtractionResult extracted) {

        int wordCount = countWords(extracted.text());

        return ResumeAnalysisResponse.builder()
                .atsScore(clamp(ai.atsScore(), 0, 100))
                .scoreBand(scoreBandFor(clamp(ai.atsScore(), 0, 100)))
                .matchedKeywords(nullSafe(ai.matchedKeywords()))
                .missingKeywords(nullSafe(ai.missingKeywords()))
                .sectionFeedback(nullSafeMap(ai.sectionFeedback()))
                .sectionPresence(nullSafeMap(ai.sectionPresence()))
                .topImprovements(nullSafe(ai.topImprovements()))
                .fileFormat(extracted.format())
                .wordCount(wordCount)
                .readingTimeSeconds(wordCount / 3)   // ~180 wpm reading
                .warning(extracted.warning())
                .build();
    }

    // ── Internal AI response schema ────────────────────────────────────────────

    /**
     * Jackson-bound record that mirrors the AI JSON schema exactly.
     * Private — never exposed outside this class.
     */
    record AiResumeResult(
            @JsonProperty("atsScore")       int                 atsScore,
            @JsonProperty("scoreBand")      String              scoreBand,
            @JsonProperty("matchedKeywords") List<String>       matchedKeywords,
            @JsonProperty("missingKeywords") List<String>       missingKeywords,
            @JsonProperty("sectionFeedback") Map<String,String> sectionFeedback,
            @JsonProperty("sectionPresence") Map<String,Boolean> sectionPresence,
            @JsonProperty("topImprovements") List<String>       topImprovements
    ) {}

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static String scoreBandFor(int score) {
        if (score >= 80) return "Excellent";
        if (score >= 60) return "Good";
        if (score >= 40) return "Needs Work";
        return "Poor";
    }

    private static int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.trim().split("\\s+").length;
    }

    private static <T> List<T> nullSafe(List<T> list) {
        return list != null ? list : List.of();
    }

    private static <K, V> Map<K, V> nullSafeMap(Map<K, V> map) {
        return map != null ? map : Map.of();
    }
}
