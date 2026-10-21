package com.placement.resume;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<StudentResume, Long> {

    Optional<StudentResume> findByStudentIdAndActiveTrue(Long studentId);

    Optional<StudentResume> findByStudentUserEmailAndActiveTrue(String userEmail);

    List<StudentResume> findByStudentIdOrderByUploadedAtDesc(Long studentId);

    Optional<StudentResume> findByStoredFileName(String storedFileName);
}
