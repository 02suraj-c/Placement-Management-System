package com.placement.interview;

import com.placement.application.ApplicationRepository;
import com.placement.application.PlacementApplication;
import com.placement.auth.AuthService;
import com.placement.auth.dto.UserResponse;
import com.placement.exception.ResourceNotFoundException;
import com.placement.interview.dto.InterviewResultRequest;
import com.placement.interview.dto.InterviewScheduleRequest;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
import com.placement.user.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;
    private final AuthService authService;
    private final StudentRepository studentRepository;
    private final ApplicationRepository applicationRepository;

    public InterviewController(InterviewService interviewService,
                               AuthService authService,
                               StudentRepository studentRepository,
                               ApplicationRepository applicationRepository) {
        this.interviewService = interviewService;
        this.authService = authService;
        this.studentRepository = studentRepository;
        this.applicationRepository = applicationRepository;
    }

    @PostMapping("/schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER')")
    public ResponseEntity<InterviewRound> scheduleInterview(@RequestBody InterviewScheduleRequest request) {
        InterviewRound round = interviewService.scheduleInterview(request);
        return new ResponseEntity<>(round, HttpStatus.CREATED);
    }

    @GetMapping("/application/{applicationId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'OFFICER', 'PLACEMENT_OFFICER')")
    public ResponseEntity<List<InterviewRound>> getInterviewsByApplication(@PathVariable Long applicationId) {
        UserResponse currentUser = authService.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == UserRole.ROLE_STUDENT) {
            Student student = studentRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student", "userId", currentUser.getId()));
            PlacementApplication application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));
            if (!student.getId().equals(application.getStudentId())) {
                throw new AccessDeniedException("You are not authorized to view interviews for another student's application");
            }
        }
        List<InterviewRound> rounds = interviewService.getInterviewsByApplication(applicationId);
        return ResponseEntity.ok(rounds);
    }

    @PostMapping("/{roundId}/result")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER')")
    public ResponseEntity<RoundResult> recordResult(
            @PathVariable Long roundId,
            @RequestBody InterviewResultRequest request) {
        RoundResult result = interviewService.recordResult(roundId, request);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}
