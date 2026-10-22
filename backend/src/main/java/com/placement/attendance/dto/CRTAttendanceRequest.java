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
public class CRTAttendanceRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotBlank(message = "Session name is required")
    @Size(max = 150, message = "Session name must not exceed 150 characters")
    private String sessionName;

    @NotNull(message = "Session date is required")
    @PastOrPresent(message = "Session date cannot be in the future")
    private LocalDate sessionDate;

    @NotNull(message = "Attendance status is required")
    private Boolean present;

    @Size(max = 100, message = "Trainer name must not exceed 100 characters")
    private String trainerName;

    @Size(max = 255, message = "Remarks must not exceed 255 characters")
    private String remarks;
}
