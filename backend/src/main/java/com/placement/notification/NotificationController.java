package com.placement.notification;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<SystemNotification>> getNotificationsByStudent(@PathVariable Long studentId) {
        List<SystemNotification> notifications = notificationService.getNotificationsByStudent(studentId);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<SystemNotification> markAsRead(@PathVariable Long id) {
        SystemNotification notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(notification);
    }
}
