package com.placement.interview;

import com.placement.interview.dto.InterviewResultRequest;
import com.placement.interview.dto.InterviewScheduleRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/schedule")
    public ResponseEntity<InterviewRound> scheduleInterview(
            @RequestBody InterviewScheduleRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        InterviewRound round = interviewService.scheduleInterview(request, email);
        return new ResponseEntity<>(round, HttpStatus.CREATED);
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<InterviewRound>> getInterviewsByApplication(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        List<InterviewRound> rounds = interviewService.getInterviewsByApplication(applicationId, email);
        return ResponseEntity.ok(rounds);
    }

    @GetMapping("/{roundId}")
    public ResponseEntity<InterviewRound> getInterviewById(
            @PathVariable Long roundId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        InterviewRound round = interviewService.getInterviewById(roundId, email);
        return ResponseEntity.ok(round);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<InterviewRound>> getInterviewsForStudent(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        List<InterviewRound> rounds = interviewService.getInterviewsForStudent(studentId, email);
        return ResponseEntity.ok(rounds);
    }

    @PostMapping("/{roundId}/result")
    public ResponseEntity<RoundResult> recordResult(
            @PathVariable Long roundId,
            @RequestBody InterviewResultRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        RoundResult result = interviewService.recordResult(roundId, request, email);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}
