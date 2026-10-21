package com.placement.ai.dto;

import com.placement.questionbank.InterviewQuestion;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

/**
 * Request DTO for AI-powered interview question generation.
 * All filter fields are optional except {@code count}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class InterviewGenerationRequest {

    /**
     * Target job role (e.g., "Software Engineer", "Data Analyst").
     */
    @NotBlank(message = "targetRole is required")
    @Size(max = 150)
    private String targetRole;

    /**
     * Target company for style/stack tuning (e.g., "Google", "TCS").
     * Nullable — omit for generic questions.
     */
    @Size(max = 150)
    private String targetCompany;

    /**
     * Comma-separated or List of skills to focus on
     * (e.g., ["Java", "Spring Boot", "System Design"]).
     */
    @NotEmpty(message = "At least one skill must be specified")
    private List<@NotBlank @Size(max = 100) String> skills;

    /**
     * Difficulty level for generated questions.
     */
    @NotNull(message = "difficulty is required (EASY | MEDIUM | HARD)")
    private InterviewQuestion.Difficulty difficulty;

    /**
     * Type of questions to generate.
     */
    @NotNull(message = "questionType is required")
    private InterviewQuestion.QuestionType questionType;

    /**
     * Number of questions to generate. Must be between 1 and 20.
     */
    @NotNull
    @Min(value = 1,  message = "count must be at least 1")
    @Max(value = 20, message = "count must not exceed 20")
    private Integer count;

    /**
     * When true, the generated questions are persisted into the question bank
     * immediately after generation. Requires write-role on the caller's part
     * (enforced in the controller via @PreAuthorize).
     */
    @Builder.Default
    private boolean saveToBank = false;

    /**
     * Optional year context (e.g., 2024) for recent-question style prompting.
     */
    @Min(2000) @Max(2100)
    private Integer year;
}
