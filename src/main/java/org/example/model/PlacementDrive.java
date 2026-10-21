package org.example.model;

import org.example.util.ValidationUtil;

import java.time.LocalDate;
import java.util.*;

/**
 * Represents a placement drive conducted by a company for eligible students.
 */
public class PlacementDrive {
    private String id;
    private String companyId;
    private String jobRole;
    private double packageCTC;
    private String jobDescription;
    private LocalDate applicationDeadline;
    private LocalDate driveDate;
    private double minimumCGPA;
    private int maximumAllowedBacklogs;
    private Set<String> eligibleBranches = new HashSet<>();
    private DriveStatus status = DriveStatus.SCHEDULED;

    public PlacementDrive() {
    }

    public PlacementDrive(String id, String companyId, String jobRole, double packageCTC,
                          String jobDescription, LocalDate applicationDeadline, LocalDate driveDate,
                          double minimumCGPA, int maximumAllowedBacklogs, Set<String> eligibleBranches) {
        this.id = id != null ? id.trim() : null;
        this.companyId = ValidationUtil.requireNonBlank(companyId, "Company ID");
        this.jobRole = ValidationUtil.requireNonBlank(jobRole, "Job role");
        this.packageCTC = ValidationUtil.validateNonNegative(packageCTC, "Package / CTC");
        this.jobDescription = jobDescription != null ? jobDescription.trim() : "";
        ValidationUtil.validateDateOrder(applicationDeadline, driveDate);
        this.applicationDeadline = applicationDeadline;
        this.driveDate = driveDate;
        this.minimumCGPA = ValidationUtil.validateCGPA(minimumCGPA, "Minimum CGPA");
        this.maximumAllowedBacklogs = ValidationUtil.validateNonNegative(maximumAllowedBacklogs, "Maximum allowed backlogs");
        setEligibleBranches(eligibleBranches);
        this.status = DriveStatus.SCHEDULED;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = ValidationUtil.requireNonBlank(id, "Drive ID");
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = ValidationUtil.requireNonBlank(companyId, "Company ID");
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = ValidationUtil.requireNonBlank(jobRole, "Job role");
    }

    public double getPackageCTC() {
        return packageCTC;
    }

    public void setPackageCTC(double packageCTC) {
        this.packageCTC = ValidationUtil.validateNonNegative(packageCTC, "Package / CTC");
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription != null ? jobDescription.trim() : "";
    }

    public LocalDate getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(LocalDate applicationDeadline) {
        if (driveDate != null && applicationDeadline != null) {
            ValidationUtil.validateDateOrder(applicationDeadline, driveDate);
        }
        this.applicationDeadline = applicationDeadline;
    }

    public LocalDate getDriveDate() {
        return driveDate;
    }

    public void setDriveDate(LocalDate driveDate) {
        if (applicationDeadline != null && driveDate != null) {
            ValidationUtil.validateDateOrder(applicationDeadline, driveDate);
        }
        this.driveDate = driveDate;
    }

    public double getMinimumCGPA() {
        return minimumCGPA;
    }

    public void setMinimumCGPA(double minimumCGPA) {
        this.minimumCGPA = ValidationUtil.validateCGPA(minimumCGPA, "Minimum CGPA");
    }

    public int getMaximumAllowedBacklogs() {
        return maximumAllowedBacklogs;
    }

    public void setMaximumAllowedBacklogs(int maximumAllowedBacklogs) {
        this.maximumAllowedBacklogs = ValidationUtil.validateNonNegative(maximumAllowedBacklogs, "Maximum allowed backlogs");
    }

    public Set<String> getEligibleBranches() {
        return Collections.unmodifiableSet(eligibleBranches);
    }

    public void setEligibleBranches(Set<String> branches) {
        this.eligibleBranches.clear();
        if (branches != null) {
            for (String b : branches) {
                if (b != null && !b.trim().isEmpty()) {
                    this.eligibleBranches.add(b.trim().toUpperCase());
                }
            }
        }
    }

    public void addEligibleBranch(String branch) {
        if (branch != null && !branch.trim().isEmpty()) {
            this.eligibleBranches.add(branch.trim().toUpperCase());
        }
    }

    public DriveStatus getStatus() {
        return status;
    }

    public void setStatus(DriveStatus status) {
        this.status = status != null ? status : DriveStatus.SCHEDULED;
    }

    public boolean isCancelled() {
        return status == DriveStatus.CANCELLED;
    }

    public boolean isBranchEligible(String branch) {
        if (branch == null || eligibleBranches.isEmpty()) {
            return false;
        }
        return eligibleBranches.contains(branch.trim().toUpperCase());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlacementDrive that = (PlacementDrive) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PlacementDrive{" +
                "id='" + id + '\'' +
                ", companyId='" + companyId + '\'' +
                ", jobRole='" + jobRole + '\'' +
                ", packageCTC=" + packageCTC +
                ", applicationDeadline=" + applicationDeadline +
                ", driveDate=" + driveDate +
                ", minimumCGPA=" + minimumCGPA +
                ", maximumAllowedBacklogs=" + maximumAllowedBacklogs +
                ", eligibleBranches=" + eligibleBranches +
                ", status=" + status +
                '}';
    }
}
