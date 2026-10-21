package org.example.repository;

import org.example.model.CandidateRoundEvaluation;
import org.example.model.InterviewRound;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory implementation of InterviewRoundRepository.
 */
public class InMemoryInterviewRoundRepository implements InterviewRoundRepository {
    private final Map<String, InterviewRound> roundStorage = new ConcurrentHashMap<>();
    private final Map<String, CandidateRoundEvaluation> evalStorage = new ConcurrentHashMap<>();
    private final AtomicLong roundIdGenerator = new AtomicLong(1);
    private final AtomicLong evalIdGenerator = new AtomicLong(1);

    @Override
    public InterviewRound saveRound(InterviewRound round) {
        if (round == null) {
            throw new IllegalArgumentException("InterviewRound cannot be null");
        }
        if (round.getId() == null || round.getId().trim().isEmpty()) {
            round.setId("ROUND-" + roundIdGenerator.getAndIncrement());
        }
        roundStorage.put(round.getId(), round);
        return round;
    }

    @Override
    public Optional<InterviewRound> findRoundById(String roundId) {
        if (roundId == null) return Optional.empty();
        return Optional.ofNullable(roundStorage.get(roundId));
    }

    @Override
    public List<InterviewRound> findRoundsByDriveId(String driveId) {
        if (driveId == null) return Collections.emptyList();
        return roundStorage.values().stream()
                .filter(r -> Objects.equals(r.getDriveId(), driveId))
                .sorted(Comparator.comparingInt(InterviewRound::getRoundNumber))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteRoundById(String roundId) {
        if (roundId == null) return false;
        return roundStorage.remove(roundId) != null;
    }

    @Override
    public CandidateRoundEvaluation saveEvaluation(CandidateRoundEvaluation evaluation) {
        if (evaluation == null) {
            throw new IllegalArgumentException("CandidateRoundEvaluation cannot be null");
        }
        if (evaluation.getId() == null || evaluation.getId().trim().isEmpty()) {
            evaluation.setId("EVAL-" + evalIdGenerator.getAndIncrement());
        }
        // Unique key by roundId + studentId
        evalStorage.put(evaluation.getRoundId() + ":" + evaluation.getStudentId(), evaluation);
        return evaluation;
    }

    @Override
    public Optional<CandidateRoundEvaluation> findEvaluation(String roundId, String studentId) {
        if (roundId == null || studentId == null) return Optional.empty();
        return Optional.ofNullable(evalStorage.get(roundId + ":" + studentId));
    }

    @Override
    public List<CandidateRoundEvaluation> findEvaluationsByRoundId(String roundId) {
        if (roundId == null) return Collections.emptyList();
        return evalStorage.values().stream()
                .filter(e -> Objects.equals(e.getRoundId(), roundId))
                .collect(Collectors.toList());
    }

    @Override
    public List<CandidateRoundEvaluation> findEvaluationsByDriveIdAndStudentId(String driveId, String studentId) {
        if (driveId == null || studentId == null) return Collections.emptyList();
        return evalStorage.values().stream()
                .filter(e -> Objects.equals(e.getDriveId(), driveId) && Objects.equals(e.getStudentId(), studentId))
                .collect(Collectors.toList());
    }

    @Override
    public void clear() {
        roundStorage.clear();
        evalStorage.clear();
    }
}
