package com.placement.hackathon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HackathonRepository extends JpaRepository<Hackathon, Long> {

    List<Hackathon> findByActiveTrueOrderByStartDateAsc();

    List<Hackathon> findByActiveTrueAndStatusOrderByStartDateAsc(HackathonStatus status);

    List<Hackathon> findAllByOrderByCreatedAtDesc();

    List<Hackathon> findByActiveTrueAndModeOrderByStartDateAsc(HackathonMode mode);

    @Query("SELECT h FROM Hackathon h WHERE h.active = true AND (" +
            "LOWER(h.title) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
            "LOWER(h.organizer) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
            "LOWER(h.description) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
            "LOWER(h.location) LIKE LOWER(CONCAT('%', :kw, '%')))" +
            " ORDER BY h.startDate ASC")
    List<Hackathon> searchActiveHackathons(@Param("kw") String keyword);
}
