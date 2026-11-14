package com.placement.hackathon;

import com.placement.dto.ApiResponse;
import com.placement.hackathon.dto.HackathonRegistrationResponse;
import com.placement.hackathon.dto.HackathonRequest;
import com.placement.hackathon.dto.HackathonResponse;
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
 * REST controller for Hackathon Management.
 * Base path: /api/hackathons
 */
@RestController
@RequestMapping("/api/hackathons")
public class HackathonController {

    private final HackathonService hackathonService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    @Autowired
    public HackathonController(HackathonService hackathonService,
                               UserRepository userRepository,
                               StudentRepository studentRepository) {
        this.hackathonService = hackathonService;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    public HackathonController(HackathonService hackathonService) {
        this(hackathonService, null, null);
    }

    /**
     * Create a new hackathon event.
     * Authorized: ADMIN, OFFICER, PLACEMENT_OFFICER, FACULTY, HOD, COMPANY, TPO, DEPUTY_TPO, COORDINATOR.
     * POST /api/hackathons
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER', 'FACULTY', 'HOD', 'COMPANY', 'TPO', 'DEPUTY_TPO', 'COORDINATOR')")
    public ResponseEntity<ApiResponse<HackathonResponse>> createHackathon(
            @Valid @RequestBody HackathonRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails != null ? userDetails.getUsername() : "system";
        HackathonResponse response = hackathonService.createHackathon(request, email);
        return new ResponseEntity<>(ApiResponse.success(response, "Hackathon created successfully"), HttpStatus.CREATED);
    }

    /**
     * Get all active hackathons for students and staff.
     * If user is student, strictly filtered to their academic year!
     * GET /api/hackathons?keyword=&mode=&status=&year=
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<HackathonResponse>>> getActiveHackathons(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) HackathonMode mode,
            @RequestParam(required = false) HackathonStatus status,
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

        List<HackathonResponse> response = hackathonService.getAllActiveHackathons(keyword, mode, status, effectiveYear);
        return ResponseEntity.ok(ApiResponse.success(response, "Active hackathons fetched successfully"));
    }

    /**
     * Get all hackathon listings (both active and inactive) for staff management.
     * GET /api/hackathons/all
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER', 'FACULTY', 'HOD', 'COMPANY', 'TPO', 'DEPUTY_TPO', 'COORDINATOR')")
    public ResponseEntity<ApiResponse<List<HackathonResponse>>> getAllHackathons() {
        List<HackathonResponse> response = hackathonService.getAllHackathons();
        return ResponseEntity.ok(ApiResponse.success(response, "All hackathons fetched successfully"));
    }

    /**
     * Get a specific hackathon by ID.
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
     * PUT /api/hackathons/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER', 'FACULTY', 'HOD', 'COMPANY', 'TPO', 'DEPUTY_TPO', 'COORDINATOR')")
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
     * DELETE /api/hackathons/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER', 'FACULTY', 'HOD', 'COMPANY', 'TPO', 'DEPUTY_TPO')")
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
     * GET /api/hackathons/{id}/registrations
     */
    @GetMapping("/{id}/registrations")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER', 'FACULTY', 'HOD', 'COMPANY', 'TPO', 'DEPUTY_TPO', 'COORDINATOR')")
    public ResponseEntity<ApiResponse<List<HackathonRegistrationResponse>>> getHackathonRegistrations(
            @PathVariable Long id) {

        List<HackathonRegistrationResponse> response = hackathonService.getHackathonRegistrations(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Hackathon attendee registrations fetched successfully"));
    }
}
