package com.placement.questionbank;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents an interview question in the question bank.
 * Stored in table: interview_questions
 */
@Entity
@Table(name = "interview_questions", indexes = {
    @Index(name = "idx_iq_company",    columnList = "company"),
    @Index(name = "idx_iq_topic",      columnList = "topic"),
    @Index(name = "idx_iq_difficulty", columnList = "difficulty"),
    @Index(name = "idx_iq_role",       columnList = "target_role")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Question text must not be blank")
    @Size(max = 2000, message = "Question text must not exceed 2000 characters")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    /**
     * The answer or model answer for this question.
     */
    @Column(columnDefinition = "TEXT")
    private String answer;

    /**
     * Company this question is associated with (e.g., "Google", "TCS").
     * NULL means company-agnostic.
     */
    @Size(max = 150)
    @Column(length = 150)
    private String company;

    /**
     * Topic / domain (e.g., "Java", "Data Structures", "HR").
     */
    @NotBlank(message = "Topic must not be blank")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String topic;

    /**
     * Target job role (e.g., "Software Engineer", "Data Analyst").
     * Maps to column target_role.
     */
    @Size(max = 150)
    @Column(name = "target_role", length = 150)
    private String targetRole;

    @NotNull(message = "Difficulty must not be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;

    @NotNull(message = "Question type must not be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestionType questionType;

    /**
     * Comma-separated tags for fine-grained filtering (e.g., "recursion,tree,dp").
     * Kept simple to avoid a join table for tags.
     */
    @Size(max = 500)
    @Column(length = 500)
    private String tags;

    /**
     * Year when this question was reportedly asked (nullable).
     */
    @Min(2000)
    @Max(2100)
    private Integer yearAsked;

    /**
     * Source: "AI_GENERATED" | "MANUAL" | "STUDENT_CONTRIBUTED"
     */
    @Size(max = 50)
    @Column(length = 50)
    @Builder.Default
    private String source = "MANUAL";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /** Username (email) of the user who created this question. */
    @Size(max = 255)
    private String createdBy;

    // ── nested enums ────────────────────────────────────────────────────────

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    public enum QuestionType {
        TECHNICAL, HR, APTITUDE, CODING, CASE_STUDY, GROUP_DISCUSSION
    }
}
