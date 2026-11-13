package com.placement.notification;

import com.placement.auth.AuthService;
import com.placement.auth.dto.UserResponse;
import com.placement.exception.ResourceNotFoundException;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final AuthService authService;
    private final StudentRepository studentRepository;
    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationService notificationService,
                                  AuthService authService,
                                  StudentRepository studentRepository,
                                  NotificationRepository notificationRepository) {
        this.notificationService = notificationService;
        this.authService = authService;
        this.studentRepository = studentRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<SystemNotification>> getNotificationsByStudent(@PathVariable Long studentId) {
        UserResponse currentUser = authService.getCurrentUser();
        Student student = studentRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "userId", currentUser.getId()));

        if (!student.getId().equals(studentId)) {
            throw new AccessDeniedException("You are not authorized to view another student's notifications");
        }

        List<SystemNotification> notifications = notificationService.getNotificationsByStudent(studentId);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SystemNotification> markAsRead(@PathVariable Long id) {
        UserResponse currentUser = authService.getCurrentUser();
        Student student = studentRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "userId", currentUser.getId()));

        SystemNotification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        if (!student.getId().equals(notification.getStudentId())) {
            throw new AccessDeniedException("You are not authorized to update another student's notification");
        }

        SystemNotification updatedNotification = notificationService.markAsRead(id);
        return ResponseEntity.ok(updatedNotification);
    }
}
