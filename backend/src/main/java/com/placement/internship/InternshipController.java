package com.placement.internship;

import com.placement.dto.ApiResponse;
import com.placement.internship.dto.InternshipRequest;
import com.placement.internship.dto.InternshipResponse;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
import com.placement.user.User;
import com.placement.user.UserRepository;
import com.placement.user.UserRole;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
public class InternshipController {

    private final InternshipService internshipService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    @Autowired
    public InternshipController(InternshipService internshipService,
                                UserRepository userRepository,
                                StudentRepository studentRepository) {
        this.internshipService = internshipService;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    public InternshipController(InternshipService internshipService) {
        this(internshipService, null, null);
    }

    /**
     * Create a new internship listing.
     * Authorized: ADMIN, OFFICER, PLACEMENT_OFFICER, FACULTY, HOD, COMPANY, TPO, DEPUTY_TPO, COORDINATOR.
     * POST /api/internships
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER', 'FACULTY', 'HOD', 'COMPANY', 'TPO', 'DEPUTY_TPO', 'COORDINATOR')")
    public ResponseEntity<ApiResponse<InternshipResponse>> createInternship(
            @Valid @RequestBody InternshipRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails != null ? userDetails.getUsername() : "system";
        InternshipResponse response = internshipService.createInternship(request, email);
        return new ResponseEntity<>(ApiResponse.success(response, "Internship listing created successfully"), HttpStatus.CREATED);
    }

    /**
     * Get all active internship listings for students and staff.
     * If user is student, strictly filtered to their academic year!
     * GET /api/internships?keyword=&location=&year=
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<InternshipResponse>>> getActiveInternships(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal UserDetails userDetails) {

        Integer effectiveYear = year;
        if (userDetails != null && userDetails.getUsername() != null) {
            String email = userDetails.getUsername();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null && user.getRole() == UserRole.ROLE_STUDENT) {
                Student student = studentRepository.findByUserEmail(email).orElse(null);
                effectiveYear = (student != null) ? student.getAcademicYear() : 1;
            }
        }

        List<InternshipResponse> response = internshipService.getAllActiveInternships(keyword, location, effectiveYear);
        return ResponseEntity.ok(ApiResponse.success(response, "Active internships fetched successfully"));
    }

    /**
     * Get all internship listings (both active and inactive) for management.
     * GET /api/internships/all
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER', 'FACULTY', 'HOD', 'COMPANY', 'TPO', 'DEPUTY_TPO', 'COORDINATOR')")
    public ResponseEntity<ApiResponse<List<InternshipResponse>>> getAllInternships() {
        List<InternshipResponse> response = internshipService.getAllInternships();
        return ResponseEntity.ok(ApiResponse.success(response, "All internships fetched successfully"));
    }

    /**
     * Get a specific internship by ID.
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
     * PUT /api/internships/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER', 'FACULTY', 'HOD', 'COMPANY', 'TPO', 'DEPUTY_TPO', 'COORDINATOR')")
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
     * DELETE /api/internships/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER', 'FACULTY', 'HOD', 'COMPANY', 'TPO', 'DEPUTY_TPO')")
    public ResponseEntity<ApiResponse<Void>> deleteInternship(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails != null ? userDetails.getUsername() : "system";
        internshipService.deleteInternship(id, email);
        return ResponseEntity.ok(ApiResponse.success(null, "Internship listing deleted successfully"));
    }
}
