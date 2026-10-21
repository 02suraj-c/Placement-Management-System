package com.placement.student;

import com.placement.dto.ApiResponse;
import com.placement.student.dto.StudentProfileRequest;
import com.placement.student.dto.StudentProfileResponse;
import com.placement.student.dto.StudentUpdateRequest;
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
 * REST controller for Student Management.
 * Base path: /api/students
 */
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /**
     * Create a new student profile for the authenticated student.
     * POST /api/students
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> createProfile(
            @Valid @RequestBody StudentProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        StudentProfileResponse response = studentService.createProfile(request, userDetails.getUsername());
        return new ResponseEntity<>(ApiResponse.success(response, "Student profile created successfully"), HttpStatus.CREATED);
    }

    /**
     * Get the authenticated student's profile.
     * GET /api/students/me
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> getCurrentProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        StudentProfileResponse response = studentService.getCurrentStudentProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Student profile fetched successfully"));
    }

    /**
     * Update the authenticated student's academic and profile details.
     * PUT /api/students/me
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> updateCurrentProfile(
            @Valid @RequestBody StudentUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        StudentProfileResponse response = studentService.updateCurrentStudentProfile(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Student profile updated successfully"));
    }

    /**
     * Get student profile by ID.
     * Students can only access their own profile; faculty/officers/admin can view any student.
     * GET /api/students/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'HOD', 'OFFICER', 'PLACEMENT_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> getProfileById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        StudentProfileResponse response = studentService.getProfileById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Student profile fetched successfully"));
    }

    /**
     * Retrieve all students with suitable role-based access control.
     * Accessible only by administrative, officer, or faculty roles.
     * GET /api/students
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER', 'FACULTY', 'HOD')")
    public ResponseEntity<ApiResponse<List<StudentProfileResponse>>> getAllStudents() {
        List<StudentProfileResponse> responses = studentService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.success(responses, "Student directory fetched successfully"));
    }
}
