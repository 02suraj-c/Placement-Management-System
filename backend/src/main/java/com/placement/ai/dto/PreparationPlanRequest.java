package com.placement.ai.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for preparation plan generation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreparationPlanRequest {

    @NotBlank(message = "targetRole is required")
    @Size(max = 150)
    private String targetRole;

    @Size(max = 150)
    private String targetCompany;

    /**
     * The date of the placement drive.
     * Must be in the future (at least tomorrow).
     */
    @NotNull(message = "driveDate is required")
    @Future(message = "driveDate must be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate driveDate;

    /**
     * Skills the student is weak in or has not yet studied.
     * At least one gap must be supplied.
     */
    @NotEmpty(message = "At least one skill gap must be specified")
    private List<@NotBlank @Size(max = 100) String> skillGaps;

    /**
     * Skills the student already possesses (optional).
     * Used by AI to avoid recommending what they already know.
     */
    @Builder.Default
    private List<String> existingSkills = List.of();

    /**
     * Daily study hours the student can commit (1-12).
     * Defaults to 4 if not specified.
     */
    @Min(1) @Max(12)
    @Builder.Default
    private int dailyStudyHours = 4;
}
