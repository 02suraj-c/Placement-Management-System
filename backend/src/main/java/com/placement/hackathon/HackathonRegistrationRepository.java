package com.placement.hackathon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HackathonRegistrationRepository extends JpaRepository<HackathonRegistration, Long> {

    List<HackathonRegistration> findByHackathonId(Long hackathonId);

    List<HackathonRegistration> findByStudentIdOrderByRegisteredAtDesc(Long studentId);

    boolean existsByHackathonIdAndStudentId(Long hackathonId, Long studentId);

    long countByHackathonId(Long hackathonId);
}
