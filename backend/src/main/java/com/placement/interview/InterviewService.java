package com.placement.interview;

import com.placement.application.ApplicationRepository;
import com.placement.application.ApplicationStatus;
import com.placement.application.PlacementApplication;
import com.placement.exception.ResourceNotFoundException;
import com.placement.interview.dto.InterviewResultRequest;
import com.placement.interview.dto.InterviewScheduleRequest;
import com.placement.notification.NotificationService;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
import com.placement.user.User;
import com.placement.user.UserRepository;
import com.placement.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRoundRepository interviewRoundRepository;
    private final RoundResultRepository roundResultRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public InterviewRound scheduleInterview(InterviewScheduleRequest request) {
        return scheduleInterview(request, null);
    }

    public InterviewRound scheduleInterview(InterviewScheduleRequest request, String currentUserEmail) {
        if (request == null || request.getApplicationId() == null) {
            throw new IllegalArgumentException("Application ID must not be null");
        }

        // Role check: Students cannot schedule interviews
        validateStaffAccess(currentUserEmail, "schedule interviews");

        PlacementApplication application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("PlacementApplication", "id", request.getApplicationId()));

        if (application.getStatus() == ApplicationStatus.REJECTED) {
            throw new IllegalStateException("Cannot schedule an interview for a rejected application");
        }
        if (application.getStatus() == ApplicationStatus.SELECTED) {
            throw new IllegalStateException("Cannot schedule an interview for an already selected application");
        }

        LocalDateTime scheduledTime = request.getInterviewDateTime() != null ? request.getInterviewDateTime() : request.getDateTime();
        if (scheduledTime != null && scheduledTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Interview date and time cannot be in the past");
        }

        String locationOrLink = request.getLocation() != null ? request.getLocation() : request.getLink();

        application.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        applicationRepository.save(application);

        InterviewRound round = new InterviewRound();
        round.setApplicationId(application.getId());
        round.setRoundName(request.getRoundName());
        round.setInterviewDateTime(scheduledTime);
        round.setMode(request.getMode());
        round.setLocation(locationOrLink);
        round.setStatus("SCHEDULED");

        InterviewRound savedRound = interviewRoundRepository.save(round);
        log.info("Interview scheduled for application ID: {}, round: {}", application.getId(), savedRound.getRoundName());

        notificationService.createNotification(
                application.getStudentId(),
                "Interview scheduled for round '" + savedRound.getRoundName() + "' on " + savedRound.getInterviewDateTime() + " (Mode: " + savedRound.getMode() + ")"
        );

        return savedRound;
    }

    @Transactional(readOnly = true)
    public List<InterviewRound> getInterviewsByApplication(Long applicationId) {
        return getInterviewsByApplication(applicationId, null);
    }

    @Transactional(readOnly = true)
    public List<InterviewRound> getInterviewsByApplication(Long applicationId, String currentUserEmail) {
        if (applicationId == null) {
            throw new IllegalArgumentException("Application ID must not be null");
        }
        PlacementApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("PlacementApplication", "id", applicationId));

        validateStudentOwnership(application.getStudentId(), currentUserEmail);

        return interviewRoundRepository.findByApplicationId(applicationId);
    }

    @Transactional(readOnly = true)
    public InterviewRound getInterviewById(Long roundId, String currentUserEmail) {
        if (roundId == null) {
            throw new IllegalArgumentException("Round ID must not be null");
        }
        InterviewRound round = interviewRoundRepository.findById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("InterviewRound", "id", roundId));

        PlacementApplication application = applicationRepository.findById(round.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("PlacementApplication", "id", round.getApplicationId()));

        validateStudentOwnership(application.getStudentId(), currentUserEmail);

        return round;
    }

    @Transactional(readOnly = true)
    public List<InterviewRound> getInterviewsForStudent(Long studentId, String currentUserEmail) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID must not be null");
        }
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student", "id", studentId);
        }

        validateStudentOwnership(studentId, currentUserEmail);

        List<PlacementApplication> applications = applicationRepository.findByStudentId(studentId);
        if (applications.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> applicationIds = applications.stream().map(PlacementApplication::getId).toList();
        return interviewRoundRepository.findByApplicationIdIn(applicationIds);
    }

    public RoundResult recordResult(Long roundId, InterviewResultRequest request) {
        return recordResult(roundId, request, null);
    }

    public RoundResult recordResult(Long roundId, InterviewResultRequest request, String currentUserEmail) {
        if (request == null || request.getResult() == null || request.getResult().isBlank()) {
            throw new IllegalArgumentException("Result must not be null or blank");
        }

        // Role check: Students cannot record interview results
        validateStaffAccess(currentUserEmail, "record interview results");

        InterviewRound round = interviewRoundRepository.findById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("InterviewRound", "id", roundId));

        PlacementApplication application = applicationRepository.findById(round.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("PlacementApplication", "id", round.getApplicationId()));

        RoundResult roundResult = new RoundResult();
        roundResult.setRoundId(roundId);
        roundResult.setResult(request.getResult().trim().toUpperCase());
        roundResult.setScore(request.getScore());
        roundResult.setRemarks(request.getRemarks());

        RoundResult savedResult = roundResultRepository.save(roundResult);

        String resultStr = request.getResult().trim().toUpperCase();
        round.setStatus(resultStr);
        interviewRoundRepository.save(round);

        if ("PASSED".equals(resultStr)) {
            application.setStatus(ApplicationStatus.SHORTLISTED);
        } else if ("FAILED".equals(resultStr) || "REJECTED".equals(resultStr)) {
            application.setStatus(ApplicationStatus.REJECTED);
        } else if ("SELECTED".equals(resultStr)) {
            application.setStatus(ApplicationStatus.SELECTED);
        }
        applicationRepository.save(application);
        log.info("Recorded interview result for round ID: {}, result: {}", roundId, resultStr);

        notificationService.createNotification(
                application.getStudentId(),
                "Interview result declared for round '" + round.getRoundName() + "': " + resultStr + ". Application status updated to " + application.getStatus()
        );

        return savedResult;
    }

    private void validateStaffAccess(String currentUserEmail, String action) {
        if (currentUserEmail == null) {
            return;
        }
        User user = userRepository.findByEmail(currentUserEmail).orElse(null);
        if (user != null && user.getRole() == UserRole.ROLE_STUDENT) {
            throw new AccessDeniedException("Students are not authorized to " + action);
        }
    }

    private void validateStudentOwnership(Long studentId, String currentUserEmail) {
        if (currentUserEmail == null || studentId == null) {
            return;
        }
        User user = userRepository.findByEmail(currentUserEmail).orElse(null);
        if (user != null && user.getRole() == UserRole.ROLE_STUDENT) {
            Student student = studentRepository.findByUserEmail(currentUserEmail).orElse(null);
            if (student != null && !student.getId().equals(studentId)) {
                throw new AccessDeniedException("You are not authorized to view interviews for another student");
            }
        }
    }
}
