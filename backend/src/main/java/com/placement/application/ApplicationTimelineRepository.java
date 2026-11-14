package com.placement.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationTimelineRepository extends JpaRepository<ApplicationTimelineEvent, Long> {

    List<ApplicationTimelineEvent> findByApplicationIdOrderByCreatedAtAsc(Long applicationId);

    void deleteByApplicationId(Long applicationId);
}

