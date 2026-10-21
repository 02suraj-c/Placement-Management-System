package org.example.repository;

import org.example.model.Student;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Student data access.
 */
public interface StudentRepository {
    Student save(Student student);
    Optional<Student> findById(String id);
    List<Student> findAll();
    boolean existsById(String id);
    boolean deleteById(String id);
    void clear();
}
