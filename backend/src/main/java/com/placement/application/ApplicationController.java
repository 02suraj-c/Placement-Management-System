package com.placement.application;

import com.placement.application.dto.ApplicationRequest;
import com.placement.application.dto.ApplicationResponse;
import com.placement.application.dto.ApplicationStatusUpdateRequest;
import com.placement.application.dto.ApplicationTimelineResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> apply(
            @RequestBody ApplicationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        ApplicationResponse response = applicationService.createApplication(request, email);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByStudent(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        List<ApplicationResponse> responses = applicationService.getApplicationsByStudent(studentId, email);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/drive/{driveId}")
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
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody ApplicationStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        ApplicationResponse response = applicationService.updateStatus(id, request, email);
        return ResponseEntity.ok(response);
    }
}
