package com.placement.questionbank.dto;

import com.placement.questionbank.InterviewQuestion;
import lombok.*;

/**
 * DTO for filter parameters passed as query params to GET /api/questions.
 * All fields are optional; null means "no filter on this field".
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionFilterRequest {

    private String company;
    private String topic;
    private String targetRole;
    private InterviewQuestion.Difficulty difficulty;
    private InterviewQuestion.QuestionType questionType;
    private String keyword;   // searches questionText + tags
    private Integer yearAsked;
}
