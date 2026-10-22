package com.placement.internship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InternshipRepository extends JpaRepository<Internship, Long> {

    List<Internship> findByActiveTrueOrderByCreatedAtDesc();

    List<Internship> findByActiveTrueAndApplicationDeadlineGreaterThanEqualOrderByApplicationDeadlineAsc(LocalDate date);

    List<Internship> findAllByOrderByCreatedAtDesc();

    List<Internship> findByCompanyIgnoreCaseAndActiveTrue(String company);

    @Query("SELECT i FROM Internship i WHERE i.active = true AND (" +
            "LOWER(i.title) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
            "LOWER(i.company) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
            "LOWER(i.requiredSkills) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
            "LOWER(i.location) LIKE LOWER(CONCAT('%', :kw, '%')))" +
            " ORDER BY i.createdAt DESC")
    List<Internship> searchActiveInternships(@Param("kw") String keyword);

    @Query("SELECT i FROM Internship i WHERE i.active = true AND " +
            "LOWER(i.location) LIKE LOWER(CONCAT('%', :loc, '%')) " +
            "ORDER BY i.createdAt DESC")
    List<Internship> findActiveByLocation(@Param("loc") String location);
}
