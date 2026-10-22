package com.placement.hackathon;

import com.placement.dto.ApiResponse;
import com.placement.hackathon.dto.HackathonRegistrationResponse;
import com.placement.hackathon.dto.HackathonRequest;
import com.placement.hackathon.dto.HackathonResponse;
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
 * REST controller for Hackathon Management.
 * Base path: /api/hackathons
 */
@RestController
@RequestMapping("/api/hackathons")
@RequiredArgsConstructor
public class HackathonController {

    private final HackathonService hackathonService;

    /**
     * Create a new hackathon event.
     * Authorized: ADMIN, OFFICER, PLACEMENT_OFFICER, FACULTY, HOD, COMPANY.
     * POST /api/hackathons
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER', 'FACULTY', 'HOD', 'COMPANY')")
    public ResponseEntity<ApiResponse<HackathonResponse>> createHackathon(
            @Valid @RequestBody HackathonRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails != null ? userDetails.getUsername() : "system";
        HackathonResponse response = hackathonService.createHackathon(request, email);
        return new ResponseEntity<>(ApiResponse.success(response, "Hackathon created successfully"), HttpStatus.CREATED);
    }

    /**
     * Get all active hackathons for students and staff.
     * GET /api/hackathons?keyword=&mode=&status=
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<HackathonResponse>>> getActiveHackathons(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) HackathonMode mode,
            @RequestParam(required = false) HackathonStatus status) {

        List<HackathonResponse> response = hackathonService.getAllActiveHackathons(keyword, mode, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Active hackathons fetched successfully"));
    }

    /**
     * Get all hackathon listings (both active and inactive) for staff management.
     * Authorized: ADMIN, OFFICER, PLACEMENT_OFFICER, FACULTY, HOD, COMPANY.
     * GET /api/hackathons/all
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER', 'FACULTY', 'HOD', 'COMPANY')")
    public ResponseEntity<ApiResponse<List<HackathonResponse>>> getAllHackathons() {
        List<HackathonResponse> response = hackathonService.getAllHackathons();
        return ResponseEntity.ok(ApiResponse.success(response, "All hackathons fetched successfully"));
    }

    /**
     * Get a specific hackathon by ID.
     * Accessible to all authenticated users.
     * GET /api/hackathons/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<HackathonResponse>> getHackathonById(@PathVariable Long id) {
        HackathonResponse response = hackathonService.getHackathonById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Hackathon details fetched successfully"));
    }

    /**
     * Update an existing hackathon event.
     * Authorized: ADMIN, OFFICER, PLACEMENT_OFFICER, FACULTY, HOD, COMPANY.
     * PUT /api/hackathons/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER', 'FACULTY', 'HOD', 'COMPANY')")
    public ResponseEntity<ApiResponse<HackathonResponse>> updateHackathon(
            @PathVariable Long id,
            @Valid @RequestBody HackathonRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails != null ? userDetails.getUsername() : "system";
        HackathonResponse response = hackathonService.updateHackathon(id, request, email);
        return ResponseEntity.ok(ApiResponse.success(response, "Hackathon updated successfully"));
    }

    /**
     * Delete an existing hackathon event.
     * Authorized: ADMIN, OFFICER, PLACEMENT_OFFICER, FACULTY, HOD, COMPANY.
     * DELETE /api/hackathons/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER', 'FACULTY', 'HOD', 'COMPANY')")
    public ResponseEntity<ApiResponse<Void>> deleteHackathon(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails != null ? userDetails.getUsername() : "system";
        hackathonService.deleteHackathon(id, email);
        return ResponseEntity.ok(ApiResponse.success(null, "Hackathon deleted successfully"));
    }

    /**
     * Register authenticated student for a hackathon.
     * Authorized: STUDENT.
     * POST /api/hackathons/{id}/register
     */
    @PostMapping("/{id}/register")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<HackathonRegistrationResponse>> registerStudent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        HackathonRegistrationResponse response = hackathonService.registerStudent(id, userDetails.getUsername());
        return new ResponseEntity<>(ApiResponse.success(response, "Registered for hackathon successfully"), HttpStatus.CREATED);
    }

    /**
     * Get hackathons registered by the current authenticated student.
     * Authorized: STUDENT.
     * GET /api/hackathons/me/registrations
     */
    @GetMapping("/me/registrations")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<HackathonRegistrationResponse>>> getMyRegistrations(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<HackathonRegistrationResponse> response = hackathonService.getStudentRegistrations(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "My hackathon registrations fetched successfully"));
    }

    /**
     * Get attendee registration roster for a hackathon.
     * Authorized: ADMIN, OFFICER, PLACEMENT_OFFICER, FACULTY, HOD, COMPANY.
     * GET /api/hackathons/{id}/registrations
     */
    @GetMapping("/{id}/registrations")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER', 'FACULTY', 'HOD', 'COMPANY')")
    public ResponseEntity<ApiResponse<List<HackathonRegistrationResponse>>> getHackathonRegistrations(
            @PathVariable Long id) {

        List<HackathonRegistrationResponse> response = hackathonService.getHackathonRegistrations(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Hackathon attendee registrations fetched successfully"));
    }
}
