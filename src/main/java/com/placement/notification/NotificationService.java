package com.placement.notification;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public SystemNotification createNotification(Long studentId, String message) {
        SystemNotification notification = new SystemNotification(studentId, message);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<SystemNotification> getNotificationsByStudent(Long studentId) {
        return notificationRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    public SystemNotification markAsRead(Long id) {
        SystemNotification notification = notificationRepository.findById(id)
                .orElseThrow(() -> {
                    // TODO: switch to ResourceNotFoundException
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found with id: " + id);
                });

        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }
}
