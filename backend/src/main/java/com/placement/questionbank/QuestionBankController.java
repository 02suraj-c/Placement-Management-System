package com.placement.questionbank;

import com.placement.questionbank.dto.QuestionFilterRequest;
import com.placement.questionbank.dto.QuestionRequest;
import com.placement.questionbank.dto.QuestionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for the Interview Question Bank.
 *
 * Base path: /api/questions
 *
 * Write roles: TPO, DEPUTY_TPO, COORDINATOR, FACULTY
 * Read roles:  all authenticated users (STUDENT, FACULTY, HOD, TPO, DEPUTY_TPO, COORDINATOR)
 */
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionBankController {

    private final QuestionBankService questionBankService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/questions  — create a new question
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAnyRole('TPO','DEPUTY_TPO','COORDINATOR','FACULTY')")
    public ResponseEntity<QuestionResponse> create(
            @Valid @RequestBody QuestionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        QuestionResponse response = questionBankService.create(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/questions/{id}  — get single question (all roles)
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuestionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(questionBankService.getById(id));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/questions  — paginated + filtered list (all roles)
    //
    // Query params (all optional):
    //   company, topic, targetRole, difficulty, questionType, keyword, yearAsked
    //   page (0-indexed, default 0), size (default 20), sort (default createdAt,desc)
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<QuestionResponse>> list(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String targetRole,
            @RequestParam(required = false) InterviewQuestion.Difficulty difficulty,
            @RequestParam(required = false) InterviewQuestion.QuestionType questionType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer yearAsked,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        QuestionFilterRequest filter = QuestionFilterRequest.builder()
                .company(company)
                .topic(topic)
                .targetRole(targetRole)
                .difficulty(difficulty)
                .questionType(questionType)
                .keyword(keyword)
                .yearAsked(yearAsked)
                .build();

        return ResponseEntity.ok(questionBankService.filter(filter, pageable));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/questions/{id}  — full update
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TPO','DEPUTY_TPO','COORDINATOR','FACULTY')")
    public ResponseEntity<QuestionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                questionBankService.update(id, request, userDetails.getUsername()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/questions/{id}  — delete (TPO / DEPUTY_TPO only)
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TPO','DEPUTY_TPO')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questionBankService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/questions/meta/companies  — distinct company list
    // GET /api/questions/meta/topics     — distinct topic list
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/meta/companies")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getCompanies() {
        return ResponseEntity.ok(questionBankService.getDistinctCompanies());
    }

    @GetMapping("/meta/topics")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getTopics() {
        return ResponseEntity.ok(questionBankService.getDistinctTopics());
    }
}
