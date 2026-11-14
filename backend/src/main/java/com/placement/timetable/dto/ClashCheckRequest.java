package com.placement.timetable.dto;

import com.placement.timetable.SlotType;
import lombok.*;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClashCheckRequest {
    private Long slotIdToExclude; // null if new
    private Integer academicYear;
    private Integer semester;
    private String division;
    private String day;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long facultyId;
    private String facultyName;
    private String room;
    private SlotType type;
}

