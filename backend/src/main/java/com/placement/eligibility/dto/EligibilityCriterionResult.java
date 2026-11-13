package com.placement.eligibility.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EligibilityCriterionResult {

    private String criterion;
    private boolean passed;
    private String message;
    private Object studentValue;
    private Object requiredValue;
}

