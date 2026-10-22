package com.placement.placement;

import com.placement.company.Company;
import com.placement.company.CompanyRepository;
import com.placement.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for Placement Drive management.
 *
 * Package  : com.placement.placement
 * Owner    : Person 3
 */
@Service
@Transactional
public class PlacementService {

    private final PlacementRepository placementRepository;
    private final CompanyRepository companyRepository;

    public PlacementService(PlacementRepository placementRepository,
                            CompanyRepository companyRepository) {
        this.placementRepository = placementRepository;
        this.companyRepository = companyRepository;
    }

    /**
     * Creates a new placement drive.
     * Verifies company existence, validates date constraints and numeric parameters.
     */
    public PlacementDrive createPlacementDrive(PlacementDrive placementDrive) {
        if (placementDrive.getCompany() == null || placementDrive.getCompany().getId() == null) {
            throw new IllegalArgumentException("A valid company with an existing ID is required for a placement drive.");
        }

        Long companyId = placementDrive.getCompany().getId();
        Company verifiedCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        placementDrive.setCompany(verifiedCompany);

        validateDriveBusinessRules(placementDrive);

        if (placementDrive.getStatus() == null) {
            placementDrive.setStatus(DriveStatus.UPCOMING);
        }
        if (placementDrive.getMinimumCgpa() == null) {
            placementDrive.setMinimumCgpa(0.0);
        }
        if (placementDrive.getMaximumAllowedBacklogs() == null) {
            placementDrive.setMaximumAllowedBacklogs(0);
        }

        return placementRepository.save(placementDrive);
    }

    /**
     * Retrieves all placement drives.
     */
    @Transactional(readOnly = true)
    public List<PlacementDrive> getAllPlacementDrives() {
        return placementRepository.findAll();
    }

    /**
     * Retrieves a single placement drive by ID.
     */
    @Transactional(readOnly = true)
    public PlacementDrive getPlacementDriveById(Long id) {
        return placementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Placement drive not found with id: " + id));
    }

    /**
     * Updates an existing placement drive. Preserves ID and createdAt.
     */
    public PlacementDrive updatePlacementDrive(Long id, PlacementDrive updatedDrive) {
        PlacementDrive existing = getPlacementDriveById(id);

        if (updatedDrive.getCompany() != null && updatedDrive.getCompany().getId() != null) {
            Long targetCompanyId = updatedDrive.getCompany().getId();
            Company verifiedCompany = companyRepository.findById(targetCompanyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + targetCompanyId));
            existing.setCompany(verifiedCompany);
        }

        validateDriveBusinessRules(updatedDrive);

        existing.setJobTitle(updatedDrive.getJobTitle());
        existing.setJobDescription(updatedDrive.getJobDescription());
        existing.setJobLocation(updatedDrive.getJobLocation());
        existing.setSalaryPackage(updatedDrive.getSalaryPackage());
        existing.setEmploymentType(updatedDrive.getEmploymentType());
        existing.setApplicationStartDate(updatedDrive.getApplicationStartDate());
        existing.setApplicationEndDate(updatedDrive.getApplicationEndDate());
        existing.setDriveDate(updatedDrive.getDriveDate());
        existing.setVacancies(updatedDrive.getVacancies());
        existing.setMinimumCgpa(updatedDrive.getMinimumCgpa() != null ? updatedDrive.getMinimumCgpa() : 0.0);
        existing.setMaximumAllowedBacklogs(updatedDrive.getMaximumAllowedBacklogs() != null ? updatedDrive.getMaximumAllowedBacklogs() : 0);
        existing.setEligibleBranches(updatedDrive.getEligibleBranches());

        if (updatedDrive.getStatus() != null) {
            existing.setStatus(updatedDrive.getStatus());
        }

        return placementRepository.save(existing);
    }

    /**
     * Deletes a placement drive by ID. Does NOT delete the associated company.
     */
    public void deletePlacementDrive(Long id) {
        PlacementDrive drive = getPlacementDriveById(id);
        placementRepository.delete(drive);
    }

    /**
     * Retrieves all drives for a specific company.
     */
    @Transactional(readOnly = true)
    public List<PlacementDrive> getDrivesByCompany(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Company not found with id: " + companyId);
        }
        return placementRepository.findByCompanyId(companyId);
    }

    /**
     * Retrieves all drives filtered by status.
     */
    @Transactional(readOnly = true)
    public List<PlacementDrive> getDrivesByStatus(DriveStatus status) {
        return placementRepository.findByStatus(status);
    }

    /**
     * Searches drives by job title keyword.
     */
    @Transactional(readOnly = true)
    public List<PlacementDrive> searchDrivesByJobTitle(String jobTitle) {
        if (jobTitle == null || jobTitle.trim().isEmpty()) {
            return placementRepository.findAll();
        }
        return placementRepository.findByJobTitleContainingIgnoreCase(jobTitle.trim());
    }

    private void validateDriveBusinessRules(PlacementDrive drive) {
        if (drive.getApplicationStartDate() != null && drive.getApplicationEndDate() != null) {
            if (drive.getApplicationStartDate().isAfter(drive.getApplicationEndDate())) {
                throw new IllegalArgumentException(
                        "Application start date (" + drive.getApplicationStartDate() +
                                ") cannot be after application end date (" + drive.getApplicationEndDate() + ")."
                );
            }
        }

        if (drive.getApplicationEndDate() != null && drive.getDriveDate() != null) {
            if (drive.getApplicationEndDate().isAfter(drive.getDriveDate())) {
                throw new IllegalArgumentException(
                        "Application end date (" + drive.getApplicationEndDate() +
                                ") cannot be after drive date (" + drive.getDriveDate() + ")."
                );
            }
        }

        if (drive.getSalaryPackage() != null && drive.getSalaryPackage() <= 0) {
            throw new IllegalArgumentException("Salary package must be positive.");
        }

        if (drive.getVacancies() != null && drive.getVacancies() <= 0) {
            throw new IllegalArgumentException("Vacancies count must be positive.");
        }

        if (drive.getMinimumCgpa() != null && (drive.getMinimumCgpa() < 0.0 || drive.getMinimumCgpa() > 10.0)) {
            throw new IllegalArgumentException("Minimum CGPA must be between 0.0 and 10.0.");
        }

        if (drive.getMaximumAllowedBacklogs() != null && drive.getMaximumAllowedBacklogs() < 0) {
            throw new IllegalArgumentException("Maximum allowed backlogs cannot be negative.");
        }
    }
}
