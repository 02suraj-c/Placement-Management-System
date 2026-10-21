package org.example.model;

import org.example.util.ValidationUtil;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents an interview or evaluation round configured for a placement drive.
 */
public class InterviewRound {
    private String id;
    private String driveId;
    private int roundNumber;
    private String roundName;
    private RoundType roundType;
    private LocalDate roundDate;

    public InterviewRound() {
    }

    public InterviewRound(String id, String driveId, int roundNumber, String roundName, RoundType roundType, LocalDate roundDate) {
        this.id = id != null ? id.trim() : null;
        this.driveId = ValidationUtil.requireNonBlank(driveId, "Drive ID");
        this.roundNumber = ValidationUtil.validateNonNegative(roundNumber, "Round number");
        this.roundName = ValidationUtil.requireNonBlank(roundName, "Round name");
        this.roundType = roundType != null ? roundType : RoundType.TECHNICAL;
        this.roundDate = roundDate != null ? roundDate : LocalDate.now();
    }

    public InterviewRound(int roundNumber, String roundName, RoundType roundType, LocalDate roundDate) {
        this.roundNumber = ValidationUtil.validateNonNegative(roundNumber, "Round number");
        this.roundName = ValidationUtil.requireNonBlank(roundName, "Round name");
        this.roundType = roundType != null ? roundType : RoundType.TECHNICAL;
        this.roundDate = roundDate != null ? roundDate : LocalDate.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = ValidationUtil.requireNonBlank(id, "Round ID");
    }

    public String getDriveId() {
        return driveId;
    }

    public void setDriveId(String driveId) {
        this.driveId = ValidationUtil.requireNonBlank(driveId, "Drive ID");
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = ValidationUtil.validateNonNegative(roundNumber, "Round number");
    }

    public String getRoundName() {
        return roundName;
    }

    public void setRoundName(String roundName) {
        this.roundName = ValidationUtil.requireNonBlank(roundName, "Round name");
    }

    public RoundType getRoundType() {
        return roundType;
    }

    public void setRoundType(RoundType roundType) {
        this.roundType = roundType != null ? roundType : RoundType.TECHNICAL;
    }

    public LocalDate getRoundDate() {
        return roundDate;
    }

    public void setRoundDate(LocalDate roundDate) {
        this.roundDate = roundDate != null ? roundDate : LocalDate.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InterviewRound that = (InterviewRound) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "InterviewRound{" +
                "id='" + id + '\'' +
                ", driveId='" + driveId + '\'' +
                ", roundNumber=" + roundNumber +
                ", roundName='" + roundName + '\'' +
                ", roundType=" + roundType +
                ", roundDate=" + roundDate +
                '}';
    }
}
