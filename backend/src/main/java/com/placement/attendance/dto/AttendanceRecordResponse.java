package com.placement.attendance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttendanceRecordResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private String rollNumber;
    private String type; // CLASS or CRT
    private String title; // Subject or Session Name
    private LocalDate date;
    private boolean present;
    private String status; // PRESENT or ABSENT
    private String trainerName;
    private String remarks;
    private String recordedBy;
    private LocalDateTime createdAt;
}
