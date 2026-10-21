package org.example;

import org.example.exception.DuplicateApplicationException;
import org.example.exception.IneligibleStudentException;
import org.example.model.*;
import org.example.service.PlacementManagementSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PlacementManagementSystemIntegrationTest {

    private PlacementManagementSystem pms;

    @BeforeEach
    void setUp() {
        pms = new PlacementManagementSystem();
    }

    @Test
    @DisplayName("End-to-End Placement Workflow: Company -> Drive -> Eligibility -> Application -> 3 Interview Rounds -> Selected")
    void testEndToEndPlacementWorkflow() {
        // 1. Company Management: Add company
        Company company = pms.getCompanyService().addCompany(
                new Company("Microsoft", "Product Engineering", "Hyderabad", "https://microsoft.com")
        );
        assertNotNull(company.getId());

        // 2. Placement Drive Management: Create placement drive
        PlacementDrive drive = pms.getPlacementDriveService().createDrive(new PlacementDrive(
                null,
                company.getId(),
                "Software Development Engineer",
                24.5,
                "Work on Core Cloud & AI platforms",
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(20),
                8.0, // Minimum CGPA: 8.0
                0,   // Max Backlogs: 0
                new HashSet<>(Arrays.asList("CSE", "IT"))
        ));
        assertNotNull(drive.getId());
        assertEquals(DriveStatus.SCHEDULED, drive.getStatus());

        // 3. Student Management & Eligibility Check
        Student eligibleStudent = pms.getStudentRepository().save(
                new Student("STU-1", "Aditi Rao", "aditi@college.edu", "CSE", 8.8, 0)
        );
        Student lowCgpaStudent = pms.getStudentRepository().save(
                new Student("STU-2", "Karan Verma", "karan@college.edu", "IT", 7.5, 0)
        );

        EligibilityResult result1 = pms.getStudentEligibilityService().checkEligibility(eligibleStudent, drive);
        assertTrue(result1.isEligible(), "Student 1 should be eligible");

        EligibilityResult result2 = pms.getStudentEligibilityService().checkEligibility(lowCgpaStudent, drive);
        assertFalse(result2.isEligible(), "Student 2 should be ineligible due to CGPA < 8.0");

        // 4. Application Management:
        // Ineligible student applying should throw IneligibleStudentException
        assertThrows(IneligibleStudentException.class, () ->
                pms.getApplicationService().applyForDrive(lowCgpaStudent.getId(), drive.getId(), LocalDate.now()));

        // Eligible student applies successfully
        Application app = pms.getApplicationService().applyForDrive(eligibleStudent.getId(), drive.getId(), LocalDate.now());
        assertEquals(ApplicationStatus.APPLIED, app.getStatus());

        // Duplicate application must be prevented
        assertThrows(DuplicateApplicationException.class, () ->
                pms.getApplicationService().applyForDrive(eligibleStudent.getId(), drive.getId(), LocalDate.now()));

        // 5. Interview Round Management:
        InterviewRound r1 = pms.getInterviewRoundService().addRoundToDrive(drive.getId(),
                new InterviewRound(1, "Online Coding Assessment", RoundType.ONLINE_ASSESSMENT, LocalDate.now().plusDays(8)));
        InterviewRound r2 = pms.getInterviewRoundService().addRoundToDrive(drive.getId(),
                new InterviewRound(2, "DSA & System Design Interview", RoundType.TECHNICAL, LocalDate.now().plusDays(10)));
        InterviewRound r3 = pms.getInterviewRoundService().addRoundToDrive(drive.getId(),
                new InterviewRound(3, "Behavioral & Leadership Round", RoundType.HR, LocalDate.now().plusDays(12)));

        List<InterviewRound> rounds = pms.getInterviewRoundService().getRoundsByDrive(drive.getId());
        assertEquals(3, rounds.size());

        // Round 1 Evaluation: CLEARED
        pms.getInterviewRoundService().recordCandidateResult(r1.getId(), eligibleStudent.getId(), RoundResult.CLEARED, "Top percentile score");
        assertEquals(ApplicationStatus.SHORTLISTED, pms.getApplicationService().getApplicationById(app.getId()).getStatus());

        // Round 2 Evaluation: CLEARED
        pms.getInterviewRoundService().recordCandidateResult(r2.getId(), eligibleStudent.getId(), RoundResult.CLEARED, "Strong design reasoning");
        assertEquals(ApplicationStatus.SHORTLISTED, pms.getApplicationService().getApplicationById(app.getId()).getStatus());

        // Round 3 Evaluation (Final Round): CLEARED -> Promoted to SELECTED!
        pms.getInterviewRoundService().recordCandidateResult(r3.getId(), eligibleStudent.getId(), RoundResult.CLEARED, "Great cultural alignment");
        Application finalApp = pms.getApplicationService().getApplicationById(app.getId());
        assertEquals(ApplicationStatus.SELECTED, finalApp.getStatus());

        // Verify full progression history
        List<CandidateRoundEvaluation> evaluations = pms.getInterviewRoundService().getCandidateProgress(drive.getId(), eligibleStudent.getId());
        assertEquals(3, evaluations.size());
        assertTrue(evaluations.stream().allMatch(e -> e.getResult() == RoundResult.CLEARED));
    }
}
