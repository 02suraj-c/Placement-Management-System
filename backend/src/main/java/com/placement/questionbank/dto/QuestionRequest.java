package com.placement.questionbank.dto;

import com.placement.questionbank.InterviewQuestion;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for creating or updating an InterviewQuestion.
 * Intentionally separated from the entity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionRequest {

    @NotBlank(message = "questionText is required")
    @Size(max = 2000)
    private String questionText;

    @Size(max = 2000)
    private String answer;

    @Size(max = 150)
    private String company;          // nullable = company-agnostic

    @NotBlank(message = "topic is required")
    @Size(max = 100)
    private String topic;

    @Size(max = 150)
    private String targetRole;

    @NotNull(message = "difficulty is required (EASY | MEDIUM | HARD)")
    private InterviewQuestion.Difficulty difficulty;

    @NotNull(message = "questionType is required")
    private InterviewQuestion.QuestionType questionType;

    @Size(max = 500)
    private String tags;             // comma-separated

    @Min(2000) @Max(2100)
    private Integer yearAsked;

    private Integer marks;

    @Size(max = 50)
    private String source;           // defaults to MANUAL if omitted
}
