package com.placement.ai.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Response DTO returned after AI resume analysis.
 * All fields are populated from the AI JSON response; never null (use empty
 * collections / 0 defaults) so the client always gets a consistent shape.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeAnalysisResponse {

    // ── ATS score ──────────────────────────────────────────────────────────────

    /**
     * Overall ATS compatibility score 0-100.
     */
    private int atsScore;

    /**
     * Score band label: "Excellent" (80-100) | "Good" (60-79) |
     * "Needs Work" (40-59) | "Poor" (<40).
     */
    private String scoreBand;

    // ── Keywords ───────────────────────────────────────────────────────────────

    /** Keywords from the JD / role that ARE present in the resume. */
    @Builder.Default
    private List<String> matchedKeywords = List.of();

    /** Keywords from the JD / role that are MISSING from the resume. */
    @Builder.Default
    private List<String> missingKeywords = List.of();

    // ── Section-by-section feedback ────────────────────────────────────────────

    /**
     * Map of section name → feedback string.
     * Common keys: "Contact", "Summary", "Education", "Experience",
     *              "Skills", "Projects", "Certifications", "Achievements".
     */
    @Builder.Default
    private Map<String, String> sectionFeedback = Map.of();

    /**
     * True if a section is present, false if missing.
     * Same key set as sectionFeedback.
     */
    @Builder.Default
    private Map<String, Boolean> sectionPresence = Map.of();

    // ── Improvements ──────────────────────────────────────────────────────────

    /** Ordered list of top-5 actionable improvement suggestions. */
    @Builder.Default
    private List<String> topImprovements = List.of();

    // ── Meta ──────────────────────────────────────────────────────────────────

    /** Format detected: "PDF" | "DOCX". */
    private String fileFormat;

    /** Approximate word count of the extracted text. */
    private int wordCount;

    /** Estimated reading time in seconds. */
    private int readingTimeSeconds;

    /**
     * Any warning produced during extraction or analysis
     * (e.g., "scanned PDF — OCR not supported").
     */
    private String warning;
}
