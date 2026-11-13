package com.placement.timetable;

import com.placement.dto.ApiResponse;
import com.placement.timetable.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    /**
     * Get timetable for authenticated student.
     * Enforces the student's own academic year from profile context.
     * Semester dropdown can only navigate within the student's academic year.
     * GET /api/timetable/student?semester=&division=
     */
    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<TimetableSlotResponse>>> getStudentTimetable(
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) String division,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<TimetableSlotResponse> response = timetableService.getStudentTimetable(
                userDetails.getUsername(), semester, division);
        return ResponseEntity.ok(ApiResponse.success(response, "Student timetable fetched successfully"));
    }

    /**
     * Get timetable for staff (Faculty, HOD, TPO, Deputy TPO, Coordinator, Admin).
     * Can view by Year, Semester, and Division.
     * GET /api/timetable?year=&semester=&division=
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('FACULTY', 'HOD', 'OFFICER', 'PLACEMENT_OFFICER', 'ADMIN', 'TPO', 'DEPUTY_TPO', 'COORDINATOR')")
    public ResponseEntity<ApiResponse<List<TimetableSlotResponse>>> getStaffTimetable(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) String division) {

        List<TimetableSlotResponse> response = timetableService.getStaffTimetable(year, semester, division);
        return ResponseEntity.ok(ApiResponse.success(response, "Timetable fetched successfully"));
    }

    /**
     * Create a new timetable slot with automatic clash detection.
     * POST /api/timetable/slots
     */
    @PostMapping("/slots")
    @PreAuthorize("hasAnyRole('FACULTY', 'HOD', 'OFFICER', 'PLACEMENT_OFFICER', 'ADMIN', 'TPO', 'DEPUTY_TPO', 'COORDINATOR')")
    public ResponseEntity<ApiResponse<TimetableSlotResponse>> createSlot(
            @Valid @RequestBody TimetableSlotRequest request) {

        TimetableSlotResponse response = timetableService.createSlot(request);
        return new ResponseEntity<>(ApiResponse.success(response, "Timetable slot created successfully"), HttpStatus.CREATED);
    }

    /**
     * Update an existing timetable slot with clash validation.
     * PUT /api/timetable/slots/{id}
     */
    @PutMapping("/slots/{id}")
    @PreAuthorize("hasAnyRole('FACULTY', 'HOD', 'OFFICER', 'PLACEMENT_OFFICER', 'ADMIN', 'TPO', 'DEPUTY_TPO', 'COORDINATOR')")
    public ResponseEntity<ApiResponse<TimetableSlotResponse>> updateSlot(
            @PathVariable Long id,
            @Valid @RequestBody TimetableSlotRequest request) {

        TimetableSlotResponse response = timetableService.updateSlot(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Timetable slot updated successfully"));
    }

    /**
     * Delete a timetable slot.
     * DELETE /api/timetable/slots/{id}
     */
    @DeleteMapping("/slots/{id}")
    @PreAuthorize("hasAnyRole('FACULTY', 'HOD', 'OFFICER', 'PLACEMENT_OFFICER', 'ADMIN', 'TPO', 'DEPUTY_TPO', 'COORDINATOR')")
    public ResponseEntity<ApiResponse<Void>> deleteSlot(@PathVariable Long id) {
        timetableService.deleteSlot(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Timetable slot deleted successfully"));
    }

    /**
     * Pre-flight clash detection check.
     * POST /api/timetable/check-clash
     */
    @PostMapping("/check-clash")
    @PreAuthorize("hasAnyRole('FACULTY', 'HOD', 'OFFICER', 'PLACEMENT_OFFICER', 'ADMIN', 'TPO', 'DEPUTY_TPO', 'COORDINATOR')")
    public ResponseEntity<ApiResponse<ClashCheckResponse>> checkClash(
            @RequestBody ClashCheckRequest request) {

        ClashCheckResponse response = timetableService.checkClashes(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Clash check evaluated"));
    }

    /**
     * Publish timetable for a Year/Semester/Division and notify students.
     * POST /api/timetable/publish
     */
    @PostMapping("/publish")
    @PreAuthorize("hasAnyRole('FACULTY', 'HOD', 'OFFICER', 'PLACEMENT_OFFICER', 'ADMIN', 'TPO', 'DEPUTY_TPO', 'COORDINATOR')")
    public ResponseEntity<ApiResponse<Integer>> publishTimetable(
            @Valid @RequestBody PublishTimetableRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        int count = timetableService.publishTimetable(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(count, "Timetable published successfully. " + count + " slots updated and students notified."));
    }

    /**
     * Get distinct divisions for Year and Semester.
     * GET /api/timetable/divisions?year=&semester=
     */
    @GetMapping("/divisions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<String>>> getDivisions(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer semester) {

        List<String> divisions = timetableService.getDivisions(year, semester);
        return ResponseEntity.ok(ApiResponse.success(divisions, "Divisions fetched successfully"));
    }

    /**
     * Download formatted PDF of timetable.
     * GET /api/timetable/pdf?year=&semester=&division=
     */
    @GetMapping("/pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadPdf(
            @RequestParam(required = false, defaultValue = "4") Integer year,
            @RequestParam(required = false, defaultValue = "7") Integer semester,
            @RequestParam(required = false, defaultValue = "A") String division) throws IOException {

        byte[] pdfBytes = timetableService.generatePdf(year, semester, division);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", String.format("Timetable_Year%d_Sem%d_%s.pdf", year, semester, division));
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}

