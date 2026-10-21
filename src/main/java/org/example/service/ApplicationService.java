package org.example.service;

import org.example.model.Application;
import org.example.model.ApplicationStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Service managing student applications to placement drives.
 */
public interface ApplicationService {
    Application applyForDrive(String studentId, String driveId);
    Application applyForDrive(String studentId, String driveId, LocalDate applicationDate);
    Application updateApplicationStatus(String applicationId, ApplicationStatus newStatus, String remarks);
    Application getApplicationById(String applicationId);
    List<Application> getApplicationsByStudent(String studentId);
    List<Application> getApplicationsByDrive(String driveId);
    List<Application> getAllApplications();
}
