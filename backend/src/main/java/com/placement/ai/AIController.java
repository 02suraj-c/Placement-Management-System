package com.placement.ai;

import com.placement.ai.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller exposing all AI-powered endpoints.
 *
 * Base path : /api/ai
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ Endpoint                          │ Roles                               │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ POST /resume/analyze              │ All authenticated                   │
 * │ POST /interview/generate          │ All authenticated                   │
 * │ POST /interview/generate-and-save │ TPO, DEPUTY_TPO, COORDINATOR,       │
 * │                                   │ FACULTY                             │
 * │ POST /preparation/plan            │ All authenticated                   │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * Global exception handling is delegated to {@link com.placement.exception.GlobalExceptionHandler}.
 * Role enforcement uses {@code @PreAuthorize} (requires {@code @EnableMethodSecurity}).
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Validated
public class AIController {

    private final ResumeAnalyzer       resumeAnalyzer;
    private final InterviewGenerator   interviewGenerator;
    private final PreparationPlanner   preparationPlanner;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/ai/resume/analyze
    //
    // Multipart request:
    //   - Part "file"    : PDF or DOCX resume (required, max 5 MB)
    //   - Part "request" : JSON blob of ResumeAnalysisRequest (optional)
    //
    // Roles: all authenticated users
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping(
        value    = "/resume/analyze",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResumeAnalysisResponse> analyzeResume(
            @RequestPart("file")
                MultipartFile file,
            @RequestPart(value = "request", required = false)
                ResumeAnalysisRequest request) {

        // request part is optional — default to empty DTO if omitted
        ResumeAnalysisRequest effectiveRequest =
                request != null ? request : new ResumeAnalysisRequest();

        ResumeAnalysisResponse response = resumeAnalyzer.analyze(file, effectiveRequest);
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/ai/interview/generate
    //
    // Body: InterviewGenerationRequest JSON
    // saveToBank is ignored / forced to false for this endpoint —
    // use /generate-and-save to persist.
    //
    // Roles: all authenticated users
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/interview/generate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InterviewGenerationResponse> generateQuestions(
            @Valid @RequestBody InterviewGenerationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Ensure this endpoint never persists (use generate-and-save for that)
        request.setSaveToBank(false);

        InterviewGenerationResponse response =
                interviewGenerator.generate(request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/ai/interview/generate-and-save
    //
    // Same body as /generate but forcibly sets saveToBank=true.
    // Returns savedQuestionIds in the response.
    //
    // Roles: TPO, DEPUTY_TPO, COORDINATOR, FACULTY
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/interview/generate-and-save")
    @PreAuthorize("hasAnyRole('TPO','DEPUTY_TPO','COORDINATOR','FACULTY')")
    public ResponseEntity<InterviewGenerationResponse> generateAndSaveQuestions(
            @Valid @RequestBody InterviewGenerationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        request.setSaveToBank(true);

        InterviewGenerationResponse response =
                interviewGenerator.generate(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/ai/preparation/plan
    //
    // Body: PreparationPlanRequest JSON
    // Roles: all authenticated users
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/preparation/plan")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PreparationPlanResponse> generatePlan(
            @Valid @RequestBody PreparationPlanRequest request) {

        PreparationPlanResponse response = preparationPlanner.plan(request);
        return ResponseEntity.ok(response);
    }
}
