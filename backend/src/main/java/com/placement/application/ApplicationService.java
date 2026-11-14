package com.placement.application;

import com.placement.application.dto.ApplicationRequest;
import com.placement.application.dto.ApplicationResponse;
import com.placement.application.dto.ApplicationStatusUpdateRequest;
import com.placement.application.dto.ApplicationTimelineResponse;
import com.placement.application.dto.TimelineStageDto;
import com.placement.eligibility.EligibilityService;
import com.placement.eligibility.dto.EligibilityCheckResponse;
import com.placement.exception.DuplicateResourceException;
import com.placement.exception.ResourceNotFoundException;
import com.placement.notification.NotificationService;
import com.placement.placement.DriveStatus;
import com.placement.placement.PlacementDrive;
import com.placement.placement.PlacementRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationTimelineRepository applicationTimelineRepository;
    private final PlacementRepository placementRepository;
    private final StudentRepository studentRepository;
    private final EligibilityService eligibilityService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public ApplicationResponse createApplication(ApplicationRequest request) {
        return createApplication(request, null);
    }

    public ApplicationResponse createApplication(ApplicationRequest request, String currentUserEmail) {
        if (request == null || request.getStudentId() == null || request.getDriveId() == null) {
            throw new IllegalArgumentException("Student ID and Drive ID must not be null");
        }

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", request.getStudentId()));

        // Ownership check: If caller is a student, ensure they only apply for themselves
        if (currentUserEmail != null) {
            User user = userRepository.findByEmail(currentUserEmail).orElse(null);
            if (user != null && user.getRole() == UserRole.ROLE_STUDENT) {
                if (student.getUser() != null && !student.getUser().getEmail().equalsIgnoreCase(currentUserEmail)) {
                    throw new AccessDeniedException("You are not authorized to submit applications for another student");
                }
            }
        }

        PlacementDrive drive = placementRepository.findById(request.getDriveId())
                .orElseThrow(() -> new ResourceNotFoundException("PlacementDrive", "id", request.getDriveId()));

        // 1. Duplicate Application Validation
        if (applicationRepository.existsByStudentIdAndDriveId(request.getStudentId(), request.getDriveId())) {
            throw new DuplicateResourceException("PlacementApplication", "studentId and driveId",
                    request.getStudentId() + " / " + request.getDriveId());
        }

        // 2. Drive Status & Deadline Validation
        validateDriveAvailability(drive);

        // 3. Eligibility Validation
        EligibilityCheckResponse eligibility = eligibilityService.evaluateEligibility(student, drive);
        if (!eligibility.isEligible()) {
            throw new IllegalStateException("Student is not eligible for this drive: " + String.join("; ", eligibility.getReasons()));
        }

        PlacementApplication application = new PlacementApplication();
        application.setStudentId(student.getId());
        application.setDriveId(drive.getId());
        application.setStatus(ApplicationStatus.APPLIED);

        PlacementApplication savedApplication = applicationRepository.save(application);
        log.info("Student {} successfully applied for Drive {}", student.getId(), drive.getId());

        // Record initial timeline event
        ApplicationTimelineEvent initialEvent = ApplicationTimelineEvent.builder()
                .applicationId(savedApplication.getId())
                .fromStatus(null)
                .toStatus(ApplicationStatus.APPLIED)
                .title("Application Submitted")
                .description("Student submitted application for placement drive '" + drive.getJobTitle() + "'")
                .actorEmail(currentUserEmail)
                .createdAt(LocalDateTime.now())
                .build();
        applicationTimelineRepository.save(initialEvent);

        notificationService.createNotification(
                student.getId(),
                "Your application for placement drive '" + drive.getJobTitle() + "' has been submitted successfully."
        );

        return ApplicationResponse.fromEntity(savedApplication);
    }

    private void validateDriveAvailability(PlacementDrive drive) {
        if (drive.getStatus() == DriveStatus.CANCELLED) {
            throw new IllegalStateException("Cannot apply: Placement drive is cancelled");
        }
        if (drive.getStatus() == DriveStatus.COMPLETED) {
            throw new IllegalStateException("Cannot apply: Placement drive has already completed");
        }

        LocalDate today = LocalDate.now();
        if (drive.getApplicationStartDate() != null && today.isBefore(drive.getApplicationStartDate())) {
            throw new IllegalStateException("Application window for this drive has not opened yet (starts on " + drive.getApplicationStartDate() + ")");
        }
        if (drive.getApplicationEndDate() != null && today.isAfter(drive.getApplicationEndDate())) {
            throw new IllegalStateException("Application deadline has passed for this drive (ended on " + drive.getApplicationEndDate() + ")");
        }
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByStudent(Long studentId) {
        return getApplicationsByStudent(studentId, null);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByStudent(Long studentId, String currentUserEmail) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID must not be null");
        }
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student", "id", studentId);
        }

        // Ownership check
        if (currentUserEmail != null) {
            User user = userRepository.findByEmail(currentUserEmail).orElse(null);
            if (user != null && user.getRole() == UserRole.ROLE_STUDENT) {
                Student student = studentRepository.findByUserEmail(currentUserEmail).orElse(null);
                if (student != null && !student.getId().equals(studentId)) {
                    throw new AccessDeniedException("You are not authorized to view another student's applications");
                }
            }
        }

        return applicationRepository.findByStudentId(studentId)
                .stream()
                .map(ApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByDrive(Long driveId) {
        if (driveId == null) {
            throw new IllegalArgumentException("Drive ID must not be null");
        }
        if (!placementRepository.existsById(driveId)) {
            throw new ResourceNotFoundException("PlacementDrive", "id", driveId);
        }
        return applicationRepository.findByDriveId(driveId)
                .stream()
                .map(ApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long id) {
        return getApplicationById(id, null);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long id, String currentUserEmail) {
        PlacementApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlacementApplication", "id", id));

        if (currentUserEmail != null) {
            User user = userRepository.findByEmail(currentUserEmail).orElse(null);
            if (user != null && user.getRole() == UserRole.ROLE_STUDENT) {
                Student student = studentRepository.findByUserEmail(currentUserEmail).orElse(null);
                if (student != null && !student.getId().equals(application.getStudentId())) {
                    throw new AccessDeniedException("You are not authorized to view this application");
                }
            }
        }

        return ApplicationResponse.fromEntity(application);
    }

    public ApplicationResponse updateStatus(Long id, ApplicationStatusUpdateRequest request) {
        return updateStatus(id, request, null);
    }

    public ApplicationResponse updateStatus(Long id, ApplicationStatusUpdateRequest request, String currentUserEmail) {
        if (request == null || request.getStatus() == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        PlacementApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlacementApplication", "id", id));

        ApplicationStatus currentStatus = application.getStatus();
        ApplicationStatus newStatus = request.getStatus();

        if (currentStatus == newStatus) {
            return ApplicationResponse.fromEntity(application);
        }

        // Validate Status Transition Rules
        validateStatusTransition(currentStatus, newStatus);

        application.setStatus(newStatus);
        PlacementApplication updatedApplication = applicationRepository.save(application);
        log.info("Updated application ID: {} status from {} to {}", id, currentStatus, newStatus);

        // Record timeline event
        String title = "Status updated to " + newStatus;
        String desc = "Application advanced from " + currentStatus + " to " + newStatus;
        if (newStatus == ApplicationStatus.REJECTED) {
            title = "Application Rejected";
            desc = "Application was not selected to proceed further.";
        } else if (newStatus == ApplicationStatus.SELECTED) {
            title = "Offer Extended / Selected";
            desc = "Congratulations! Student selected for the role.";
        }

        ApplicationTimelineEvent event = ApplicationTimelineEvent.builder()
                .applicationId(id)
                .fromStatus(currentStatus)
                .toStatus(newStatus)
                .title(title)
                .description(desc)
                .actorEmail(currentUserEmail)
                .createdAt(LocalDateTime.now())
                .build();
        applicationTimelineRepository.save(event);

        notificationService.createNotification(
                updatedApplication.getStudentId(),
                "Your application status for drive " + updatedApplication.getDriveId() + " has been updated to: " + updatedApplication.getStatus()
        );

        return ApplicationResponse.fromEntity(updatedApplication);
    }

    @Transactional(readOnly = true)
    public ApplicationTimelineResponse getApplicationTimeline(Long applicationId, String currentUserEmail) {
        PlacementApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("PlacementApplication", "id", applicationId));

        // Ownership check
        if (currentUserEmail != null) {
            User user = userRepository.findByEmail(currentUserEmail).orElse(null);
            if (user != null && user.getRole() == UserRole.ROLE_STUDENT) {
                Student student = studentRepository.findByUserEmail(currentUserEmail).orElse(null);
                if (student != null && !student.getId().equals(application.getStudentId())) {
                    throw new AccessDeniedException("You are not authorized to view the timeline for this application");
                }
            }
        }

        PlacementDrive drive = placementRepository.findById(application.getDriveId()).orElse(null);
        List<ApplicationTimelineEvent> events = applicationTimelineRepository.findByApplicationIdOrderByCreatedAtAsc(applicationId);

        Map<ApplicationStatus, LocalDateTime> statusReachedMap = events.stream()
                .collect(Collectors.toMap(
                        ApplicationTimelineEvent::getToStatus,
                        ApplicationTimelineEvent::getCreatedAt,
                        (existing, newer) -> newer
                ));

        ApplicationStatus current = application.getStatus();

        // Build standard sequential stages
        List<TimelineStageDto> stages = new ArrayList<>();

        stages.add(buildStage(ApplicationStatus.APPLIED, "Applied", "Application submitted",
                statusReachedMap.get(ApplicationStatus.APPLIED), current, ApplicationStatus.APPLIED));

        stages.add(buildStage(ApplicationStatus.SHORTLISTED, "Shortlisted", "Profile reviewed and shortlisted",
                statusReachedMap.get(ApplicationStatus.SHORTLISTED), current, ApplicationStatus.SHORTLISTED));

        stages.add(buildStage(ApplicationStatus.INTERVIEW_SCHEDULED, "Interview", "Interview round scheduled",
                statusReachedMap.get(ApplicationStatus.INTERVIEW_SCHEDULED), current, ApplicationStatus.INTERVIEW_SCHEDULED));

        if (current == ApplicationStatus.REJECTED) {
            stages.add(TimelineStageDto.builder()
                    .status(ApplicationStatus.REJECTED)
                    .label("Rejected")
                    .description("Application not selected")
                    .completed(true)
                    .current(true)
                    .reachedAt(statusReachedMap.get(ApplicationStatus.REJECTED))
                    .build());
        } else {
            stages.add(buildStage(ApplicationStatus.SELECTED, "Selected", "Final selection / offer received",
                    statusReachedMap.get(ApplicationStatus.SELECTED), current, ApplicationStatus.SELECTED));
        }

        return ApplicationTimelineResponse.builder()
                .applicationId(application.getId())
                .studentId(application.getStudentId())
                .driveId(application.getDriveId())
                .companyName(drive != null && drive.getCompany() != null ? drive.getCompany().getName() : null)
                .jobTitle(drive != null ? drive.getJobTitle() : null)
                .currentStatus(application.getStatus())
                .appliedAt(application.getCreatedAt())
                .lastUpdatedAt(application.getUpdatedAt())
                .stages(stages)
                .eventHistory(events)
                .build();
    }

    private TimelineStageDto buildStage(ApplicationStatus stageStatus, String label, String description,
                                        LocalDateTime reachedAt, ApplicationStatus currentStatus, ApplicationStatus thisStage) {
        boolean isCurrent = (currentStatus == thisStage);
        boolean isCompleted = reachedAt != null || isPastStage(currentStatus, thisStage);

        return TimelineStageDto.builder()
                .status(stageStatus)
                .label(label)
                .description(description)
                .completed(isCompleted)
                .current(isCurrent)
                .reachedAt(reachedAt)
                .build();
    }

    private boolean isPastStage(ApplicationStatus current, ApplicationStatus check) {
        int currentRank = getStatusRank(current);
        int checkRank = getStatusRank(check);
        return currentRank > checkRank;
    }

    private int getStatusRank(ApplicationStatus status) {
        return switch (status) {
            case APPLIED -> 1;
            case SHORTLISTED -> 2;
            case INTERVIEW_SCHEDULED -> 3;
            case SELECTED -> 4;
            case REJECTED -> 5;
        };
    }

    private void validateStatusTransition(ApplicationStatus current, ApplicationStatus next) {
        if (current == ApplicationStatus.REJECTED) {
            throw new IllegalStateException("Cannot change status of an application that is already REJECTED");
        }
        if (current == ApplicationStatus.SELECTED) {
            throw new IllegalStateException("Cannot change status of an application that is already SELECTED");
        }

        boolean valid = switch (current) {
            case APPLIED -> (next == ApplicationStatus.SHORTLISTED || next == ApplicationStatus.REJECTED);
            case SHORTLISTED -> (next == ApplicationStatus.INTERVIEW_SCHEDULED || next == ApplicationStatus.SELECTED || next == ApplicationStatus.REJECTED);
            case INTERVIEW_SCHEDULED -> (next == ApplicationStatus.SHORTLISTED || next == ApplicationStatus.SELECTED || next == ApplicationStatus.REJECTED);
            default -> false;
        };

        if (!valid) {
            throw new IllegalStateException(String.format("Invalid status transition from '%s' to '%s'", current, next));
        }
    }
}
