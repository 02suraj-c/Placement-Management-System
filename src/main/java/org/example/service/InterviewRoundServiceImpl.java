package org.example.service;

import org.example.exception.PlacementDriveNotFoundException;
import org.example.exception.ResourceNotFoundException;
import org.example.exception.StudentNotFoundException;
import org.example.exception.ValidationException;
import org.example.model.*;
import org.example.repository.ApplicationRepository;
import org.example.repository.InterviewRoundRepository;
import org.example.repository.PlacementDriveRepository;
import org.example.repository.StudentRepository;
import org.example.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of InterviewRoundService.
 */
public class InterviewRoundServiceImpl implements InterviewRoundService {
    private final InterviewRoundRepository roundRepository;
    private final PlacementDriveRepository driveRepository;
    private final StudentRepository studentRepository;
    private final ApplicationRepository applicationRepository;

    public InterviewRoundServiceImpl(InterviewRoundRepository roundRepository,
                                     PlacementDriveRepository driveRepository,
                                     StudentRepository studentRepository,
                                     ApplicationRepository applicationRepository) {
        this.roundRepository = Objects.requireNonNull(roundRepository, "InterviewRoundRepository must not be null");
        this.driveRepository = Objects.requireNonNull(driveRepository, "PlacementDriveRepository must not be null");
        this.studentRepository = Objects.requireNonNull(studentRepository, "StudentRepository must not be null");
        this.applicationRepository = Objects.requireNonNull(applicationRepository, "ApplicationRepository must not be null");
    }

    @Override
    public InterviewRound addRoundToDrive(String driveId, InterviewRound round) {
        ValidationUtil.requireNonBlank(driveId, "Drive ID");
        if (round == null) {
            throw new ValidationException("Interview round details must not be null");
        }

        PlacementDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new PlacementDriveNotFoundException("Placement drive not found with ID: " + driveId));

        if (drive.isCancelled()) {
            throw new ValidationException("Cannot add interview round to a cancelled placement drive (ID: " + driveId + ")");
        }

        ValidationUtil.requireNonBlank(round.getRoundName(), "Round name");
        round.setDriveId(driveId);

        List<InterviewRound> existingRounds = roundRepository.findRoundsByDriveId(driveId);
        if (round.getRoundNumber() <= 0) {
            int nextNumber = existingRounds.stream()
                    .mapToInt(InterviewRound::getRoundNumber)
                    .max()
                    .orElse(0) + 1;
            round.setRoundNumber(nextNumber);
        }

        if (round.getRoundType() == null) {
            round.setRoundType(RoundType.TECHNICAL);
        }

        if (round.getRoundDate() == null) {
            round.setRoundDate(LocalDate.now());
        }

        return roundRepository.saveRound(round);
    }

    @Override
    public InterviewRound getRoundById(String roundId) {
        ValidationUtil.requireNonBlank(roundId, "Round ID");
        return roundRepository.findRoundById(roundId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview round not found with ID: " + roundId));
    }

    @Override
    public List<InterviewRound> getRoundsByDrive(String driveId) {
        ValidationUtil.requireNonBlank(driveId, "Drive ID");
        return roundRepository.findRoundsByDriveId(driveId);
    }

    @Override
    public CandidateRoundEvaluation recordCandidateResult(String roundId, String studentId, RoundResult result, String remarks) {
        ValidationUtil.requireNonBlank(roundId, "Round ID");
        ValidationUtil.requireNonBlank(studentId, "Student ID");
        if (result == null) {
            throw new ValidationException("Evaluation result cannot be null");
        }

        InterviewRound currentRound = getRoundById(roundId);
        String driveId = currentRound.getDriveId();

        if (!studentRepository.existsById(studentId)) {
            throw new StudentNotFoundException("Student not found with ID: " + studentId);
        }

        Application application = applicationRepository.findByDriveIdAndStudentId(driveId, studentId)
                .orElseThrow(() -> new ValidationException(String.format(
                        "Student %s has not applied for placement drive %s", studentId, driveId)));

        // Enforce round progression: if roundNumber > 1, verify student cleared previous round
        List<InterviewRound> allRounds = roundRepository.findRoundsByDriveId(driveId);
        int currentRoundNumber = currentRound.getRoundNumber();

        Optional<InterviewRound> previousRoundOpt = allRounds.stream()
                .filter(r -> r.getRoundNumber() < currentRoundNumber)
                .reduce((first, second) -> second); // get highest round number before current

        if (previousRoundOpt.isPresent()) {
            InterviewRound prevRound = previousRoundOpt.get();
            Optional<CandidateRoundEvaluation> prevEvalOpt = roundRepository.findEvaluation(prevRound.getId(), studentId);

            if (prevEvalOpt.isEmpty() || prevEvalOpt.get().getResult() != RoundResult.CLEARED) {
                throw new ValidationException(String.format(
                        "Candidate cannot be evaluated for Round %d (%s) without clearing previous Round %d (%s)",
                        currentRoundNumber, currentRound.getRoundName(),
                        prevRound.getRoundNumber(), prevRound.getRoundName()));
            }
        }

        // Save or update evaluation
        CandidateRoundEvaluation evaluation = roundRepository.findEvaluation(roundId, studentId)
                .orElse(new CandidateRoundEvaluation());

        evaluation.setDriveId(driveId);
        evaluation.setRoundId(roundId);
        evaluation.setStudentId(studentId);
        evaluation.setResult(result);
        evaluation.setRemarks(remarks != null ? remarks.trim() : "");
        evaluation.setEvaluationDate(LocalDate.now());

        roundRepository.saveEvaluation(evaluation);

        // Update application status based on evaluation
        if (result == RoundResult.FAILED) {
            application.setStatus(ApplicationStatus.REJECTED);
            application.setRemarks("Failed in Round " + currentRoundNumber + " (" + currentRound.getRoundName() + ")");
            applicationRepository.save(application);
        } else if (result == RoundResult.CLEARED) {
            // Check if this is the final round of the drive
            int maxRoundNumber = allRounds.stream().mapToInt(InterviewRound::getRoundNumber).max().orElse(currentRoundNumber);
            if (currentRoundNumber >= maxRoundNumber) {
                application.setStatus(ApplicationStatus.SELECTED);
                application.setRemarks("Cleared all interview rounds and selected!");
            } else {
                application.setStatus(ApplicationStatus.SHORTLISTED);
                application.setRemarks("Cleared Round " + currentRoundNumber + " (" + currentRound.getRoundName() + ")");
            }
            applicationRepository.save(application);
        }

        return evaluation;
    }

    @Override
    public List<CandidateRoundEvaluation> getCandidateProgress(String driveId, String studentId) {
        ValidationUtil.requireNonBlank(driveId, "Drive ID");
        ValidationUtil.requireNonBlank(studentId, "Student ID");
        return roundRepository.findEvaluationsByDriveIdAndStudentId(driveId, studentId);
    }

    @Override
    public List<CandidateRoundEvaluation> getCandidatesByRound(String roundId) {
        ValidationUtil.requireNonBlank(roundId, "Round ID");
        return roundRepository.findEvaluationsByRoundId(roundId);
    }
}
