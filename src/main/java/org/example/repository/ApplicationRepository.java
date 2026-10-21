package org.example.repository;

import org.example.model.Application;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Application data access.
 */
public interface ApplicationRepository {
    Application save(Application application);
    Optional<Application> findById(String id);
    List<Application> findAll();
    List<Application> findByStudentId(String studentId);
    List<Application> findByDriveId(String driveId);
    Optional<Application> findByDriveIdAndStudentId(String driveId, String studentId);
    boolean existsByDriveIdAndStudentId(String driveId, String studentId);
    boolean deleteById(String id);
    void clear();
}
