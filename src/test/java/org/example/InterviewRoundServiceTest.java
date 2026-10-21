package org.example;

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

public class InterviewRoundServiceTest {

    private InMemoryInterviewRoundRepository roundRepository;
    private InMemoryPlacementDriveRepository driveRepository;
    private InMemoryStudentRepository studentRepository;
    private InMemoryApplicationRepository applicationRepository;
    private InterviewRoundService roundService;
    private ApplicationService applicationService;

    private PlacementDrive drive;
    private Student candidate;

    @BeforeEach
    void setUp() {
        roundRepository = new InMemoryInterviewRoundRepository();
        driveRepository = new InMemoryPlacementDriveRepository();
        studentRepository = new InMemoryStudentRepository();
        applicationRepository = new InMemoryApplicationRepository();

        roundService = new InterviewRoundServiceImpl(roundRepository, driveRepository, studentRepository, applicationRepository);

        StudentEligibilityService eligibilityService = new StudentEligibilityServiceImpl(studentRepository, driveRepository);
        applicationService = new ApplicationServiceImpl(applicationRepository, driveRepository, studentRepository, eligibilityService);

        drive = driveRepository.save(new PlacementDrive(
                "DRIVE-1",
                "COMP-1",
                "Software Engineer",
                14.0,
                "Backend role",
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(15),
                7.0,
                0,
                new HashSet<>(Arrays.asList("CSE", "IT"))
        ));

        candidate = studentRepository.save(new Student("STU-1", "Aarav", "aarav@example.com", "CSE", 8.5, 0));
    }

    @Test
    @DisplayName("Should add interview rounds to placement drive with sequential numbering")
    void testAddInterviewRounds() {
        InterviewRound r1 = new InterviewRound(1, "Coding Assessment", RoundType.ONLINE_ASSESSMENT, LocalDate.now().plusDays(10));
        InterviewRound r2 = new InterviewRound(2, "System Design & Technical", RoundType.TECHNICAL, LocalDate.now().plusDays(12));
        InterviewRound r3 = new InterviewRound(3, "HR & Culture Fit", RoundType.HR, LocalDate.now().plusDays(14));

        InterviewRound saved1 = roundService.addRoundToDrive(drive.getId(), r1);
        InterviewRound saved2 = roundService.addRoundToDrive(drive.getId(), r2);
        InterviewRound saved3 = roundService.addRoundToDrive(drive.getId(), r3);

        assertNotNull(saved1.getId());
        assertNotNull(saved2.getId());
        assertNotNull(saved3.getId());

        List<InterviewRound> rounds = roundService.getRoundsByDrive(drive.getId());
        assertEquals(3, rounds.size());
        assertEquals(1, rounds.get(0).getRoundNumber());
        assertEquals(2, rounds.get(1).getRoundNumber());
        assertEquals(3, rounds.get(2).getRoundNumber());
    }

    @Test
    @DisplayName("Should prevent adding rounds to a cancelled drive")
    void testAddRoundToCancelledDriveFails() {
        drive.setStatus(DriveStatus.CANCELLED);
        driveRepository.save(drive);

        InterviewRound round = new InterviewRound(1, "Technical Round", RoundType.TECHNICAL, LocalDate.now());
        ValidationException ex = assertThrows(ValidationException.class, () -> roundService.addRoundToDrive(drive.getId(), round));
        assertTrue(ex.getMessage().contains("Cannot add interview round to a cancelled placement drive"));
    }

    @Test
    @DisplayName("Should track student progress through interview rounds and select candidate on final round clearance")
    void testInterviewRoundProgressionToSelected() {
        // Setup 2 rounds
        InterviewRound r1 = roundService.addRoundToDrive(drive.getId(), new InterviewRound(1, "Online Test", RoundType.ONLINE_ASSESSMENT, LocalDate.now().plusDays(8)));
        InterviewRound r2 = roundService.addRoundToDrive(drive.getId(), new InterviewRound(2, "Technical Interview", RoundType.TECHNICAL, LocalDate.now().plusDays(10)));

        // Candidate applies
        Application app = applicationService.applyForDrive(candidate.getId(), drive.getId(), LocalDate.now());
        assertEquals(ApplicationStatus.APPLIED, app.getStatus());

        // Evaluate Round 1 -> CLEARED
        CandidateRoundEvaluation eval1 = roundService.recordCandidateResult(r1.getId(), candidate.getId(), RoundResult.CLEARED, "Excellent score in algorithms");
        assertEquals(RoundResult.CLEARED, eval1.getResult());

        Application appAfterR1 = applicationService.getApplicationById(app.getId());
        assertEquals(ApplicationStatus.SHORTLISTED, appAfterR1.getStatus());

        // Evaluate Round 2 (final round) -> CLEARED
        CandidateRoundEvaluation eval2 = roundService.recordCandidateResult(r2.getId(), candidate.getId(), RoundResult.CLEARED, "Solved all live coding problems");
        assertEquals(RoundResult.CLEARED, eval2.getResult());

        // Because it was the final round, status must become SELECTED
        Application appAfterFinal = applicationService.getApplicationById(app.getId());
        assertEquals(ApplicationStatus.SELECTED, appAfterFinal.getStatus());

        // Verify progress history
        List<CandidateRoundEvaluation> progress = roundService.getCandidateProgress(drive.getId(), candidate.getId());
        assertEquals(2, progress.size());
    }

