package com.placement.attendance;

import com.placement.attendance.dto.AttendanceRecordResponse;
import com.placement.attendance.dto.AttendanceSummaryResponse;
import com.placement.attendance.dto.CRTAttendanceRequest;
import com.placement.attendance.dto.ClassAttendanceRequest;
import com.placement.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Class and CRT Attendance Management.
 * Base path: /api/attendance
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * Record class attendance for a student.
     * Authorized: FACULTY, HOD, OFFICER, PLACEMENT_OFFICER, ADMIN.
     * POST /api/attendance/class
     */
    @PostMapping("/class")
    @PreAuthorize("hasAnyRole('FACULTY', 'HOD', 'OFFICER', 'PLACEMENT_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> recordClassAttendance(
            @Valid @RequestBody ClassAttendanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        AttendanceRecordResponse response = attendanceService.recordClassAttendance(request, userDetails.getUsername());
        return new ResponseEntity<>(ApiResponse.success(response, "Class attendance recorded successfully"), HttpStatus.CREATED);
    }

    /**
     * Record CRT (Campus Recruitment Training) attendance for a student.
     * Authorized: FACULTY, HOD, OFFICER, PLACEMENT_OFFICER, ADMIN.
     * POST /api/attendance/crt
     */
    @PostMapping("/crt")
    @PreAuthorize("hasAnyRole('FACULTY', 'HOD', 'OFFICER', 'PLACEMENT_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> recordCRTAttendance(
            @Valid @RequestBody CRTAttendanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        AttendanceRecordResponse response = attendanceService.recordCRTAttendance(request, userDetails.getUsername());
        return new ResponseEntity<>(ApiResponse.success(response, "CRT attendance recorded successfully"), HttpStatus.CREATED);
    }

    /**
     * Get attendance summary and percentage for authenticated student.
     * GET /api/attendance/me/summary
     */
    @GetMapping("/me/summary")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<AttendanceSummaryResponse>> getMyAttendanceSummary(
            @AuthenticationPrincipal UserDetails userDetails) {

        AttendanceSummaryResponse response = attendanceService.getCurrentStudentAttendanceSummary(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Attendance summary fetched successfully"));
    }

    /**
     * Get class attendance history for authenticated student.
     * GET /api/attendance/me/class
     */
    @GetMapping("/me/class")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getMyClassAttendance(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<AttendanceRecordResponse> response = attendanceService.getCurrentStudentClassAttendance(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Class attendance history fetched successfully"));
    }

    /**
     * Get CRT attendance history for authenticated student.
     * GET /api/attendance/me/crt
     */
    @GetMapping("/me/crt")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getMyCRTAttendance(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<AttendanceRecordResponse> response = attendanceService.getCurrentStudentCRTAttendance(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "CRT attendance history fetched successfully"));
    }

    /**
     * Get attendance summary for any student by student ID.
     * Students may only view their own attendance; staff may view any student's.
     * GET /api/attendance/student/{studentId}/summary
     */
    @GetMapping("/student/{studentId}/summary")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'HOD', 'OFFICER', 'PLACEMENT_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceSummaryResponse>> getStudentAttendanceSummary(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        AttendanceSummaryResponse response = attendanceService.getStudentAttendanceSummary(studentId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Attendance summary fetched successfully"));
    }

    /**
     * Get class attendance history by student ID.
     * GET /api/attendance/student/{studentId}/class
     */
    @GetMapping("/student/{studentId}/class")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'HOD', 'OFFICER', 'PLACEMENT_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getStudentClassAttendance(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<AttendanceRecordResponse> response = attendanceService.getStudentClassAttendance(studentId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Class attendance history fetched successfully"));
    }

    /**
     * Get CRT attendance history by student ID.
     * GET /api/attendance/student/{studentId}/crt
     */
    @GetMapping("/student/{studentId}/crt")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'HOD', 'OFFICER', 'PLACEMENT_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getStudentCRTAttendance(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<AttendanceRecordResponse> response = attendanceService.getStudentCRTAttendance(studentId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "CRT attendance history fetched successfully"));
    }
}
