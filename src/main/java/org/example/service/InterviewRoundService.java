package org.example.service;

import org.example.model.CandidateRoundEvaluation;
import org.example.model.InterviewRound;
import org.example.model.RoundResult;

import java.util.List;

/**
 * Service managing interview rounds and tracking candidate progression.
 */
public interface InterviewRoundService {
    InterviewRound addRoundToDrive(String driveId, InterviewRound round);
    InterviewRound getRoundById(String roundId);
    List<InterviewRound> getRoundsByDrive(String driveId);
    CandidateRoundEvaluation recordCandidateResult(String roundId, String studentId, RoundResult result, String remarks);
    List<CandidateRoundEvaluation> getCandidateProgress(String driveId, String studentId);
    List<CandidateRoundEvaluation> getCandidatesByRound(String roundId);
}
