package com.placement.placement;

import com.placement.company.Company;
import com.placement.company.CompanyRepository;
import com.placement.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlacementServiceTest {

    @Mock
    private PlacementRepository placementRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private PlacementService placementService;

    private Company sampleCompany;
    private PlacementDrive sampleDrive;

    @BeforeEach
    void setUp() {
        sampleCompany = new Company(
                1L,
                "Acme Corp",
                "Leading Tech Solutions",
                "IT",
                "https://acme.example.com",
                "Bangalore",
                "careers@acme.example.com",
                "+91 9876543210",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        sampleDrive = new PlacementDrive();
        sampleDrive.setId(10L);
        sampleDrive.setCompany(sampleCompany);
        sampleDrive.setJobTitle("Software Development Engineer");
        sampleDrive.setJobDescription("Develop and maintain scalable backend services.");
        sampleDrive.setJobLocation("Bangalore");
        sampleDrive.setSalaryPackage(14.5);
        sampleDrive.setEmploymentType("FULL_TIME");
        sampleDrive.setApplicationStartDate(LocalDate.now());
        sampleDrive.setApplicationEndDate(LocalDate.now().plusDays(10));
        sampleDrive.setDriveDate(LocalDate.now().plusDays(20));
        sampleDrive.setVacancies(5);
        sampleDrive.setStatus(DriveStatus.UPCOMING);
    }

    @Test
    @DisplayName("Successfully creates placement drive when all data and company are valid")
    void createPlacementDrive_Success() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(sampleCompany));
        when(placementRepository.save(any(PlacementDrive.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlacementDrive created = placementService.createPlacementDrive(sampleDrive);

        assertNotNull(created);
        assertEquals(DriveStatus.UPCOMING, created.getStatus());
        assertEquals("Software Development Engineer", created.getJobTitle());
        assertEquals(sampleCompany, created.getCompany());
        verify(placementRepository, times(1)).save(sampleDrive);
    }

    @Test
    @DisplayName("Fails to create drive when company is null or missing ID")
    void createPlacementDrive_MissingCompany() {
        sampleDrive.setCompany(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                placementService.createPlacementDrive(sampleDrive));
        assertTrue(ex.getMessage().contains("valid company"));

        sampleDrive.setCompany(new Company());
        assertThrows(IllegalArgumentException.class, () ->
                placementService.createPlacementDrive(sampleDrive));
    }

    @Test
    @DisplayName("Fails to create drive when referenced company does not exist (ResourceNotFoundException)")
    void createPlacementDrive_CompanyNotFound() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                placementService.createPlacementDrive(sampleDrive));
        assertTrue(ex.getMessage().contains("Company not found with id: 1"));
    }

    @Test
    @DisplayName("Fails when application start date is after end date")
    void createPlacementDrive_InvalidStartEndDate() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(sampleCompany));
        sampleDrive.setApplicationStartDate(LocalDate.now().plusDays(15));
        sampleDrive.setApplicationEndDate(LocalDate.now().plusDays(5));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                placementService.createPlacementDrive(sampleDrive));
        assertTrue(ex.getMessage().contains("cannot be after application end date"));
    }

    @Test
    @DisplayName("Fails when application end date is after drive date")
    void createPlacementDrive_InvalidEndAndDriveDate() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(sampleCompany));
        sampleDrive.setApplicationStartDate(LocalDate.now());
        sampleDrive.setApplicationEndDate(LocalDate.now().plusDays(25));
        sampleDrive.setDriveDate(LocalDate.now().plusDays(20));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                placementService.createPlacementDrive(sampleDrive));
        assertTrue(ex.getMessage().contains("cannot be after drive date"));
    }

    @Test
    @DisplayName("Fails when salary package is zero or negative")
    void createPlacementDrive_NonPositiveSalary() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(sampleCompany));
        sampleDrive.setSalaryPackage(-5.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                placementService.createPlacementDrive(sampleDrive));
        assertTrue(ex.getMessage().contains("Salary package must be positive"));
    }

    @Test
    @DisplayName("Fails when vacancies are zero or negative")
    void createPlacementDrive_NonPositiveVacancies() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(sampleCompany));
        sampleDrive.setVacancies(0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                placementService.createPlacementDrive(sampleDrive));
        assertTrue(ex.getMessage().contains("Vacancies count must be positive"));
    }

    @Test
    @DisplayName("Update preserves existing drive ID and createdAt")
    void updatePlacementDrive_PreservesIdAndCreatedAt() {
        LocalDateTime createdTime = LocalDateTime.now().minusDays(5);
        sampleDrive.setCreatedAt(createdTime);

        when(placementRepository.findById(10L)).thenReturn(Optional.of(sampleDrive));
        when(placementRepository.save(any(PlacementDrive.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlacementDrive updatePayload = new PlacementDrive();
        updatePayload.setJobTitle("Senior Software Engineer");
        updatePayload.setJobDescription("Lead development of backend services.");
        updatePayload.setJobLocation("Hyderabad");
        updatePayload.setSalaryPackage(18.0);
        updatePayload.setEmploymentType("FULL_TIME");
        updatePayload.setApplicationStartDate(LocalDate.now());
        updatePayload.setApplicationEndDate(LocalDate.now().plusDays(5));
        updatePayload.setDriveDate(LocalDate.now().plusDays(15));
        updatePayload.setVacancies(2);
        updatePayload.setStatus(DriveStatus.ONGOING);

        PlacementDrive result = placementService.updatePlacementDrive(10L, updatePayload);

        assertEquals(10L, result.getId());
        assertEquals(createdTime, result.getCreatedAt());
        assertEquals("Senior Software Engineer", result.getJobTitle());
        assertEquals(DriveStatus.ONGOING, result.getStatus());
        assertEquals(18.0, result.getSalaryPackage());
    }

    @Test
    @DisplayName("Delete drive deletes the drive without deleting the company")
    void deletePlacementDrive_DeletesDriveOnly() {
        when(placementRepository.findById(10L)).thenReturn(Optional.of(sampleDrive));
        doNothing().when(placementRepository).delete(sampleDrive);

        placementService.deletePlacementDrive(10L);

        verify(placementRepository, times(1)).delete(sampleDrive);
        verify(companyRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Get drives by company verifies company existence first")
    void getDrivesByCompany_Success() {
        when(companyRepository.existsById(1L)).thenReturn(true);
        when(placementRepository.findByCompanyId(1L)).thenReturn(List.of(sampleDrive));

        List<PlacementDrive> drives = placementService.getDrivesByCompany(1L);

        assertEquals(1, drives.size());
        assertEquals(sampleDrive, drives.get(0));
    }

    @Test
    @DisplayName("Get drives by company throws ResourceNotFoundException if company does not exist")
    void getDrivesByCompany_CompanyNotFound() {
        when(companyRepository.existsById(99L)).thenReturn(false);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                placementService.getDrivesByCompany(99L));
        assertTrue(ex.getMessage().contains("Company not found with id: 99"));
    }

    @Test
    @DisplayName("Get drives by status returns repository results")
    void getDrivesByStatus_ReturnsMatchingDrives() {
        when(placementRepository.findByStatus(DriveStatus.UPCOMING)).thenReturn(List.of(sampleDrive));

        List<PlacementDrive> drives = placementService.getDrivesByStatus(DriveStatus.UPCOMING);

        assertEquals(1, drives.size());
        assertEquals(DriveStatus.UPCOMING, drives.get(0).getStatus());
    }

    @Test
    @DisplayName("Search drives by job title returns matching results")
    void searchDrivesByJobTitle_QueriesRepository() {
        when(placementRepository.findByJobTitleContainingIgnoreCase("Engineer")).thenReturn(List.of(sampleDrive));

        List<PlacementDrive> drives = placementService.searchDrivesByJobTitle("Engineer");

        assertEquals(1, drives.size());
        assertEquals("Software Development Engineer", drives.get(0).getJobTitle());
    }

    @Test
    @DisplayName("Successfully creates placement drive with valid eligibility criteria")
    void createPlacementDrive_WithValidEligibilityCriteria() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(sampleCompany));
        when(placementRepository.save(any(PlacementDrive.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sampleDrive.setMinimumCgpa(7.5);
        sampleDrive.setMaximumAllowedBacklogs(1);
        sampleDrive.setEligibleBranches("CSE,ECE,IT");

        PlacementDrive created = placementService.createPlacementDrive(sampleDrive);

        assertNotNull(created);
        assertEquals(7.5, created.getMinimumCgpa());
        assertEquals(1, created.getMaximumAllowedBacklogs());
        assertEquals("CSE,ECE,IT", created.getEligibleBranches());
    }

    @Test
    @DisplayName("Fails to create drive when minimum CGPA is below 0.0")
    void createPlacementDrive_MinimumCgpaBelowZero_Fails() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(sampleCompany));
        sampleDrive.setMinimumCgpa(-0.5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                placementService.createPlacementDrive(sampleDrive));
        assertTrue(ex.getMessage().contains("Minimum CGPA must be between 0.0 and 10.0"));
    }

    @Test
    @DisplayName("Fails to create drive when minimum CGPA is above 10.0")
    void createPlacementDrive_MinimumCgpaAboveTen_Fails() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(sampleCompany));
        sampleDrive.setMinimumCgpa(10.5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                placementService.createPlacementDrive(sampleDrive));
        assertTrue(ex.getMessage().contains("Minimum CGPA must be between 0.0 and 10.0"));
    }

    @Test
    @DisplayName("Fails to create drive when maximumAllowedBacklogs is negative")
    void createPlacementDrive_NegativeBacklogs_Fails() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(sampleCompany));
        sampleDrive.setMaximumAllowedBacklogs(-1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                placementService.createPlacementDrive(sampleDrive));
        assertTrue(ex.getMessage().contains("Maximum allowed backlogs cannot be negative"));
    }

    @Test
    @DisplayName("Allows null or blank eligibleBranches to represent all branches")
    void createPlacementDrive_NullOrBlankEligibleBranches_Accepted() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(sampleCompany));
        when(placementRepository.save(any(PlacementDrive.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sampleDrive.setEligibleBranches(null);
        PlacementDrive created = placementService.createPlacementDrive(sampleDrive);
        assertNull(created.getEligibleBranches());

        sampleDrive.setEligibleBranches("   ");
        PlacementDrive created2 = placementService.createPlacementDrive(sampleDrive);
        assertEquals("   ", created2.getEligibleBranches());
    }

    @Test
    @DisplayName("Updating placement drive preserves and updates eligibility criteria")
    void updatePlacementDrive_PreservesEligibilityCriteria() {
        PlacementDrive existing = new PlacementDrive();
        existing.setId(10L);
        existing.setCompany(sampleCompany);
        existing.setJobTitle("Old Title");
        existing.setMinimumCgpa(6.0);
        existing.setMaximumAllowedBacklogs(2);
        existing.setEligibleBranches("MECH,CIVIL");

        when(placementRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(placementRepository.save(any(PlacementDrive.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlacementDrive updateRequest = new PlacementDrive();
        updateRequest.setJobTitle("Updated Title");
        updateRequest.setJobDescription("Updated Description");
        updateRequest.setJobLocation("Pune");
        updateRequest.setSalaryPackage(15.0);
        updateRequest.setEmploymentType("FULL_TIME");
        updateRequest.setApplicationStartDate(LocalDate.now());
        updateRequest.setApplicationEndDate(LocalDate.now().plusDays(5));
        updateRequest.setDriveDate(LocalDate.now().plusDays(10));
        updateRequest.setVacancies(10);
        updateRequest.setMinimumCgpa(8.0);
        updateRequest.setMaximumAllowedBacklogs(0);
        updateRequest.setEligibleBranches("CSE,IT");

        PlacementDrive updated = placementService.updatePlacementDrive(10L, updateRequest);

        assertEquals(8.0, updated.getMinimumCgpa());
        assertEquals(0, updated.getMaximumAllowedBacklogs());
        assertEquals("CSE,IT", updated.getEligibleBranches());
    }
}
