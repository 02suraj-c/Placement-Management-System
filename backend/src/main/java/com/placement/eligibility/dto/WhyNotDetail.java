package com.placement.eligibility.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WhyNotDetail {

    private boolean eligible;
    private List<String> reasons;

    private Double cgpaShortfall;
    private Double studentCgpa;
    private Double requiredCgpa;

    private Integer excessBacklogs;
    private Integer studentBacklogs;
    private Integer allowedBacklogs;

    private Boolean branchDisqualified;
    private String studentDepartment;
    private String eligibleBranches;

    private Boolean yearDisqualified;
    private Integer studentYear;
    private String targetYears;

    private Boolean driveInactive;
    private String driveStatus;

    private Boolean profileIncomplete;
}

