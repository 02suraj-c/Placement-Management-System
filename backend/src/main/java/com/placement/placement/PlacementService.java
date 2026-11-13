package com.placement.placement;

import com.placement.company.Company;
import com.placement.company.CompanyRepository;
import com.placement.exception.ResourceNotFoundException;
import com.placement.util.YearUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for Placement Drive management.
 *
 * Package  : com.placement.placement
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
     * Retrieves all placement drives eligible for a specific academic year (1-4).
     */
    @Transactional(readOnly = true)
    public List<PlacementDrive> getDrivesForYear(Integer year) {
        List<PlacementDrive> all = placementRepository.findAll();
        if (year == null || year <= 0) {
            return all;
        }
        return all.stream()
                .filter(drive -> YearUtils.matchesYear(drive.getTargetYears(), year))
                .toList();
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
        existing.setTargetYears(updatedDrive.getTargetYears());

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
    public List<PlacementDrive> searchDrivesByTitle(String keyword) {
        return placementRepository.findByJobTitleContainingIgnoreCase(keyword);
    }

    @Transactional(readOnly = true)
    public List<PlacementDrive> searchDrivesByJobTitle(String jobTitle) {
        return searchDrivesByTitle(jobTitle);
    }

    /**
     * Enforces domain business rules:
     *  - applicationEndDate must be after or equal to applicationStartDate
     *  - driveDate must be on or after applicationEndDate
     */
    private void validateDriveBusinessRules(PlacementDrive drive) {
        if (drive.getApplicationStartDate() != null && drive.getApplicationEndDate() != null) {
            if (drive.getApplicationEndDate().isBefore(drive.getApplicationStartDate())) {
                throw new IllegalArgumentException(
                        "Application end date (" + drive.getApplicationEndDate() +
                        ") must be on or after application start date (" + drive.getApplicationStartDate() + ")."
                );
            }
        }

        if (drive.getDriveDate() != null && drive.getApplicationEndDate() != null) {
            if (drive.getDriveDate().isBefore(drive.getApplicationEndDate())) {
                throw new IllegalArgumentException(
                        "Drive date (" + drive.getDriveDate() +
                        ") must be on or after application end date (" + drive.getApplicationEndDate() + ")."
                );
            }
        }
    }
}
