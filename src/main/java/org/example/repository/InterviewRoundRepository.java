package org.example.repository;

import org.example.model.CandidateRoundEvaluation;
import org.example.model.InterviewRound;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for interview rounds and candidate evaluations.
 */
public interface InterviewRoundRepository {
    InterviewRound saveRound(InterviewRound round);
    Optional<InterviewRound> findRoundById(String roundId);
    List<InterviewRound> findRoundsByDriveId(String driveId);
    boolean deleteRoundById(String roundId);

    CandidateRoundEvaluation saveEvaluation(CandidateRoundEvaluation evaluation);
    Optional<CandidateRoundEvaluation> findEvaluation(String roundId, String studentId);
    List<CandidateRoundEvaluation> findEvaluationsByRoundId(String roundId);
    List<CandidateRoundEvaluation> findEvaluationsByDriveIdAndStudentId(String driveId, String studentId);
    void clear();
}
