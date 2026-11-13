package com.placement.reminder;

import com.placement.application.ApplicationRepository;
import com.placement.eligibility.EligibilityService;
import com.placement.hackathon.Hackathon;
import com.placement.hackathon.HackathonRegistrationRepository;
import com.placement.hackathon.HackathonRepository;
import com.placement.hackathon.HackathonStatus;
import com.placement.notification.NotificationService;
import com.placement.placement.DriveStatus;
import com.placement.placement.PlacementDrive;
import com.placement.placement.PlacementRepository;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
import com.placement.util.YearUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeadlineReminderService {

    private final PlacementRepository placementRepository;
    private final HackathonRepository hackathonRepository;
    private final StudentRepository studentRepository;
    private final ApplicationRepository applicationRepository;
    private final HackathonRegistrationRepository hackathonRegistrationRepository;
    private final EligibilityService eligibilityService;
    private final NotificationService notificationService;

    /**
     * Automated cron scheduled daily at 9:00 AM.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void scheduledDailyReminderRun() {
        log.info("Starting automated daily deadline reminder check...");
        ReminderExecutionSummary summary = executeDeadlineReminders(2);
        log.info("Daily deadline reminder completed: {} total reminders sent.", summary.getTotalRemindersSent());
    }

    /**
     * Executes reminder scan for deadlines closing within {@code daysThreshold} days.
     */
    @Transactional
    public ReminderExecutionSummary executeDeadlineReminders(int daysThreshold) {
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(daysThreshold);

        List<String> details = new ArrayList<>();
        int driveReminders = 0;
        int hackathonReminders = 0;

        List<Student> allStudents = studentRepository.findAll();

        // 1. Placement Drives closing soon
        List<PlacementDrive> drives = placementRepository.findAll().stream()
                .filter(d -> d.getStatus() == DriveStatus.UPCOMING)
                .filter(d -> d.getApplicationEndDate() != null)
                .filter(d -> !d.getApplicationEndDate().isBefore(today) && !d.getApplicationEndDate().isAfter(cutoff))
                .toList();

        for (PlacementDrive drive : drives) {
            long daysLeft = ChronoUnit.DAYS.between(today, drive.getApplicationEndDate());
            String companyName = (drive.getCompany() != null) ? drive.getCompany().getName() : "Company";
            int countForDrive = 0;

            for (Student student : allStudents) {
                // If student has already applied, skip
                if (applicationRepository.existsByStudentIdAndDriveId(student.getId(), drive.getId())) {
                    continue;
                }
                // Check if student is eligible
                if (!eligibilityService.evaluateEligibility(student, drive).isEligible()) {
                    continue;
                }

                String urgency = daysLeft == 0 ? "TODAY" : ("in " + daysLeft + " day" + (daysLeft > 1 ? "s" : ""));
                String msg = String.format("URGENT REMINDER: Application deadline for '%s' (%s) closes %s (%s)! Apply now.",
                        drive.getJobTitle(), companyName, urgency, drive.getApplicationEndDate());

                notificationService.createNotification(student.getId(), msg);
                countForDrive++;
                driveReminders++;
            }

            details.add(String.format("Drive '%s' (%s): %d reminders sent", drive.getJobTitle(), companyName, countForDrive));
        }

        // 2. Hackathons closing soon
        List<Hackathon> hackathons = hackathonRepository.findAll().stream()
                .filter(Hackathon::isActive)
                .filter(h -> h.getStatus() == HackathonStatus.UPCOMING)
                .filter(h -> h.getRegistrationDeadline() != null)
                .filter(h -> !h.getRegistrationDeadline().isBefore(today) && !h.getRegistrationDeadline().isAfter(cutoff))
                .toList();

        for (Hackathon hackathon : hackathons) {
            long daysLeft = ChronoUnit.DAYS.between(today, hackathon.getRegistrationDeadline());
            int countForHackathon = 0;

            for (Student student : allStudents) {
                if (hackathonRegistrationRepository.existsByHackathonIdAndStudentId(hackathon.getId(), student.getId())) {
                    continue;
                }
                if (!YearUtils.matchesYear(hackathon.getTargetYears(), student.getAcademicYear())) {
                    continue;
                }

                String urgency = daysLeft == 0 ? "TODAY" : ("in " + daysLeft + " day" + (daysLeft > 1 ? "s" : ""));
                String msg = String.format("HACKATHON REMINDER: Registration for '%s' by %s closes %s (%s)!",
                        hackathon.getTitle(), hackathon.getOrganizer(), urgency, hackathon.getRegistrationDeadline());

                notificationService.createNotification(student.getId(), msg);
                countForHackathon++;
                hackathonReminders++;
            }

            details.add(String.format("Hackathon '%s': %d reminders sent", hackathon.getTitle(), countForHackathon));
        }

        return ReminderExecutionSummary.builder()
                .drivesEvaluated(drives.size())
                .driveRemindersSent(driveReminders)
                .hackathonsEvaluated(hackathons.size())
                .hackathonRemindersSent(hackathonReminders)
                .totalRemindersSent(driveReminders + hackathonReminders)
                .executedAt(LocalDateTime.now())
                .details(details)
                .build();
    }

    /**
     * Returns list of all upcoming deadlines closing within {@code daysThreshold} days.
     */
    @Transactional(readOnly = true)
    public List<UpcomingDeadlineDto> getUpcomingDeadlines(int daysThreshold) {
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(daysThreshold);
        List<UpcomingDeadlineDto> results = new ArrayList<>();

        placementRepository.findAll().stream()
                .filter(d -> d.getStatus() == DriveStatus.UPCOMING)
                .filter(d -> d.getApplicationEndDate() != null)
                .filter(d -> !d.getApplicationEndDate().isBefore(today) && !d.getApplicationEndDate().isAfter(cutoff))
                .forEach(d -> results.add(UpcomingDeadlineDto.builder()
                        .type("PLACEMENT_DRIVE")
                        .id(d.getId())
                        .title(d.getJobTitle())
                        .organization(d.getCompany() != null ? d.getCompany().getName() : "N/A")
                        .deadline(d.getApplicationEndDate())
                        .daysRemaining(ChronoUnit.DAYS.between(today, d.getApplicationEndDate()))
                        .status(d.getStatus().name())
                        .build()));

        hackathonRepository.findAll().stream()
                .filter(Hackathon::isActive)
                .filter(h -> h.getStatus() == HackathonStatus.UPCOMING)
                .filter(h -> h.getRegistrationDeadline() != null)
                .filter(h -> !h.getRegistrationDeadline().isBefore(today) && !h.getRegistrationDeadline().isAfter(cutoff))
                .forEach(h -> results.add(UpcomingDeadlineDto.builder()
                        .type("HACKATHON")
                        .id(h.getId())
                        .title(h.getTitle())
                        .organization(h.getOrganizer())
                        .deadline(h.getRegistrationDeadline())
                        .daysRemaining(ChronoUnit.DAYS.between(today, h.getRegistrationDeadline()))
                        .status(h.getStatus().name())
                        .build()));

        results.sort((a, b) -> a.getDeadline().compareTo(b.getDeadline()));
        return results;
    }
}

