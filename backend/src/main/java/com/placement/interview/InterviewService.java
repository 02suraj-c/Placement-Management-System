package com.placement.interview;

import com.placement.application.ApplicationRepository;
import com.placement.application.ApplicationStatus;
import com.placement.application.PlacementApplication;
import com.placement.interview.dto.InterviewResultRequest;
import com.placement.interview.dto.InterviewScheduleRequest;
import com.placement.notification.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class InterviewService {

    private final InterviewRoundRepository interviewRoundRepository;
    private final RoundResultRepository roundResultRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationService notificationService;

    public InterviewService(InterviewRoundRepository interviewRoundRepository,
                            RoundResultRepository roundResultRepository,
                            ApplicationRepository applicationRepository,
                            NotificationService notificationService) {
        this.interviewRoundRepository = interviewRoundRepository;
        this.roundResultRepository = roundResultRepository;
        this.applicationRepository = applicationRepository;
        this.notificationService = notificationService;
    }

    public InterviewRound scheduleInterview(InterviewScheduleRequest request) {
        if (request == null || request.getApplicationId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application ID must not be null");
        }

        PlacementApplication application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> {
                    // TODO: switch to ResourceNotFoundException
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found with id: " + request.getApplicationId());
                });

        application.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        applicationRepository.save(application);

        LocalDateTime scheduledTime = request.getInterviewDateTime() != null ? request.getInterviewDateTime() : request.getDateTime();
        String locationOrLink = request.getLocation() != null ? request.getLocation() : request.getLink();

        InterviewRound round = new InterviewRound();
        round.setApplicationId(application.getId());
        round.setRoundName(request.getRoundName());
        round.setInterviewDateTime(scheduledTime);
        round.setMode(request.getMode());
        round.setLocation(locationOrLink);
        round.setStatus("SCHEDULED");

        InterviewRound savedRound = interviewRoundRepository.save(round);

        notificationService.createNotification(
                application.getStudentId(),
                "Interview scheduled for round '" + savedRound.getRoundName() + "' on " + savedRound.getInterviewDateTime() + " (Mode: " + savedRound.getMode() + ")"
        );

        return savedRound;
    }

    @Transactional(readOnly = true)
    public List<InterviewRound> getInterviewsByApplication(Long applicationId) {
        if (!applicationRepository.existsById(applicationId)) {
            // TODO: switch to ResourceNotFoundException
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found with id: " + applicationId);
        }
        return interviewRoundRepository.findByApplicationId(applicationId);
    }

    public RoundResult recordResult(Long roundId, InterviewResultRequest request) {
        if (request == null || request.getResult() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Result must not be null");
        }

        InterviewRound round = interviewRoundRepository.findById(roundId)
                .orElseThrow(() -> {
                    // TODO: switch to ResourceNotFoundException
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview round not found with id: " + roundId);
                });

        PlacementApplication application = applicationRepository.findById(round.getApplicationId())
                .orElseThrow(() -> {
                    // TODO: switch to ResourceNotFoundException
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found with id: " + round.getApplicationId());
                });

        RoundResult roundResult = new RoundResult();
        roundResult.setRoundId(roundId);
        roundResult.setResult(request.getResult());
        roundResult.setScore(request.getScore());
        roundResult.setRemarks(request.getRemarks());

        RoundResult savedResult = roundResultRepository.save(roundResult);

        round.setStatus(request.getResult().toUpperCase());
        interviewRoundRepository.save(round);

        String resultStr = request.getResult().trim().toUpperCase();
        if ("PASSED".equals(resultStr)) {
            application.setStatus(ApplicationStatus.SHORTLISTED);
        } else if ("FAILED".equals(resultStr) || "REJECTED".equals(resultStr)) {
            application.setStatus(ApplicationStatus.REJECTED);
        } else if ("SELECTED".equals(resultStr)) {
            application.setStatus(ApplicationStatus.SELECTED);
        }
        applicationRepository.save(application);

        notificationService.createNotification(
                application.getStudentId(),
                "Interview result declared for round '" + round.getRoundName() + "': " + request.getResult() + ". Application status updated to " + application.getStatus()
        );

        return savedResult;
    }
}
