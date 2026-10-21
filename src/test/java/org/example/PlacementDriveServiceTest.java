package org.example;

import org.example.exception.CompanyNotFoundException;
import org.example.exception.PlacementDriveNotFoundException;
import org.example.exception.ValidationException;
import org.example.model.Company;
import org.example.model.DriveStatus;
import org.example.model.PlacementDrive;
import org.example.repository.InMemoryCompanyRepository;
import org.example.repository.InMemoryPlacementDriveRepository;
import org.example.service.PlacementDriveService;
import org.example.service.PlacementDriveServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PlacementDriveServiceTest {

    private InMemoryCompanyRepository companyRepository;
    private InMemoryPlacementDriveRepository driveRepository;
    private PlacementDriveService driveService;
    private Company savedCompany;

    @BeforeEach
    void setUp() {
        companyRepository = new InMemoryCompanyRepository();
        driveRepository = new InMemoryPlacementDriveRepository();
        driveService = new PlacementDriveServiceImpl(driveRepository, companyRepository);

        savedCompany = companyRepository.save(new Company("Cisco", "Networking", "Bangalore", "https://cisco.com"));
    }

    private PlacementDrive createValidDrive() {
        Set<String> branches = new HashSet<>(Arrays.asList("CSE", "IT", "ECE"));
        return new PlacementDrive(
                null,
                savedCompany.getId(),
                "Software Engineer",
                15.5,
                "Backend developer role working with Java and cloud services",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(20),
                7.5,
                0,
                branches
        );
    }

    @Test
    @DisplayName("Should create placement drive successfully when inputs are valid")
    void testCreateDriveSuccess() {
        PlacementDrive drive = createValidDrive();
        PlacementDrive created = driveService.createDrive(drive);

        assertNotNull(created.getId());
        assertEquals("Software Engineer", created.getJobRole());
        assertEquals(15.5, created.getPackageCTC());
        assertEquals(DriveStatus.SCHEDULED, created.getStatus());
        assertTrue(created.isBranchEligible("CSE"));
        assertTrue(created.isBranchEligible("ece")); // case-insensitivity check

        PlacementDrive fetched = driveService.getDriveById(created.getId());
        assertEquals(created.getId(), fetched.getId());
    }

    @Test
    @DisplayName("Should fail when company does not exist")
    void testCreateDriveNonExistentCompanyThrows() {
        PlacementDrive drive = createValidDrive();
        drive.setCompanyId("NON_EXISTENT_COMP");

        assertThrows(CompanyNotFoundException.class, () -> driveService.createDrive(drive));
    }

    @Test
    @DisplayName("Should reject negative package value")
    void testCreateDriveNegativePackageFails() {
        PlacementDrive drive = createValidDrive();
        ValidationException ex = assertThrows(ValidationException.class, () -> drive.setPackageCTC(-5.0));
        assertTrue(ex.getMessage().contains("cannot be negative"));
    }

    @Test
    @DisplayName("Should reject invalid CGPA (> 10 or < 0)")
    void testCreateDriveInvalidCGPAFails() {
        PlacementDrive drive = createValidDrive();
        ValidationException exHigh = assertThrows(ValidationException.class, () -> drive.setMinimumCGPA(10.5));
        assertTrue(exHigh.getMessage().contains("must be between 0.0 and 10.0"));

        ValidationException exLow = assertThrows(ValidationException.class, () -> drive.setMinimumCGPA(-1.0));
        assertTrue(exLow.getMessage().contains("must be between 0.0 and 10.0"));
    }

    @Test
    @DisplayName("Should reject negative allowed backlogs")
    void testCreateDriveNegativeBacklogsFails() {
        PlacementDrive drive = createValidDrive();
        ValidationException ex = assertThrows(ValidationException.class, () -> drive.setMaximumAllowedBacklogs(-2));
        assertTrue(ex.getMessage().contains("cannot be negative"));
    }

    @Test
    @DisplayName("Should reject invalid dates where deadline is after drive date")
    void testCreateDriveDeadlineAfterDriveDateFails() {
        PlacementDrive drive = createValidDrive();
        ValidationException ex = assertThrows(ValidationException.class, () ->
                drive.setDriveDate(drive.getApplicationDeadline().minusDays(1)));
        assertTrue(ex.getMessage().contains("cannot be after drive date"));
    }

    @Test
    @DisplayName("Should reject empty eligible branches")
    void testCreateDriveEmptyBranchesFails() {
        PlacementDrive drive = createValidDrive();
        drive.setEligibleBranches(new HashSet<>());
        ValidationException ex = assertThrows(ValidationException.class, () -> driveService.createDrive(drive));
        assertTrue(ex.getMessage().contains("At least one eligible branch"));
    }

    @Test
    @DisplayName("Should view drives by company")
    void testGetDrivesByCompany() {
        driveService.createDrive(createValidDrive());

        PlacementDrive secondDrive = createValidDrive();
        secondDrive.setJobRole("Cloud DevOps Engineer");
        driveService.createDrive(secondDrive);

        List<PlacementDrive> drives = driveService.getDrivesByCompany(savedCompany.getId());
        assertEquals(2, drives.size());
    }

    @Test
    @DisplayName("Should update drive details")
    void testUpdateDrive() {
        PlacementDrive drive = driveService.createDrive(createValidDrive());

        drive.setJobRole("Senior Software Engineer");
        drive.setPackageCTC(18.0);
        PlacementDrive updated = driveService.updateDrive(drive);

        assertEquals("Senior Software Engineer", updated.getJobRole());
        assertEquals(18.0, updated.getPackageCTC());
    }

    @Test
    @DisplayName("Should cancel drive and prevent modifications to cancelled drive")
    void testCancelDriveAndPreventUpdate() {
        PlacementDrive drive = driveService.createDrive(createValidDrive());

        PlacementDrive cancelled = driveService.cancelDrive(drive.getId());
        assertEquals(DriveStatus.CANCELLED, cancelled.getStatus());
        assertTrue(cancelled.isCancelled());

        cancelled.setJobRole("Hacked Role");
        ValidationException ex = assertThrows(ValidationException.class, () -> driveService.updateDrive(cancelled));
        assertTrue(ex.getMessage().contains("Cannot update a cancelled placement drive"));
    }

    @Test
    @DisplayName("Should throw exception when drive is not found")
    void testGetNonExistentDriveThrows() {
        assertThrows(PlacementDriveNotFoundException.class, () -> driveService.getDriveById("UNKNOWN_DRIVE"));
    }
}
