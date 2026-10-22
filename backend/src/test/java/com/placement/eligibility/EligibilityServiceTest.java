package com.placement.eligibility;

import com.placement.company.Company;
import com.placement.eligibility.dto.EligibilityCheckResponse;
import com.placement.exception.ResourceNotFoundException;
import com.placement.placement.DriveStatus;
import com.placement.placement.PlacementDrive;
import com.placement.placement.PlacementRepository;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EligibilityService Tests")
class EligibilityServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PlacementRepository placementRepository;

    @InjectMocks
    private EligibilityService eligibilityService;

    private Student standardStudent;
    private PlacementDrive standardDrive;
    private Company standardCompany;

    @BeforeEach
    void setUp() {
        standardCompany = new Company();
        standardCompany.setId(10L);
        standardCompany.setName("Acme Corp");

        standardStudent = Student.builder()
                .id(1L)
                .rollNumber("CS2026-001")
                .department("CSE")
                .course("B.Tech")
                .currentSemester(6)
                .cgpa(8.5)
                .graduationYear(2026)
                .activeBacklogs(0)
                .build();

        standardDrive = new PlacementDrive();
        standardDrive.setId(100L);
        standardDrive.setCompany(standardCompany);
        standardDrive.setJobTitle("Software Engineer");
        standardDrive.setJobDescription("Full-stack development");
        standardDrive.setJobLocation("Bengaluru");
        standardDrive.setSalaryPackage(1200000.0);
        standardDrive.setEmploymentType("Full-Time");
        standardDrive.setApplicationStartDate(LocalDate.now());
        standardDrive.setApplicationEndDate(LocalDate.now().plusDays(10));
        standardDrive.setDriveDate(LocalDate.now().plusDays(15));
        standardDrive.setVacancies(5);
        standardDrive.setStatus(DriveStatus.UPCOMING);
        standardDrive.setMinimumCgpa(7.0);
        standardDrive.setMaximumAllowedBacklogs(0);
        standardDrive.setEligibleBranches("CSE, ECE, IT");
    }

    @Test
    @DisplayName("1. Eligible student with unrestricted department should be eligible")
    void testEligibleStudentWithUnrestrictedDepartment() {
        standardDrive.setEligibleBranches(null);
        standardStudent.setDepartment("Mechanical");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(standardStudent));
        when(placementRepository.findById(100L)).thenReturn(Optional.of(standardDrive));

        EligibilityCheckResponse response = eligibilityService.checkEligibility(1L, 100L);

        assertTrue(response.isEligible());
        assertTrue(response.getReasons().isEmpty());
        assertEquals(8.5, response.getStudentCgpa());
        assertEquals(7.0, response.getMinimumCgpa());
        assertEquals(0, response.getStudentActiveBacklogs());
        assertEquals(0, response.getMaximumAllowedBacklogs());
        assertEquals("Mechanical", response.getStudentDepartment());
        assertNull(response.getEligibleBranches());
        assertEquals("UPCOMING", response.getDriveStatus());
    }

    @Test
    @DisplayName("2. CGPA below minimum -> ineligible with descriptive reason")
    void testCgpaBelowMinimumIneligible() {
        standardStudent.setCgpa(6.5);
        standardDrive.setMinimumCgpa(7.5);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(standardStudent));
        when(placementRepository.findById(100L)).thenReturn(Optional.of(standardDrive));

        EligibilityCheckResponse response = eligibilityService.checkEligibility(1L, 100L);

        assertFalse(response.isEligible());
        assertEquals(1, response.getReasons().size());
        assertTrue(response.getReasons().get(0).contains("Student CGPA (6.5) is below minimum required CGPA (7.5)."));
    }

    @Test
    @DisplayName("3. Active backlogs above maximum -> ineligible with descriptive reason")
    void testActiveBacklogsAboveMaximumIneligible() {
        standardStudent.setActiveBacklogs(2);
        standardDrive.setMaximumAllowedBacklogs(1);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(standardStudent));
        when(placementRepository.findById(100L)).thenReturn(Optional.of(standardDrive));

        EligibilityCheckResponse response = eligibilityService.checkEligibility(1L, 100L);

        assertFalse(response.isEligible());
        assertEquals(1, response.getReasons().size());
        assertTrue(response.getReasons().get(0).contains("Student active backlogs (2) exceeds maximum allowed backlogs (1)."));
    }

    @Test
    @DisplayName("4. Department not eligible -> ineligible with descriptive reason")
    void testDepartmentNotEligibleIneligible() {
        standardStudent.setDepartment("Civil Engineering");
        standardDrive.setEligibleBranches("CSE, ECE, IT");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(standardStudent));
        when(placementRepository.findById(100L)).thenReturn(Optional.of(standardDrive));

        EligibilityCheckResponse response = eligibilityService.checkEligibility(1L, 100L);

        assertFalse(response.isEligible());
        assertEquals(1, response.getReasons().size());
        assertTrue(response.getReasons().get(0).contains("Student department 'Civil Engineering' is not in eligible branches CSE, ECE, IT."));
    }

    @Test
    @DisplayName("5. Cancelled drive -> ineligible even if academic criteria pass")
    void testCancelledDriveIneligible() {
        standardDrive.setStatus(DriveStatus.CANCELLED);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(standardStudent));
        when(placementRepository.findById(100L)).thenReturn(Optional.of(standardDrive));

        EligibilityCheckResponse response = eligibilityService.checkEligibility(1L, 100L);

        assertFalse(response.isEligible());
        assertEquals(1, response.getReasons().size());
        assertEquals("Placement drive is cancelled.", response.getReasons().get(0));
    }

    @Test
    @DisplayName("6. Multiple failed criteria -> all applicable reasons returned")
    void testMultipleFailedCriteriaReturnsAllReasons() {
        standardDrive.setStatus(DriveStatus.CANCELLED);
        standardDrive.setMinimumCgpa(8.0);
        standardDrive.setMaximumAllowedBacklogs(0);
        standardDrive.setEligibleBranches("CSE, IT");

        standardStudent.setCgpa(6.5);
        standardStudent.setActiveBacklogs(2);
        standardStudent.setDepartment("Civil");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(standardStudent));
        when(placementRepository.findById(100L)).thenReturn(Optional.of(standardDrive));

        EligibilityCheckResponse response = eligibilityService.checkEligibility(1L, 100L);

        assertFalse(response.isEligible());
        assertEquals(4, response.getReasons().size());
        assertEquals("Placement drive is cancelled.", response.getReasons().get(0));
        assertTrue(response.getReasons().get(1).contains("Student CGPA (6.5) is below minimum required CGPA (8.0)."));
        assertTrue(response.getReasons().get(2).contains("Student active backlogs (2) exceeds maximum allowed backlogs (0)."));
        assertTrue(response.getReasons().get(3).contains("Student department 'Civil' is not in eligible branches CSE, IT."));
    }

    @Test
    @DisplayName("7. eligibleBranches null -> all departments allowed")
    void testEligibleBranchesNullAllowsAllDepartments() {
        standardDrive.setEligibleBranches(null);
        standardStudent.setDepartment("BioTech");

        EligibilityCheckResponse response = eligibilityService.evaluateEligibility(standardStudent, standardDrive);

        assertTrue(response.isEligible());
        assertTrue(response.getReasons().isEmpty());
    }

    @Test
    @DisplayName("8. eligibleBranches blank -> all departments allowed")
    void testEligibleBranchesBlankAllowsAllDepartments() {
        standardDrive.setEligibleBranches("   ");
        standardStudent.setDepartment("Chemical Engineering");

        EligibilityCheckResponse response = eligibilityService.evaluateEligibility(standardStudent, standardDrive);

        assertTrue(response.isEligible());
        assertTrue(response.getReasons().isEmpty());
    }

    @Test
    @DisplayName("9. Department matching is case-insensitive")
    void testDepartmentMatchingIsCaseInsensitive() {
        standardDrive.setEligibleBranches("CSE, ECE, IT");
        standardStudent.setDepartment("cse");

        EligibilityCheckResponse response = eligibilityService.evaluateEligibility(standardStudent, standardDrive);

        assertTrue(response.isEligible());
        assertTrue(response.getReasons().isEmpty());
    }

    @Test
    @DisplayName("10. Department matching ignores surrounding whitespace")
    void testDepartmentMatchingIgnoresSurroundingWhitespace() {
        standardDrive.setEligibleBranches("  CSE  ,   ECE , IT  ");
        standardStudent.setDepartment("   ECE   ");

        EligibilityCheckResponse response = eligibilityService.evaluateEligibility(standardStudent, standardDrive);

        assertTrue(response.isEligible());
        assertTrue(response.getReasons().isEmpty());
    }

    @Test
    @DisplayName("11. Missing student -> throws ResourceNotFoundException")
    void testMissingStudentThrowsResourceNotFoundException() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                eligibilityService.checkEligibility(999L, 100L)
        );

        assertTrue(ex.getMessage().contains("Student not found with id : '999'"));
        verify(placementRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("12. Missing drive -> throws ResourceNotFoundException")
    void testMissingDriveThrowsResourceNotFoundException() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(standardStudent));
        when(placementRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                eligibilityService.checkEligibility(1L, 999L)
        );

        assertTrue(ex.getMessage().contains("PlacementDrive not found with id : '999'"));
    }
}
