package com.placement.interview;

import com.placement.interview.dto.InterviewResultRequest;
import com.placement.interview.dto.InterviewScheduleRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/schedule")
    public ResponseEntity<InterviewRound> scheduleInterview(@RequestBody InterviewScheduleRequest request) {
        InterviewRound round = interviewService.scheduleInterview(request);
        return new ResponseEntity<>(round, HttpStatus.CREATED);
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<InterviewRound>> getInterviewsByApplication(@PathVariable Long applicationId) {
        List<InterviewRound> rounds = interviewService.getInterviewsByApplication(applicationId);
        return ResponseEntity.ok(rounds);
    }

    @PostMapping("/{roundId}/result")
    public ResponseEntity<RoundResult> recordResult(
            @PathVariable Long roundId,
            @RequestBody InterviewResultRequest request) {
        RoundResult result = interviewService.recordResult(roundId, request);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}
