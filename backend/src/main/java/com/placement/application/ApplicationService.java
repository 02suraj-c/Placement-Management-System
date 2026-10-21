package com.placement.application;

import com.placement.application.dto.ApplicationRequest;
import com.placement.application.dto.ApplicationResponse;
import com.placement.application.dto.ApplicationStatusUpdateRequest;
import com.placement.notification.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final NotificationService notificationService;

    public ApplicationService(ApplicationRepository applicationRepository, NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.notificationService = notificationService;
    }

    public ApplicationResponse createApplication(ApplicationRequest request) {
        if (request == null || request.getStudentId() == null || request.getDriveId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student ID and Drive ID must not be null");
        }

        if (applicationRepository.existsByStudentIdAndDriveId(request.getStudentId(), request.getDriveId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Application already exists for student " + request.getStudentId() + " and drive " + request.getDriveId());
        }

        PlacementApplication application = new PlacementApplication();
        application.setStudentId(request.getStudentId());
        application.setDriveId(request.getDriveId());
        application.setStatus(ApplicationStatus.APPLIED);

        PlacementApplication savedApplication = applicationRepository.save(application);
        return ApplicationResponse.fromEntity(savedApplication);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByStudent(Long studentId) {
        return applicationRepository.findByStudentId(studentId)
                .stream()
                .map(ApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByDrive(Long driveId) {
        return applicationRepository.findByDriveId(driveId)
                .stream()
                .map(ApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long id) {
        PlacementApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> {
                    // TODO: switch to ResourceNotFoundException
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found with id: " + id);
                });
        return ApplicationResponse.fromEntity(application);
    }

    public ApplicationResponse updateStatus(Long id, ApplicationStatusUpdateRequest request) {
        if (request == null || request.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status cannot be null");
        }

        PlacementApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> {
                    // TODO: switch to ResourceNotFoundException
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found with id: " + id);
                });

        application.setStatus(request.getStatus());
        PlacementApplication updatedApplication = applicationRepository.save(application);

        notificationService.createNotification(
                updatedApplication.getStudentId(),
                "Your application status for drive " + updatedApplication.getDriveId() + " has been updated to: " + updatedApplication.getStatus()
        );

        return ApplicationResponse.fromEntity(updatedApplication);
    }
}
