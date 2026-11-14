package com.placement.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.placement.application.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimelineStageDto {

    private ApplicationStatus status;
    private String label;
    private String description;
    private boolean completed;
    private boolean current;
    private LocalDateTime reachedAt;
}

