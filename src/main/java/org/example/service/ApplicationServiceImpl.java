package org.example.service;

import org.example.exception.*;
import org.example.model.Application;
import org.example.model.ApplicationStatus;
import org.example.model.EligibilityResult;
import org.example.model.PlacementDrive;
import org.example.model.Student;
import org.example.repository.ApplicationRepository;
import org.example.repository.PlacementDriveRepository;
import org.example.repository.StudentRepository;
import org.example.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of ApplicationService.
 */
public class ApplicationServiceImpl implements ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final PlacementDriveRepository driveRepository;
    private final StudentRepository studentRepository;
    private final StudentEligibilityService eligibilityService;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  PlacementDriveRepository driveRepository,
                                  StudentRepository studentRepository,
                                  StudentEligibilityService eligibilityService) {
        this.applicationRepository = Objects.requireNonNull(applicationRepository, "ApplicationRepository must not be null");
        this.driveRepository = Objects.requireNonNull(driveRepository, "PlacementDriveRepository must not be null");
        this.studentRepository = Objects.requireNonNull(studentRepository, "StudentRepository must not be null");
        this.eligibilityService = Objects.requireNonNull(eligibilityService, "StudentEligibilityService must not be null");
    }

    @Override
    public Application applyForDrive(String studentId, String driveId) {
        return applyForDrive(studentId, driveId, LocalDate.now());
    }

    @Override
    public Application applyForDrive(String studentId, String driveId, LocalDate applicationDate) {
        ValidationUtil.requireNonBlank(studentId, "Student ID");
        ValidationUtil.requireNonBlank(driveId, "Drive ID");
        LocalDate date = applicationDate != null ? applicationDate : LocalDate.now();

        // Check student exists
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Cannot apply. Student not found with ID: " + studentId));

        // Check drive exists
        PlacementDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new PlacementDriveNotFoundException("Cannot apply. Drive not found with ID: " + driveId));

        // Check if drive is cancelled
        if (drive.isCancelled()) {
            throw new ValidationException("Cannot apply to a cancelled placement drive (ID: " + driveId + ")");
        }

        // Validate application deadline
        if (date.isAfter(drive.getApplicationDeadline())) {
            throw new ValidationException(String.format("Application deadline (%s) has passed. Attempted application date: %s",
                    drive.getApplicationDeadline(), date));
        }

        // Prevent duplicate application
        if (applicationRepository.existsByDriveIdAndStudentId(driveId, studentId)) {
            throw new DuplicateApplicationException(String.format("Student '%s' has already applied to placement drive '%s'",
                    studentId, driveId));
        }

        // Check eligibility
        EligibilityResult eligibility = eligibilityService.checkEligibility(student, drive);
        if (!eligibility.isEligible()) {
            throw new IneligibleStudentException(
                    "Student does not meet eligibility requirements: " + eligibility.getFormattedReason(),
                    eligibility.getReasons()
            );
        }

        Application application = new Application();
        application.setDriveId(driveId);
        application.setStudentId(studentId);
        application.setAppliedDate(date);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setRemarks("Application submitted successfully");

        return applicationRepository.save(application);
    }

    @Override
    public Application updateApplicationStatus(String applicationId, ApplicationStatus newStatus, String remarks) {
        ValidationUtil.requireNonBlank(applicationId, "Application ID");
        if (newStatus == null) {
            throw new ValidationException("New application status cannot be null");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + applicationId));

        application.setStatus(newStatus);
        if (remarks != null) {
            application.setRemarks(remarks.trim());
        }

        return applicationRepository.save(application);
    }

    @Override
    public Application getApplicationById(String applicationId) {
        ValidationUtil.requireNonBlank(applicationId, "Application ID");
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found with ID: " + applicationId));
    }

    @Override
    public List<Application> getApplicationsByStudent(String studentId) {
        ValidationUtil.requireNonBlank(studentId, "Student ID");
        return applicationRepository.findByStudentId(studentId);
    }

    @Override
    public List<Application> getApplicationsByDrive(String driveId) {
        ValidationUtil.requireNonBlank(driveId, "Drive ID");
        return applicationRepository.findByDriveId(driveId);
    }

    @Override
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }
}
