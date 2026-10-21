package com.placement.attendance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttendanceSummaryResponse {

    private Long studentId;
    private String studentName;
    private String rollNumber;
    private String department;

    // Class Attendance Metrics
    private long totalClassSessions;
    private long attendedClassSessions;
    private double classAttendancePercentage;

    // CRT Attendance Metrics
    private long totalCrtSessions;
    private long attendedCrtSessions;
    private double crtAttendancePercentage;

    // Aggregate
    private long totalSessions;
    private long totalAttended;
    private double overallAttendancePercentage;

    private boolean eligibleForPlacements; // e.g. overall >= 75%

    private List<SubjectAttendanceSummary> subjectBreakdowns;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectAttendanceSummary {
        private String subject;
        private long totalSessions;
        private long attendedSessions;
        private double percentage;
    }
}
