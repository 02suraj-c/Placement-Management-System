package com.placement.student;

import com.placement.application.ApplicationRepository;
import com.placement.application.PlacementApplication;
import com.placement.application.ApplicationStatus;
import com.placement.eligibility.EligibilityService;
import com.placement.exception.ResourceNotFoundException;
import com.placement.hackathon.HackathonRegistrationRepository;
import com.placement.interview.InterviewRound;
import com.placement.interview.InterviewRoundRepository;
import com.placement.notification.NotificationRepository;
import com.placement.notification.SystemNotification;
import com.placement.placement.DriveStatus;
import com.placement.placement.PlacementDrive;
import com.placement.placement.PlacementRepository;
import com.placement.reminder.DeadlineReminderService;
import com.placement.reminder.UpcomingDeadlineDto;
import com.placement.student.dto.StudentDashboardResponse;
import com.placement.student.dto.StudentProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aggregates all student-relevant data into a single dashboard snapshot.
 *
 * <p>All reads are done in a single transaction; every sub-query is a cheap
 * in-memory filter or a targeted JPA call so the method stays fast.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentDashboardService {

    private final StudentRepository studentRepository;
    private final StudentService studentService;
    private final ApplicationRepository applicationRepository;
    private final InterviewRoundRepository interviewRoundRepository;
    private final HackathonRegistrationRepository hackathonRegistrationRepository;
    private final NotificationRepository notificationRepository;
    private final PlacementRepository placementRepository;
    private final EligibilityService eligibilityService;
    private final DeadlineReminderService deadlineReminderService;

    /**
     * Builds and returns the full dashboard snapshot for the authenticated student.
     *
     * @param userEmail the email extracted from the JWT of the currently authenticated user
     * @return a fully-populated {@link StudentDashboardResponse}
     * @throws ResourceNotFoundException if no student profile exists for the given email
     */
    @Transactional(readOnly = true)
    public StudentDashboardResponse getStudentDashboard(String userEmail) {
        log.debug("Building dashboard snapshot for user: {}", userEmail);

        // ── 1. Load student ───────────────────────────────────────────────
        Student student = studentRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student profile not found for user: " + userEmail));

        StudentProfileResponse profile = studentService.mapToResponse(student);

        // ── 2. Applications ───────────────────────────────────────────────
        List<PlacementApplication> applications = applicationRepository.findByStudentId(student.getId());
        int totalApplications = applications.size();

        Map<String, Long> applicationsByStatus = applications.stream()
                .collect(Collectors.groupingBy(
                        app -> app.getStatus().name(),
                        Collectors.counting()
                ));

        // ── 3. Interviews ─────────────────────────────────────────────────
        List<Long> applicationIds = applications.stream()
                .map(PlacementApplication::getId)
                .toList();

        List<InterviewRound> allInterviews = applicationIds.isEmpty()
                ? List.of()
                : interviewRoundRepository.findByApplicationIdIn(applicationIds);

        // Upcoming = scheduled in the future
        List<InterviewRound> upcomingInterviews = allInterviews.stream()
                .filter(r -> r.getInterviewDateTime() != null
                        && r.getInterviewDateTime().isAfter(LocalDateTime.now()))
                .sorted((a, b) -> a.getInterviewDateTime().compareTo(b.getInterviewDateTime()))
                .toList();

        // ── 4. Notifications ──────────────────────────────────────────────
        List<SystemNotification> allNotifications =
                notificationRepository.findByStudentIdOrderByCreatedAtDesc(student.getId());

        List<SystemNotification> recentNotifications = allNotifications.stream()
                .limit(5)
                .toList();

        long unreadCount = notificationRepository.countByStudentIdAndIsReadFalse(student.getId());

        // ── 5. Hackathon registrations ────────────────────────────────────
        long hackathonCount =
                hackathonRegistrationRepository.findByStudentIdOrderByRegisteredAtDesc(student.getId()).size();

        // ── 6. Upcoming deadlines (next 7 days) ───────────────────────────
        List<UpcomingDeadlineDto> upcomingDeadlines = deadlineReminderService.getUpcomingDeadlines(7);

        // ── 7. Eligible drives count ──────────────────────────────────────
        List<PlacementDrive> activeDrives = placementRepository.findAll().stream()
                .filter(d -> d.getStatus() == DriveStatus.UPCOMING
                        || d.getStatus() == DriveStatus.ONGOING)
                .toList();

        long eligibleDrivesCount = activeDrives.stream()
                .filter(drive -> eligibilityService.evaluateEligibility(student, drive).isEligible())
                .count();

        log.debug("Dashboard built for student {} — {} applications, {} upcoming interviews, {} eligible drives",
                student.getId(), totalApplications, upcomingInterviews.size(), eligibleDrivesCount);

        return StudentDashboardResponse.builder()
                .studentProfile(profile)
                .totalApplications(totalApplications)
                .applicationsByStatus(applicationsByStatus)
                .upcomingInterviews(upcomingInterviews)
                .totalInterviews(allInterviews.size())
                .recentNotifications(recentNotifications)
                .unreadNotificationsCount(unreadCount)
                .hackathonRegistrationsCount(hackathonCount)
                .upcomingDeadlines(upcomingDeadlines)
                .eligibleDrivesCount(eligibleDrivesCount)
                .build();
    }
}
