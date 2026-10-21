package org.example.model;

import org.example.util.ValidationUtil;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a student's application to a placement drive.
 */
public class Application {
    private String id;
    private String driveId;
    private String studentId;
    private LocalDate appliedDate;
    private ApplicationStatus status;
    private String remarks;

    public Application() {
        this.status = ApplicationStatus.APPLIED;
        this.appliedDate = LocalDate.now();
    }

    public Application(String id, String driveId, String studentId, LocalDate appliedDate, ApplicationStatus status, String remarks) {
        this.id = id != null ? id.trim() : null;
        this.driveId = ValidationUtil.requireNonBlank(driveId, "Drive ID");
        this.studentId = ValidationUtil.requireNonBlank(studentId, "Student ID");
        this.appliedDate = appliedDate != null ? appliedDate : LocalDate.now();
        this.status = status != null ? status : ApplicationStatus.APPLIED;
        this.remarks = remarks != null ? remarks.trim() : "";
    }

    public Application(String driveId, String studentId, LocalDate appliedDate) {
        this(null, driveId, studentId, appliedDate, ApplicationStatus.APPLIED, "");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = ValidationUtil.requireNonBlank(id, "Application ID");
    }

    public String getDriveId() {
        return driveId;
    }

    public void setDriveId(String driveId) {
        this.driveId = ValidationUtil.requireNonBlank(driveId, "Drive ID");
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = ValidationUtil.requireNonBlank(studentId, "Student ID");
    }

    public LocalDate getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(LocalDate appliedDate) {
        this.appliedDate = appliedDate != null ? appliedDate : LocalDate.now();
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status != null ? status : ApplicationStatus.APPLIED;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks != null ? remarks.trim() : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Application that = (Application) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Application{" +
                "id='" + id + '\'' +
                ", driveId='" + driveId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", appliedDate=" + appliedDate +
                ", status=" + status +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}
