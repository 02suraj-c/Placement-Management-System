package com.placement.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.placement.application.ApplicationStatus;
import com.placement.application.ApplicationTimelineEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationTimelineResponse {

    private Long applicationId;
    private Long studentId;
    private Long driveId;
    private String companyName;
    private String jobTitle;
    private ApplicationStatus currentStatus;
    private LocalDateTime appliedAt;
    private LocalDateTime lastUpdatedAt;

    private List<TimelineStageDto> stages;
    private List<ApplicationTimelineEvent> eventHistory;
}

