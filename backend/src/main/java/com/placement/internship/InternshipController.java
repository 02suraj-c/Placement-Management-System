package com.placement.internship;

import com.placement.dto.ApiResponse;
import com.placement.internship.dto.InternshipRequest;
import com.placement.internship.dto.InternshipResponse;
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
 * REST controller for Internship Management.
 * Base path: /api/internships
 */
@RestController
@RequestMapping("/api/internships")
@RequiredArgsConstructor
public class InternshipController {

    private final InternshipService internshipService;

    /**
     * Create a new internship listing.
     * Authorized: ADMIN, OFFICER, PLACEMENT_OFFICER, FACULTY, HOD, COMPANY.
     * POST /api/internships
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER', 'FACULTY', 'HOD', 'COMPANY')")
    public ResponseEntity<ApiResponse<InternshipResponse>> createInternship(
            @Valid @RequestBody InternshipRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails != null ? userDetails.getUsername() : "system";
        InternshipResponse response = internshipService.createInternship(request, email);
        return new ResponseEntity<>(ApiResponse.success(response, "Internship listing created successfully"), HttpStatus.CREATED);
    }

    /**
     * Get all active internship listings for students and staff.
     * GET /api/internships?keyword=&location=
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<InternshipResponse>>> getActiveInternships(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location) {

        List<InternshipResponse> response = internshipService.getAllActiveInternships(keyword, location);
        return ResponseEntity.ok(ApiResponse.success(response, "Active internships fetched successfully"));
    }

    /**
     * Get all internship listings (both active and inactive) for management.
     * Authorized: ADMIN, OFFICER, PLACEMENT_OFFICER, FACULTY, HOD, COMPANY.
     * GET /api/internships/all
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER', 'FACULTY', 'HOD', 'COMPANY')")
    public ResponseEntity<ApiResponse<List<InternshipResponse>>> getAllInternships() {
        List<InternshipResponse> response = internshipService.getAllInternships();
        return ResponseEntity.ok(ApiResponse.success(response, "All internships fetched successfully"));
    }

    /**
     * Get a specific internship by ID.
     * Accessible by all authenticated users (students and staff).
     * GET /api/internships/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<InternshipResponse>> getInternshipById(@PathVariable Long id) {
        InternshipResponse response = internshipService.getInternshipById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Internship details fetched successfully"));
    }

    /**
     * Update an existing internship listing.
     * Authorized: ADMIN, OFFICER, PLACEMENT_OFFICER, FACULTY, HOD, COMPANY.
     * PUT /api/internships/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER', 'FACULTY', 'HOD', 'COMPANY')")
    public ResponseEntity<ApiResponse<InternshipResponse>> updateInternship(
            @PathVariable Long id,
            @Valid @RequestBody InternshipRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails != null ? userDetails.getUsername() : "system";
        InternshipResponse response = internshipService.updateInternship(id, request, email);
        return ResponseEntity.ok(ApiResponse.success(response, "Internship listing updated successfully"));
    }

    /**
     * Delete an existing internship listing.
     * Authorized: ADMIN, OFFICER, PLACEMENT_OFFICER, FACULTY, HOD, COMPANY.
     * DELETE /api/internships/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER', 'FACULTY', 'HOD', 'COMPANY')")
    public ResponseEntity<ApiResponse<Void>> deleteInternship(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails != null ? userDetails.getUsername() : "system";
        internshipService.deleteInternship(id, email);
        return ResponseEntity.ok(ApiResponse.success(null, "Internship listing deleted successfully"));
    }
}
