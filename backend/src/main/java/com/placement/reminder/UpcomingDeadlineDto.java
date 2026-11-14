package com.placement.reminder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingDeadlineDto {

    private String type; // "PLACEMENT_DRIVE" or "HACKATHON"
    private Long id;
    private String title;
    private String organization;
    private LocalDate deadline;
    private long daysRemaining;
    private String status;
}

