package com.placement.timetable.dto;

import com.placement.timetable.SlotType;
import com.placement.timetable.TimetableSlot;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimetableSlotResponse {

    private Long id;
    private Integer academicYear;
    private Integer semester;
    private String division;
    private String day;
    private LocalTime startTime;
    private LocalTime endTime;
    private String timeDisplay;
    private String subject;
    private Long facultyId;
    private String facultyName;
    private String room;
    private SlotType type;
    private Boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TimetableSlotResponse from(TimetableSlot slot) {
        String timeDisplay = (slot.getStartTime() != null && slot.getEndTime() != null)
                ? String.format("%02d:%02d - %02d:%02d",
                    slot.getStartTime().getHour(), slot.getStartTime().getMinute(),
                    slot.getEndTime().getHour(), slot.getEndTime().getMinute())
                : "";

        return TimetableSlotResponse.builder()
                .id(slot.getId())
                .academicYear(slot.getAcademicYear())
                .semester(slot.getSemester())
                .division(slot.getDivision())
                .day(slot.getDay())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .timeDisplay(timeDisplay)
                .subject(slot.getSubject())
                .facultyId(slot.getFacultyId())
                .facultyName(slot.getFacultyName())
                .room(slot.getRoom())
                .type(slot.getType())
                .published(slot.getPublished())
                .createdAt(slot.getCreatedAt())
                .updatedAt(slot.getUpdatedAt())
                .build();
    }
}

