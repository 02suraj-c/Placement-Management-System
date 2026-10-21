package com.placement.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<PlacementApplication, Long> {

    List<PlacementApplication> findByStudentId(Long studentId);

    List<PlacementApplication> findByDriveId(Long driveId);

    boolean existsByStudentIdAndDriveId(Long studentId, Long driveId);

    Optional<PlacementApplication> findByStudentIdAndDriveId(Long studentId, Long driveId);
}
