package com.placement.ai.dto;

import lombok.*;

/**
 * Request DTO for resume analysis.
 * The actual file bytes arrive via MultipartFile in the controller;
 * this DTO carries the optional metadata sent alongside the upload.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeAnalysisRequest {

    /**
     * Target job role for ATS matching (e.g., "Software Engineer").
     * Used to tune the keyword set the AI checks against.
     */
    private String targetRole;

    /**
     * Target company name (optional). Lets the AI tailor keyword matching
     * for known company tech stacks.
     */
    private String targetCompany;

    /**
     * Comma-separated list of additional job-description keywords the student
     * wants checked (optional override).
     */
    private String jobDescriptionKeywords;
}
