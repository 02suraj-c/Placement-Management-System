package com.placement.application;

import com.placement.application.dto.ApplicationRequest;
import com.placement.application.dto.ApplicationResponse;
import com.placement.application.dto.ApplicationStatusUpdateRequest;
import com.placement.application.dto.ApplicationTimelineResponse;
import com.placement.auth.AuthService;
import com.placement.auth.dto.UserResponse;
import com.placement.exception.ResourceNotFoundException;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
import com.placement.user.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final AuthService authService;
    private final StudentRepository studentRepository;

    public ApplicationController(ApplicationService applicationService,
                                 AuthService authService,
                                 StudentRepository studentRepository) {
        this.applicationService = applicationService;
        this.authService = authService;
        this.studentRepository = studentRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApplicationResponse> apply(
            @RequestBody ApplicationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse currentUser = authService.getCurrentUser();
        Student student = studentRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "userId", currentUser.getId()));

        if (request == null || request.getStudentId() == null || !student.getId().equals(request.getStudentId())) {
            throw new AccessDeniedException("You are not authorized to submit applications for another student");
        }

        ApplicationResponse response = applicationService.createApplication(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'OFFICER', 'PLACEMENT_OFFICER')")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByStudent(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse currentUser = authService.getCurrentUser();
        if (currentUser.getRole() == UserRole.ROLE_STUDENT) {
            Student student = studentRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student", "userId", currentUser.getId()));
            if (!student.getId().equals(studentId)) {
                throw new AccessDeniedException("You are not authorized to view another student's applications");
            }
        }

        List<ApplicationResponse> responses = applicationService.getApplicationsByStudent(studentId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/drive/{driveId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER')")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByDrive(@PathVariable Long driveId) {
        List<ApplicationResponse> responses = applicationService.getApplicationsByDrive(driveId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        ApplicationResponse response = applicationService.getApplicationById(id, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<ApplicationTimelineResponse> getApplicationTimeline(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        ApplicationTimelineResponse timeline = applicationService.getApplicationTimeline(id, email);
        return ResponseEntity.ok(timeline);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER')")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody ApplicationStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        ApplicationResponse response = applicationService.updateStatus(id, request);
        return ResponseEntity.ok(response);
    }
}
