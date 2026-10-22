package com.placement.attendance.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassAttendanceRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotBlank(message = "Subject is required")
    @Size(max = 100, message = "Subject name must not exceed 100 characters")
    private String subject;

    @NotNull(message = "Attendance date is required")
    @PastOrPresent(message = "Attendance date cannot be in the future")
    private LocalDate attendanceDate;

    @NotNull(message = "Attendance status is required")
    private Boolean present;

    @Size(max = 255, message = "Remarks must not exceed 255 characters")
    private String remarks;
}
