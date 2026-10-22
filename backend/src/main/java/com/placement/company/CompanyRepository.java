package com.placement.company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Company entities.
 *
 * Package  : com.placement.company
 * Owner    : Person 3
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findByNameContainingIgnoreCase(String name);

    Optional<Company> findByNameIgnoreCase(String name);

    List<Company> findByActive(Boolean active);
}
