package com.placement.timetable.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublishTimetableRequest {
    @NotNull(message = "Academic year is required")
    @Min(1) @Max(4)
    private Integer academicYear;

    @NotNull(message = "Semester is required")
    @Min(1) @Max(8)
    private Integer semester;

    private String division; // optional, if null publishes for all divisions in that year/sem
}

