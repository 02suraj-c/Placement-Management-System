package com.placement.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<SystemNotification, Long> {

    List<SystemNotification> findByStudentId(Long studentId);

    List<SystemNotification> findByStudentIdOrderByCreatedAtDesc(Long studentId);
}
