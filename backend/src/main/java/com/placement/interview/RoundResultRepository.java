package com.placement.interview;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoundResultRepository extends JpaRepository<RoundResult, Long> {

    Optional<RoundResult> findByRoundId(Long roundId);

    List<RoundResult> findAllByRoundId(Long roundId);
}
