package org.example;

import org.example.model.EligibilityResult;
import org.example.model.PlacementDrive;
import org.example.model.Student;
import org.example.repository.InMemoryPlacementDriveRepository;
import org.example.repository.InMemoryStudentRepository;
import org.example.service.StudentEligibilityService;
import org.example.service.StudentEligibilityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class StudentEligibilityServiceTest {

    private InMemoryStudentRepository studentRepository;
    private InMemoryPlacementDriveRepository driveRepository;
    private StudentEligibilityService eligibilityService;
    private PlacementDrive standardDrive;

    @BeforeEach
    void setUp() {
        studentRepository = new InMemoryStudentRepository();
        driveRepository = new InMemoryPlacementDriveRepository();
        eligibilityService = new StudentEligibilityServiceImpl(studentRepository, driveRepository);

        standardDrive = new PlacementDrive(
                "DRIVE-1",
                "COMP-1",
                "Software Engineer",
                12.0,
                "Backend role",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(20),
                7.0, // min CGPA: 7.0
                0,   // max backlogs: 0
                new HashSet<>(Arrays.asList("CSE", "IT"))
        );
        driveRepository.save(standardDrive);
    }

    @Test
    @DisplayName("Should pass eligibility when student meets CGPA, backlogs, and branch criteria")
    void testEligibleStudent() {
        Student student = new Student("STU-1", "Alice", "alice@example.com", "CSE", 8.5, 0);

        EligibilityResult result = eligibilityService.checkEligibility(student, standardDrive);

        assertTrue(result.isEligible());
        assertTrue(result.getFormattedReason().contains("Eligible"));
    }

    @Test
    @DisplayName("Should match branch case-insensitively")
    void testCaseInsensitiveBranchMatching() {
        Student student = new Student("STU-2", "Bob", "bob@example.com", "cse", 7.5, 0);

        EligibilityResult result = eligibilityService.checkEligibility(student, standardDrive);

        assertTrue(result.isEligible());
    }

    @Test
    @DisplayName("Should fail eligibility when CGPA is below minimum required")
    void testIneligibleDueToLowCGPA() {
        Student student = new Student("STU-3", "Charlie", "charlie@example.com", "CSE", 6.8, 0);

        EligibilityResult result = eligibilityService.checkEligibility(student, standardDrive);

        assertFalse(result.isEligible());
        assertTrue(result.getFormattedReason().contains("below minimum required CGPA"));
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("6.80") && r.contains("7.00")));
    }

    @Test
    @DisplayName("Should fail eligibility when student has active backlogs exceeding maximum allowed")
    void testIneligibleDueToBacklogs() {
        Student student = new Student("STU-4", "David", "david@example.com", "IT", 8.0, 1);

        EligibilityResult result = eligibilityService.checkEligibility(student, standardDrive);

        assertFalse(result.isEligible());
        assertTrue(result.getFormattedReason().contains("exceeds maximum allowed backlogs"));
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("1") && r.contains("0")));
    }

    @Test
    @DisplayName("Should fail eligibility when student's branch is not in eligible branches")
    void testIneligibleDueToBranch() {
        Student student = new Student("STU-5", "Eve", "eve@example.com", "CIVIL", 9.0, 0);

        EligibilityResult result = eligibilityService.checkEligibility(student, standardDrive);

        assertFalse(result.isEligible());
        assertTrue(result.getFormattedReason().contains("CIVIL"));
        assertTrue(result.getFormattedReason().contains("not in eligible branches"));
    }

    @Test
    @DisplayName("Should report all failing reasons when multiple criteria fail")
    void testIneligibleMultipleCriteria() {
        // Low CGPA, backlogs > 0, branch not allowed
        Student student = new Student("STU-6", "Frank", "frank@example.com", "MECHANICAL", 5.5, 2);

        EligibilityResult result = eligibilityService.checkEligibility(student, standardDrive);

        assertFalse(result.isEligible());
        assertEquals(3, result.getReasons().size());
        assertTrue(result.getFormattedReason().contains("below minimum required CGPA"));
        assertTrue(result.getFormattedReason().contains("exceeds maximum allowed backlogs"));
        assertTrue(result.getFormattedReason().contains("MECHANICAL"));
    }

    @Test
    @DisplayName("Should check eligibility by IDs via repositories")
    void testCheckEligibilityByIds() {
        Student student = studentRepository.save(new Student("STU-100", "Grace", "grace@example.com", "IT", 8.2, 0));

        EligibilityResult result = eligibilityService.checkEligibility(student.getId(), standardDrive.getId());

        assertTrue(result.isEligible());
    }
}
