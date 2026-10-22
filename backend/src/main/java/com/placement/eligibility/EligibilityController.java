package com.placement.eligibility;

import com.placement.dto.ApiResponse;
import com.placement.eligibility.dto.EligibilityCheckRequest;
import com.placement.eligibility.dto.EligibilityCheckResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Eligibility evaluation operations.
 *
 * Base Path: /api/eligibility
 * Package  : com.placement.eligibility
 * Owner    : Person 3
 *
 * Security:
 *   - Check eligibility: isAuthenticated()
 */
@RestController
@RequestMapping("/api/eligibility")
@RequiredArgsConstructor
public class EligibilityController {

    private final EligibilityService eligibilityService;

    /**
     * Evaluate eligibility of a student for a specific placement drive.
     *
     * GET /api/eligibility/check?studentId={studentId}&driveId={driveId}
     *
     * @param request containing validated studentId and driveId
     * @return ApiResponse with EligibilityCheckResponse
     */
    @GetMapping("/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EligibilityCheckResponse>> checkEligibility(
            @Valid @ModelAttribute EligibilityCheckRequest request) {

        if (request == null || request.getStudentId() == null) {
            throw new IllegalArgumentException("studentId is required");
        }
        if (request.getDriveId() == null) {
            throw new IllegalArgumentException("driveId is required");
        }

        EligibilityCheckResponse response = eligibilityService.checkEligibility(
                request.getStudentId(),
                request.getDriveId()
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Eligibility evaluated successfully"));
    }
}
