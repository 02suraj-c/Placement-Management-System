package com.placement.placement;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Placement Drive operations.
 *
 * Base Path: /api/placement-drives
 * Package  : com.placement.placement
 * Owner    : Person 3
 *
 * Security:
 *   - Read operations   : isAuthenticated()
 *   - Create/Update     : hasAnyRole('OFFICER','PLACEMENT_OFFICER','ADMIN','TPO','DEPUTY_TPO','COORDINATOR')
 *   - Delete            : hasAnyRole('OFFICER','PLACEMENT_OFFICER','ADMIN','TPO','DEPUTY_TPO')
 */
@RestController
@RequestMapping("/api/placement-drives")
public class PlacementController {

    private final PlacementService placementService;

    public PlacementController(PlacementService placementService) {
        this.placementService = placementService;
    }

    // =========================================================================
    // POST /api/placement-drives
    // =========================================================================
    @PostMapping
    @PreAuthorize("hasAnyRole('OFFICER','PLACEMENT_OFFICER','ADMIN','TPO','DEPUTY_TPO','COORDINATOR')")
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
    public ResponseEntity<List<PlacementDrive>> getAllPlacementDrives() {
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
    @PreAuthorize("hasAnyRole('OFFICER','PLACEMENT_OFFICER','ADMIN','TPO','DEPUTY_TPO','COORDINATOR')")
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
    // GET /api/placement-drives/search?jobTitle={jobTitle}
    // =========================================================================
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PlacementDrive>> searchDrivesByJobTitle(
            @RequestParam(required = false, defaultValue = "") String jobTitle) {

        List<PlacementDrive> drives = placementService.searchDrivesByJobTitle(jobTitle);
        return ResponseEntity.ok(drives);
    }
}
