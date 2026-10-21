package org.example.service;

import org.example.exception.CompanyNotFoundException;
import org.example.exception.PlacementDriveNotFoundException;
import org.example.exception.ValidationException;
import org.example.model.DriveStatus;
import org.example.model.PlacementDrive;
import org.example.repository.CompanyRepository;
import org.example.repository.PlacementDriveRepository;
import org.example.util.ValidationUtil;

import java.util.List;
import java.util.Objects;

/**
 * Implementation of PlacementDriveService.
 */
public class PlacementDriveServiceImpl implements PlacementDriveService {
    private final PlacementDriveRepository driveRepository;
    private final CompanyRepository companyRepository;

    public PlacementDriveServiceImpl(PlacementDriveRepository driveRepository, CompanyRepository companyRepository) {
        this.driveRepository = Objects.requireNonNull(driveRepository, "PlacementDriveRepository must not be null");
        this.companyRepository = Objects.requireNonNull(companyRepository, "CompanyRepository must not be null");
    }

    @Override
    public PlacementDrive createDrive(PlacementDrive drive) {
        if (drive == null) {
            throw new ValidationException("Placement drive details must not be null");
        }

        ValidationUtil.requireNonBlank(drive.getCompanyId(), "Company ID");
        if (!companyRepository.existsById(drive.getCompanyId())) {
            throw new CompanyNotFoundException("Cannot create drive. Linked company not found with ID: " + drive.getCompanyId());
        }

        ValidationUtil.requireNonBlank(drive.getJobRole(), "Job role");
        ValidationUtil.validateNonNegative(drive.getPackageCTC(), "Package / CTC");
        ValidationUtil.validateCGPA(drive.getMinimumCGPA(), "Minimum CGPA");
        ValidationUtil.validateNonNegative(drive.getMaximumAllowedBacklogs(), "Maximum allowed backlogs");

        if (drive.getEligibleBranches() == null || drive.getEligibleBranches().isEmpty()) {
            throw new ValidationException("At least one eligible branch must be specified for the drive");
        }

        ValidationUtil.validateDateOrder(drive.getApplicationDeadline(), drive.getDriveDate());

        if (drive.getStatus() == null) {
            drive.setStatus(DriveStatus.SCHEDULED);
        }

        return driveRepository.save(drive);
    }

    @Override
    public PlacementDrive getDriveById(String driveId) {
        ValidationUtil.requireNonBlank(driveId, "Drive ID");
        return driveRepository.findById(driveId)
                .orElseThrow(() -> new PlacementDriveNotFoundException("Placement drive not found with ID: " + driveId));
    }

    @Override
    public List<PlacementDrive> getAllDrives() {
        return driveRepository.findAll();
    }

    @Override
    public List<PlacementDrive> getDrivesByCompany(String companyId) {
        ValidationUtil.requireNonBlank(companyId, "Company ID");
        return driveRepository.findByCompanyId(companyId);
    }

    @Override
    public PlacementDrive updateDrive(PlacementDrive drive) {
        if (drive == null) {
            throw new ValidationException("Placement drive cannot be null for update");
        }
        ValidationUtil.requireNonBlank(drive.getId(), "Drive ID");

        PlacementDrive existing = driveRepository.findById(drive.getId())
                .orElseThrow(() -> new PlacementDriveNotFoundException("Cannot update. Drive not found with ID: " + drive.getId()));

        if (existing.getStatus() == DriveStatus.CANCELLED) {
            throw new ValidationException("Cannot update a cancelled placement drive (ID: " + drive.getId() + ")");
        }

        if (drive.getCompanyId() != null && !drive.getCompanyId().trim().isEmpty()) {
            if (!companyRepository.existsById(drive.getCompanyId())) {
                throw new CompanyNotFoundException("Cannot update drive. Company not found with ID: " + drive.getCompanyId());
            }
            existing.setCompanyId(drive.getCompanyId().trim());
        }

        if (drive.getJobRole() != null) {
            existing.setJobRole(ValidationUtil.requireNonBlank(drive.getJobRole(), "Job role"));
        }
        existing.setPackageCTC(ValidationUtil.validateNonNegative(drive.getPackageCTC(), "Package / CTC"));
        existing.setJobDescription(drive.getJobDescription() != null ? drive.getJobDescription().trim() : "");
        existing.setMinimumCGPA(ValidationUtil.validateCGPA(drive.getMinimumCGPA(), "Minimum CGPA"));
        existing.setMaximumAllowedBacklogs(ValidationUtil.validateNonNegative(drive.getMaximumAllowedBacklogs(), "Maximum allowed backlogs"));

        if (drive.getApplicationDeadline() != null && drive.getDriveDate() != null) {
            ValidationUtil.validateDateOrder(drive.getApplicationDeadline(), drive.getDriveDate());
            existing.setApplicationDeadline(drive.getApplicationDeadline());
            existing.setDriveDate(drive.getDriveDate());
        } else if (drive.getApplicationDeadline() != null) {
            ValidationUtil.validateDateOrder(drive.getApplicationDeadline(), existing.getDriveDate());
            existing.setApplicationDeadline(drive.getApplicationDeadline());
        } else if (drive.getDriveDate() != null) {
            ValidationUtil.validateDateOrder(existing.getApplicationDeadline(), drive.getDriveDate());
            existing.setDriveDate(drive.getDriveDate());
        }

        if (drive.getEligibleBranches() != null && !drive.getEligibleBranches().isEmpty()) {
            existing.setEligibleBranches(drive.getEligibleBranches());
        }

        if (drive.getStatus() != null) {
            existing.setStatus(drive.getStatus());
        }

        return driveRepository.save(existing);
    }

    @Override
    public PlacementDrive cancelDrive(String driveId) {
        ValidationUtil.requireNonBlank(driveId, "Drive ID");
        PlacementDrive drive = getDriveById(driveId);
        drive.setStatus(DriveStatus.CANCELLED);
        return driveRepository.save(drive);
    }
}
