package com.placement.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUserId(Long userId);

    Optional<Student> findByUserEmail(String email);

    Optional<Student> findByRollNumber(String rollNumber);

    Optional<Student> findByEnrollmentNumber(String enrollmentNumber);

    boolean existsByRollNumber(String rollNumber);

    boolean existsByEnrollmentNumber(String enrollmentNumber);

    boolean existsByUserId(Long userId);

    List<Student> findByDepartment(String department);

    List<Student> findByGraduationYear(Integer graduationYear);
}
