package org.example.repository;

import org.example.model.PlacementDrive;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PlacementDrive data access.
 */
public interface PlacementDriveRepository {
    PlacementDrive save(PlacementDrive drive);
    Optional<PlacementDrive> findById(String id);
    List<PlacementDrive> findAll();
    List<PlacementDrive> findByCompanyId(String companyId);
    boolean existsById(String id);
    boolean deleteById(String id);
    void clear();
}
