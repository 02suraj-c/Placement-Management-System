package com.placement.placement;

import com.placement.company.Company;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entity representing a recruitment/placement drive conducted by a company.
 *
 * Package  : com.placement.placement
 * Owner    : Person 3
 * Table    : placement_drive
 */
@Entity
@Table(name = "placement_drive")
public class PlacementDrive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Company is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotBlank(message = "Job title is required")
    @Size(max = 150, message = "Job title must not exceed 150 characters")
    @Column(name = "job_title", nullable = false, length = 150)
    private String jobTitle;

    @NotBlank(message = "Job description is required")
    @Size(max = 2000, message = "Job description must not exceed 2000 characters")
    @Column(name = "job_description", nullable = false, length = 2000)
    private String jobDescription;

    @NotBlank(message = "Job location is required")
    @Size(max = 150, message = "Job location must not exceed 150 characters")
    @Column(name = "job_location", nullable = false, length = 150)
    private String jobLocation;

    @NotNull(message = "Salary package is required")
    @Positive(message = "Salary package must be positive")
    @Column(name = "salary_package", nullable = false)
    private Double salaryPackage;

    @NotBlank(message = "Employment type is required")
    @Size(max = 50, message = "Employment type must not exceed 50 characters")
    @Column(name = "employment_type", nullable = false, length = 50)
    private String employmentType;

    @NotNull(message = "Application start date is required")
    @Column(name = "application_start_date", nullable = false)
    private LocalDate applicationStartDate;

    @NotNull(message = "Application end date is required")
    @Column(name = "application_end_date", nullable = false)
    private LocalDate applicationEndDate;

    @NotNull(message = "Drive date is required")
    @Column(name = "drive_date", nullable = false)
    private LocalDate driveDate;

    @NotNull(message = "Vacancies count is required")
    @Min(value = 1, message = "Vacancies must be at least 1")
    @Positive(message = "Vacancies must be positive")
    @Column(name = "vacancies", nullable = false)
    private Integer vacancies;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DriveStatus status = DriveStatus.UPCOMING;

    @NotNull(message = "Minimum CGPA is required")
    @DecimalMin(value = "0.0", message = "Minimum CGPA must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Minimum CGPA cannot exceed 10.0")
    @Column(name = "minimum_cgpa", nullable = false)
    private Double minimumCgpa = 0.0;

    @NotNull(message = "Maximum allowed backlogs is required")
    @Min(value = 0, message = "Maximum allowed backlogs cannot be negative")
    @Column(name = "maximum_allowed_backlogs", nullable = false)
    private Integer maximumAllowedBacklogs = 0;

    @Size(max = 500, message = "Eligible branches must not exceed 500 characters")
    @Column(name = "eligible_branches", length = 500)
    private String eligibleBranches;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = DriveStatus.UPCOMING;
        }
        if (this.minimumCgpa == null) {
            this.minimumCgpa = 0.0;
        }
        if (this.maximumAllowedBacklogs == null) {
            this.maximumAllowedBacklogs = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public PlacementDrive() {
    }

    public PlacementDrive(Long id, Company company, String jobTitle, String jobDescription,
                          String jobLocation, Double salaryPackage, String employmentType,
                          LocalDate applicationStartDate, LocalDate applicationEndDate,
                          LocalDate driveDate, Integer vacancies, DriveStatus status,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.company = company;
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
        this.jobLocation = jobLocation;
        this.salaryPackage = salaryPackage;
        this.employmentType = employmentType;
        this.applicationStartDate = applicationStartDate;
        this.applicationEndDate = applicationEndDate;
        this.driveDate = driveDate;
        this.vacancies = vacancies;
        this.status = (status != null) ? status : DriveStatus.UPCOMING;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public PlacementDrive(Long id, Company company, String jobTitle, String jobDescription,
                          String jobLocation, Double salaryPackage, String employmentType,
                          LocalDate applicationStartDate, LocalDate applicationEndDate,
                          LocalDate driveDate, Integer vacancies, DriveStatus status,
                          Double minimumCgpa, Integer maximumAllowedBacklogs, String eligibleBranches,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, company, jobTitle, jobDescription, jobLocation, salaryPackage, employmentType,
                applicationStartDate, applicationEndDate, driveDate, vacancies, status, createdAt, updatedAt);
        this.minimumCgpa = (minimumCgpa != null) ? minimumCgpa : 0.0;
        this.maximumAllowedBacklogs = (maximumAllowedBacklogs != null) ? maximumAllowedBacklogs : 0;
        this.eligibleBranches = eligibleBranches;
    }

    public Long getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public String getJobLocation() {
        return jobLocation;
    }

    public Double getSalaryPackage() {
        return salaryPackage;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public LocalDate getApplicationStartDate() {
        return applicationStartDate;
    }

    public LocalDate getApplicationEndDate() {
        return applicationEndDate;
    }

    public LocalDate getDriveDate() {
        return driveDate;
    }

    public Integer getVacancies() {
        return vacancies;
    }

    public DriveStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public void setJobLocation(String jobLocation) {
        this.jobLocation = jobLocation;
    }

    public void setSalaryPackage(Double salaryPackage) {
        this.salaryPackage = salaryPackage;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public void setApplicationStartDate(LocalDate applicationStartDate) {
        this.applicationStartDate = applicationStartDate;
    }

    public void setApplicationEndDate(LocalDate applicationEndDate) {
        this.applicationEndDate = applicationEndDate;
    }

    public void setDriveDate(LocalDate driveDate) {
        this.driveDate = driveDate;
    }

    public void setVacancies(Integer vacancies) {
        this.vacancies = vacancies;
    }

    public void setStatus(DriveStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Double getMinimumCgpa() {
        return minimumCgpa;
    }

    public void setMinimumCgpa(Double minimumCgpa) {
        this.minimumCgpa = minimumCgpa;
    }

    public Integer getMaximumAllowedBacklogs() {
        return maximumAllowedBacklogs;
    }

    public void setMaximumAllowedBacklogs(Integer maximumAllowedBacklogs) {
        this.maximumAllowedBacklogs = maximumAllowedBacklogs;
    }

    public String getEligibleBranches() {
        return eligibleBranches;
    }

    public void setEligibleBranches(String eligibleBranches) {
        this.eligibleBranches = eligibleBranches;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlacementDrive)) return false;
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
                "id=" + id +
                ", company=" + (company != null ? company.getName() : null) +
                ", jobTitle='" + jobTitle + '\'' +
                ", jobLocation='" + jobLocation + '\'' +
                ", salaryPackage=" + salaryPackage +
                ", employmentType='" + employmentType + '\'' +
                ", driveDate=" + driveDate +
                ", vacancies=" + vacancies +
                ", status=" + status +
                ", minimumCgpa=" + minimumCgpa +
                ", maximumAllowedBacklogs=" + maximumAllowedBacklogs +
                ", eligibleBranches='" + eligibleBranches + '\'' +
                '}';
    }
}
