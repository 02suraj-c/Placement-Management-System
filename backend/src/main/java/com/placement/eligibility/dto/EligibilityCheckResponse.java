package com.placement.eligibility.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO containing evaluation result and details of student eligibility for a placement drive.
 *
 * Package : com.placement.eligibility.dto
 * Owner   : Person 3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EligibilityCheckResponse {

    private Long studentId;
    private Long driveId;
    private boolean eligible;
    private List<String> reasons;

    private Double studentCgpa;
    private Double minimumCgpa;
    private Integer studentActiveBacklogs;
    private Integer maximumAllowedBacklogs;
    private String studentDepartment;
    private String eligibleBranches;
    private String driveStatus;
}
