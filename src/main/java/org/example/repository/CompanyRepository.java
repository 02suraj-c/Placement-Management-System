package org.example.repository;

import org.example.model.Company;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Company data access.
 */
public interface CompanyRepository {
    Company save(Company company);
    Optional<Company> findById(String id);
    List<Company> findAll();
    List<Company> findByNameContainingIgnoreCase(String name);
    boolean existsById(String id);
    boolean deleteById(String id);
    void clear();
}
