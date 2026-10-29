package com.placement.questionbank.dto;

import com.placement.questionbank.InterviewQuestion;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for returning an InterviewQuestion to the client.
 * Never exposes JPA internals.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponse {

    private Long id;
    private String questionText;
    private String answer;
    private String company;
    private String topic;
    private String targetRole;
    private InterviewQuestion.Difficulty difficulty;
    private InterviewQuestion.QuestionType questionType;
    private String tags;
    private Integer yearAsked;
    private Integer marks;
    private String source;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Static factory — maps entity → response DTO. */
    public static QuestionResponse from(InterviewQuestion q) {
        return QuestionResponse.builder()
                .id(q.getId())
                .questionText(q.getQuestionText())
                .answer(q.getAnswer())
                .company(q.getCompany())
                .topic(q.getTopic())
                .targetRole(q.getTargetRole())
                .difficulty(q.getDifficulty())
                .questionType(q.getQuestionType())
                .tags(q.getTags())
                .yearAsked(q.getYearAsked())
                .marks(q.getMarks() != null ? q.getMarks() : 10)
                .source(q.getSource())
                .createdBy(q.getCreatedBy())
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .build();
    }
}
