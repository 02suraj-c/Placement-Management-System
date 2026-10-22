package com.placement.placement;

import com.placement.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for PlacementDrive entities.
 *
 * Package  : com.placement.placement
 * Owner    : Person 3
 */
@Repository
public interface PlacementRepository extends JpaRepository<PlacementDrive, Long> {

    List<PlacementDrive> findByCompanyId(Long companyId);

    List<PlacementDrive> findByCompany(Company company);

    List<PlacementDrive> findByStatus(DriveStatus status);

    List<PlacementDrive> findByJobTitleContainingIgnoreCase(String jobTitle);

    List<PlacementDrive> findByStatusOrderByDriveDateAsc(DriveStatus status);
}
