package org.example;

import org.example.exception.ApplicationNotFoundException;
import org.example.exception.DuplicateApplicationException;
import org.example.exception.IneligibleStudentException;
import org.example.exception.ValidationException;
import org.example.model.*;
import org.example.repository.*;
import org.example.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationServiceTest {

    private InMemoryApplicationRepository applicationRepository;
    private InMemoryPlacementDriveRepository driveRepository;
    private InMemoryStudentRepository studentRepository;
    private InMemoryCompanyRepository companyRepository;
    private StudentEligibilityService eligibilityService;
    private ApplicationService applicationService;

    private Student eligibleStudent;
    private Student ineligibleStudent;
    private PlacementDrive activeDrive;

    @BeforeEach
    void setUp() {
        applicationRepository = new InMemoryApplicationRepository();
        driveRepository = new InMemoryPlacementDriveRepository();
        studentRepository = new InMemoryStudentRepository();
        companyRepository = new InMemoryCompanyRepository();

        eligibilityService = new StudentEligibilityServiceImpl(studentRepository, driveRepository);
        applicationService = new ApplicationServiceImpl(applicationRepository, driveRepository, studentRepository, eligibilityService);

        Company company = companyRepository.save(new Company("COMP-1", "TCS", "IT", "Mumbai", "https://tcs.com"));

        eligibleStudent = studentRepository.save(new Student("STU-1", "Rohit", "rohit@example.com", "CSE", 8.2, 0));
        ineligibleStudent = studentRepository.save(new Student("STU-2", "Mohit", "mohit@example.com", "MECH", 6.0, 2));

        activeDrive = driveRepository.save(new PlacementDrive(
                "DRIVE-1",
                company.getId(),
                "Assistant System Engineer",
                7.0,
                "Entry level software role",
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(15),
                7.0,
                0,
                new HashSet<>(Arrays.asList("CSE", "IT"))
        ));
    }

    @Test
    @DisplayName("Should allow eligible student to apply successfully before deadline")
    void testApplySuccess() {
        Application app = applicationService.applyForDrive(eligibleStudent.getId(), activeDrive.getId(), LocalDate.now());

        assertNotNull(app.getId());
        assertEquals(eligibleStudent.getId(), app.getStudentId());
        assertEquals(activeDrive.getId(), app.getDriveId());
        assertEquals(ApplicationStatus.APPLIED, app.getStatus());
    }

    @Test
    @DisplayName("Should prevent duplicate applications to the same drive")
    void testPreventDuplicateApplication() {
        applicationService.applyForDrive(eligibleStudent.getId(), activeDrive.getId(), LocalDate.now());

        DuplicateApplicationException ex = assertThrows(DuplicateApplicationException.class, () ->
                applicationService.applyForDrive(eligibleStudent.getId(), activeDrive.getId(), LocalDate.now()));

        assertTrue(ex.getMessage().contains("already applied"));
    }

    @Test
    @DisplayName("Should prevent application after application deadline")
    void testPreventApplicationAfterDeadline() {
        LocalDate afterDeadline = activeDrive.getApplicationDeadline().plusDays(1);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                applicationService.applyForDrive(eligibleStudent.getId(), activeDrive.getId(), afterDeadline));

        assertTrue(ex.getMessage().contains("Application deadline"));
    }

    @Test
    @DisplayName("Should prevent ineligible student from applying")
    void testIneligibleStudentCannotApply() {
        IneligibleStudentException ex = assertThrows(IneligibleStudentException.class, () ->
                applicationService.applyForDrive(ineligibleStudent.getId(), activeDrive.getId(), LocalDate.now()));

        assertTrue(ex.getMessage().contains("Student does not meet eligibility requirements"));
        assertFalse(ex.getReasons().isEmpty());
    }

    @Test
    @DisplayName("Should prevent applying to a cancelled drive")
    void testCannotApplyToCancelledDrive() {
        activeDrive.setStatus(DriveStatus.CANCELLED);
        driveRepository.save(activeDrive);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                applicationService.applyForDrive(eligibleStudent.getId(), activeDrive.getId(), LocalDate.now()));

        assertTrue(ex.getMessage().contains("Cannot apply to a cancelled placement drive"));
    }

    @Test
    @DisplayName("Should view applications by student")
    void testViewApplicationsByStudent() {
        applicationService.applyForDrive(eligibleStudent.getId(), activeDrive.getId(), LocalDate.now());

        List<Application> apps = applicationService.getApplicationsByStudent(eligibleStudent.getId());
        assertEquals(1, apps.size());
        assertEquals(activeDrive.getId(), apps.get(0).getDriveId());
    }

    @Test
    @DisplayName("Should view applications by placement drive")
    void testViewApplicationsByDrive() {
        Student eligibleStudent2 = studentRepository.save(new Student("STU-3", "Simran", "simran@example.com", "IT", 8.8, 0));

        applicationService.applyForDrive(eligibleStudent.getId(), activeDrive.getId(), LocalDate.now());
        applicationService.applyForDrive(eligibleStudent2.getId(), activeDrive.getId(), LocalDate.now());

        List<Application> apps = applicationService.getApplicationsByDrive(activeDrive.getId());
        assertEquals(2, apps.size());
    }

    @Test
    @DisplayName("Should update application status (SHORTLISTED, SELECTED, REJECTED)")
    void testUpdateApplicationStatus() {
        Application app = applicationService.applyForDrive(eligibleStudent.getId(), activeDrive.getId(), LocalDate.now());

        Application updated = applicationService.updateApplicationStatus(app.getId(), ApplicationStatus.SHORTLISTED, "Shortlisted for Round 1");
        assertEquals(ApplicationStatus.SHORTLISTED, updated.getStatus());
        assertEquals("Shortlisted for Round 1", updated.getRemarks());

        Application finalUpdate = applicationService.updateApplicationStatus(app.getId(), ApplicationStatus.SELECTED, "Offer letter released");
        assertEquals(ApplicationStatus.SELECTED, finalUpdate.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when application is not found")
    void testGetNonExistentApplicationThrows() {
        assertThrows(ApplicationNotFoundException.class, () -> applicationService.getApplicationById("NON_EXISTENT_APP"));
    }
}