    @Test
    @DisplayName("Should enforce round progression: cannot evaluate Round 2 if Round 1 was not cleared")
    void testProgressionRequiresClearingPreviousRound() {
        InterviewRound r1 = roundService.addRoundToDrive(drive.getId(), new InterviewRound(1, "Online Test", RoundType.ONLINE_ASSESSMENT, LocalDate.now().plusDays(8)));
        InterviewRound r2 = roundService.addRoundToDrive(drive.getId(), new InterviewRound(2, "Technical Interview", RoundType.TECHNICAL, LocalDate.now().plusDays(10)));

        applicationService.applyForDrive(candidate.getId(), drive.getId(), LocalDate.now());

        // Without evaluating Round 1, attempting Round 2 must fail!
        ValidationException ex = assertThrows(ValidationException.class, () ->
                roundService.recordCandidateResult(r2.getId(), candidate.getId(), RoundResult.CLEARED, "Jumped ahead"));

        assertTrue(ex.getMessage().contains("without clearing previous Round 1"));
    }

    @Test
    @DisplayName("Should reject application when candidate fails an interview round")
    void testCandidateFailsRoundResultsInRejectedApplication() {
        InterviewRound r1 = roundService.addRoundToDrive(drive.getId(), new InterviewRound(1, "Online Test", RoundType.ONLINE_ASSESSMENT, LocalDate.now().plusDays(8)));

        Application app = applicationService.applyForDrive(candidate.getId(), drive.getId(), LocalDate.now());

        roundService.recordCandidateResult(r1.getId(), candidate.getId(), RoundResult.FAILED, "Did not meet cutoff");

        Application appAfterFail = applicationService.getApplicationById(app.getId());
        assertEquals(ApplicationStatus.REJECTED, appAfterFail.getStatus());
        assertTrue(appAfterFail.getRemarks().contains("Failed in Round 1"));
    }

    @Test
    @DisplayName("Should prevent round evaluation if student has not applied to the drive")
    void testEvaluationWithoutApplicationFails() {
        InterviewRound r1 = roundService.addRoundToDrive(drive.getId(), new InterviewRound(1, "Online Test", RoundType.ONLINE_ASSESSMENT, LocalDate.now().plusDays(8)));

        ValidationException ex = assertThrows(ValidationException.class, () ->
                roundService.recordCandidateResult(r1.getId(), candidate.getId(), RoundResult.CLEARED, "Never applied"));

        assertTrue(ex.getMessage().contains("has not applied for placement drive"));
    }

    @Test
    @DisplayName("Should view all candidates evaluated for a round")
    void testGetCandidatesByRound() {
        InterviewRound r1 = roundService.addRoundToDrive(drive.getId(), new InterviewRound(1, "Online Test", RoundType.ONLINE_ASSESSMENT, LocalDate.now().plusDays(8)));

        Student candidate2 = studentRepository.save(new Student("STU-2", "Priya", "priya@example.com", "IT", 8.9, 0));

        applicationService.applyForDrive(candidate.getId(), drive.getId(), LocalDate.now());
        applicationService.applyForDrive(candidate2.getId(), drive.getId(), LocalDate.now());

        roundService.recordCandidateResult(r1.getId(), candidate.getId(), RoundResult.CLEARED, "Passed");
        roundService.recordCandidateResult(r1.getId(), candidate2.getId(), RoundResult.FAILED, "Failed");

        List<CandidateRoundEvaluation> roundCandidates = roundService.getCandidatesByRound(r1.getId());
        assertEquals(2, roundCandidates.size());
    }
}
