package org.example.model;

import org.example.util.ValidationUtil;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Tracks a student's evaluation and outcome in a specific interview round.
 */
public class CandidateRoundEvaluation {
    private String id;
    private String driveId;
    private String roundId;
    private String studentId;
    private RoundResult result;
    private String remarks;
    private LocalDate evaluationDate;

    public CandidateRoundEvaluation() {
        this.result = RoundResult.PENDING;
        this.evaluationDate = LocalDate.now();
    }

    public CandidateRoundEvaluation(String id, String driveId, String roundId, String studentId, RoundResult result, String remarks, LocalDate evaluationDate) {
        this.id = id != null ? id.trim() : null;
        this.driveId = ValidationUtil.requireNonBlank(driveId, "Drive ID");
        this.roundId = ValidationUtil.requireNonBlank(roundId, "Round ID");
        this.studentId = ValidationUtil.requireNonBlank(studentId, "Student ID");
        this.result = result != null ? result : RoundResult.PENDING;
        this.remarks = remarks != null ? remarks.trim() : "";
        this.evaluationDate = evaluationDate != null ? evaluationDate : LocalDate.now();
    }

    public CandidateRoundEvaluation(String driveId, String roundId, String studentId, RoundResult result, String remarks) {
        this(null, driveId, roundId, studentId, result, remarks, LocalDate.now());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = ValidationUtil.requireNonBlank(id, "Evaluation ID");
    }

    public String getDriveId() {
        return driveId;
    }

    public void setDriveId(String driveId) {
        this.driveId = ValidationUtil.requireNonBlank(driveId, "Drive ID");
    }

    public String getRoundId() {
        return roundId;
    }

    public void setRoundId(String roundId) {
        this.roundId = ValidationUtil.requireNonBlank(roundId, "Round ID");
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = ValidationUtil.requireNonBlank(studentId, "Student ID");
    }

    public RoundResult getResult() {
        return result;
    }

    public void setResult(RoundResult result) {
        this.result = result != null ? result : RoundResult.PENDING;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks != null ? remarks.trim() : "";
    }

    public LocalDate getEvaluationDate() {
        return evaluationDate;
    }

    public void setEvaluationDate(LocalDate evaluationDate) {
        this.evaluationDate = evaluationDate != null ? evaluationDate : LocalDate.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CandidateRoundEvaluation that = (CandidateRoundEvaluation) o;
        return Objects.equals(id, that.id) ||
                (Objects.equals(roundId, that.roundId) && Objects.equals(studentId, that.studentId));
    }

    @Override
    public int hashCode() {
        return Objects.hash(roundId, studentId);
    }

    @Override
    public String toString() {
        return "CandidateRoundEvaluation{" +
                "id='" + id + '\'' +
                ", driveId='" + driveId + '\'' +
                ", roundId='" + roundId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", result=" + result +
                ", remarks='" + remarks + '\'' +
                ", evaluationDate=" + evaluationDate +
                '}';
    }
}
