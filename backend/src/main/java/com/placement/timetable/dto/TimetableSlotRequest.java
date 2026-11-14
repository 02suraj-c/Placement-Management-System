package com.placement.timetable.dto;

import com.placement.timetable.SlotType;
import com.placement.timetable.TimetableSlot;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimetableSlotRequest {

    private Long id;

    @NotNull(message = "Academic year is required (1-4)")
    @Min(1) @Max(4)
    private Integer academicYear;

    @NotNull(message = "Semester is required (1-8)")
    @Min(1) @Max(8)
    private Integer semester;

    @NotBlank(message = "Division / Section is required")
    @Size(max = 20)
    private String division;

    @NotBlank(message = "Day is required")
    private String day;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotBlank(message = "Subject is required")
    @Size(max = 150)
    private String subject;

    private Long facultyId;

    @Size(max = 100)
    private String facultyName;

    @Size(max = 50)
    private String room;

    @NotNull(message = "Type is required")
    private SlotType type;

    @Builder.Default
    private Boolean published = true;
}

