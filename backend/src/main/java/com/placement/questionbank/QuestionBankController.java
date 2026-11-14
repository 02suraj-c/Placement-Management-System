package com.placement.questionbank;

import com.placement.questionbank.dto.QuestionFilterRequest;
import com.placement.questionbank.dto.QuestionRequest;
import com.placement.questionbank.dto.QuestionResponse;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
import com.placement.user.User;
import com.placement.user.UserRepository;
import com.placement.user.UserRole;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Write roles: FACULTY, OFFICER, PLACEMENT_OFFICER, ADMIN
 * Delete roles: OFFICER, PLACEMENT_OFFICER, ADMIN
 * Read roles:  all authenticated users
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionBankController {

    private final QuestionBankService questionBankService;

    @Autowired(required = false)
    private UserRepository userRepository;

    @Autowired(required = false)
    private StudentRepository studentRepository;

    public QuestionBankController(QuestionBankService questionBankService) {
        this.questionBankService = questionBankService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/questions  — create a new question
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAnyRole('FACULTY','OFFICER','PLACEMENT_OFFICER','ADMIN')")
    public ResponseEntity<QuestionResponse> create(
            @Valid @RequestBody QuestionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails != null ? userDetails.getUsername() : null;
        QuestionResponse response = questionBankService.create(request, username);
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
            @RequestParam(required = false) Integer targetYear,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserDetails userDetails) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Integer effectiveTargetYear = targetYear;

        // If authenticated user is a student, enforce their academic year!
        if (userRepository != null && studentRepository != null && userDetails != null && userDetails.getUsername() != null) {
            String email = userDetails.getUsername();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null && user.getRole() == UserRole.ROLE_STUDENT) {
                Student student = studentRepository.findByUserEmail(email).orElse(null);
                effectiveTargetYear = (student != null) ? student.getAcademicYear() : 1;
            }
        }

        QuestionFilterRequest filter = QuestionFilterRequest.builder()
                .company(company)
                .topic(topic)
                .targetRole(targetRole)
                .difficulty(difficulty)
                .questionType(questionType)
                .keyword(keyword)
                .yearAsked(yearAsked)
                .targetYear(effectiveTargetYear)
                .build();

        return ResponseEntity.ok(questionBankService.filter(filter, pageable));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/questions/{id}  — full update
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('FACULTY','OFFICER','PLACEMENT_OFFICER','ADMIN')")
    public ResponseEntity<QuestionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails != null ? userDetails.getUsername() : null;
        QuestionResponse response = questionBankService.update(id, request, username);
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/questions/{id}  — delete (OFFICER, PLACEMENT_OFFICER, ADMIN only)
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OFFICER','PLACEMENT_OFFICER','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questionBankService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/questions/companies (and /meta/companies) — distinct companies for autocomplete
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping({"/companies", "/meta/companies"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getDistinctCompanies() {
        return ResponseEntity.ok(questionBankService.getDistinctCompanies());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/questions/topics (and /meta/topics) — distinct topics for autocomplete
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping({"/topics", "/meta/topics"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getDistinctTopics() {
        return ResponseEntity.ok(questionBankService.getDistinctTopics());
    }
}
