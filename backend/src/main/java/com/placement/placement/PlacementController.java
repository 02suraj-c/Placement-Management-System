package com.placement.placement;

import com.placement.student.Student;
import com.placement.student.StudentRepository;
import com.placement.user.User;
import com.placement.user.UserRepository;
import com.placement.user.UserRole;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Placement Drive operations.
 *
 * Base Path: /api/placement-drives
 * Package  : com.placement.placement
 */
@RestController
@RequestMapping("/api/placement-drives")
public class PlacementController {

    private final PlacementService placementService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public PlacementController(PlacementService placementService) {
        this(placementService, null, null);
    }

    @Autowired
    public PlacementController(PlacementService placementService,
                               UserRepository userRepository,
                               StudentRepository studentRepository) {
        this.placementService = placementService;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    // =========================================================================
    // POST /api/placement-drives
    // =========================================================================
    @PostMapping
    @PreAuthorize("hasAnyRole('OFFICER','PLACEMENT_OFFICER','ADMIN','TPO','DEPUTY_TPO','COORDINATOR','FACULTY','HOD')")
    public ResponseEntity<PlacementDrive> createPlacementDrive(
            @Valid @RequestBody PlacementDrive placementDrive) {

        PlacementDrive created = placementService.createPlacementDrive(placementDrive);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // =========================================================================
    // GET /api/placement-drives
    // =========================================================================
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PlacementDrive>> getAllPlacementDrives(
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails != null) {
            String email = userDetails.getUsername();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null && user.getRole() == UserRole.ROLE_STUDENT) {
                // Students can ONLY see drives targeting their own academic year.
                // Any 'year' parameter passed from client is ignored and overridden.
                Student student = studentRepository.findByUserEmail(email).orElse(null);
                int studentYear = (student != null) ? student.getAcademicYear() : 1;
                return ResponseEntity.ok(placementService.getDrivesForYear(studentYear));
            }
        }

        // Faculty/Staff can filter by year or view all
        if (year != null && year > 0) {
            return ResponseEntity.ok(placementService.getDrivesForYear(year));
        }

        List<PlacementDrive> drives = placementService.getAllPlacementDrives();
        return ResponseEntity.ok(drives);
    }

    // =========================================================================
    // GET /api/placement-drives/{id}
    // =========================================================================
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlacementDrive> getPlacementDriveById(@PathVariable Long id) {
        PlacementDrive drive = placementService.getPlacementDriveById(id);
        return ResponseEntity.ok(drive);
    }

    // =========================================================================
    // PUT /api/placement-drives/{id}
    // =========================================================================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OFFICER','PLACEMENT_OFFICER','ADMIN','TPO','DEPUTY_TPO','COORDINATOR','FACULTY','HOD')")
    public ResponseEntity<PlacementDrive> updatePlacementDrive(
            @PathVariable Long id,
            @Valid @RequestBody PlacementDrive placementDrive) {

        PlacementDrive updated = placementService.updatePlacementDrive(id, placementDrive);
        return ResponseEntity.ok(updated);
    }

    // =========================================================================
    // DELETE /api/placement-drives/{id}
    // =========================================================================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OFFICER','PLACEMENT_OFFICER','ADMIN','TPO','DEPUTY_TPO')")
    public ResponseEntity<Void> deletePlacementDrive(@PathVariable Long id) {
        placementService.deletePlacementDrive(id);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // GET /api/placement-drives/company/{companyId}
    // =========================================================================
    @GetMapping("/company/{companyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PlacementDrive>> getDrivesByCompany(@PathVariable Long companyId) {
        List<PlacementDrive> drives = placementService.getDrivesByCompany(companyId);
        return ResponseEntity.ok(drives);
    }

    // =========================================================================
    // GET /api/placement-drives/status/{status}
    // =========================================================================
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PlacementDrive>> getDrivesByStatus(@PathVariable DriveStatus status) {
        List<PlacementDrive> drives = placementService.getDrivesByStatus(status);
        return ResponseEntity.ok(drives);
    }

    // =========================================================================
    // GET /api/placement-drives/search?keyword=
    // =========================================================================
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PlacementDrive>> searchDrives(@RequestParam String keyword) {
        List<PlacementDrive> drives = placementService.searchDrivesByTitle(keyword);
        return ResponseEntity.ok(drives);
    }
}
