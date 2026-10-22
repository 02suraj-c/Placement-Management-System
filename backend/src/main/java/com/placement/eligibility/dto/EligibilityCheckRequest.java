package com.placement.eligibility.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for evaluating a student's eligibility for a placement drive.
 *
 * Package : com.placement.eligibility.dto
 * Owner   : Person 3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityCheckRequest {

    @NotNull(message = "studentId is required")
    private Long studentId;

    @NotNull(message = "driveId is required")
    private Long driveId;
}
