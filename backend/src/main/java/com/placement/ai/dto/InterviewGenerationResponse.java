package com.placement.ai.dto;

import com.placement.questionbank.InterviewQuestion;
import lombok.*;

import java.util.List;

/**
 * Response DTO returned by InterviewGenerator.
 * Contains the generated questions, generation metadata,
 * and (optionally) the IDs of questions persisted to the bank.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewGenerationResponse {

    /** Generated questions as flat DTOs (no JPA entity exposure). */
    @Builder.Default
    private List<GeneratedQuestion> questions = List.of();

    /** Total number of questions generated. */
    private int totalGenerated;

    /** True if questions were persisted to the question bank. */
    private boolean savedToBank;

    /**
     * IDs of saved InterviewQuestion rows.
     * Empty list when {@code savedToBank} is false.
     */
    @Builder.Default
    private List<Long> savedQuestionIds = List.of();

    /** Context used for generation (echoed back for transparency). */
    private String targetRole;
    private String targetCompany;
    private InterviewQuestion.Difficulty difficulty;
    private InterviewQuestion.QuestionType questionType;

    // ── nested DTO ─────────────────────────────────────────────────────────────

    /**
     * A single AI-generated question with its model answer and metadata.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GeneratedQuestion {

        private String questionText;
        private String answer;

        /**
         * One-sentence hint or approach guide for this question.
         */
        private String hint;

        /**
         * Suggested follow-up question the interviewer might ask.
         */
        private String followUp;

        /** Skill(s) this question tests (subset of the requested skills list). */
        @Builder.Default
        private List<String> relatedSkills = List.of();

        /** Estimated time to answer in minutes. */
        private int estimatedMinutes;
    }
}
