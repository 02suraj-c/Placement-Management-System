package com.placement.notification;

import com.placement.student.Student;
import com.placement.student.StudentRepository;
import com.placement.user.User;
import com.placement.user.UserRepository;
import com.placement.user.UserRole;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public NotificationController(NotificationService notificationService,
                                  UserRepository userRepository,
                                  StudentRepository studentRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<SystemNotification>> getNotificationsByStudent(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails != null ? userDetails.getUsername() : null;
        Integer academicYear = null;
        if (email != null) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null && user.getRole() == UserRole.ROLE_STUDENT) {
                Student student = studentRepository.findByUserEmail(email).orElse(null);
                if (student != null) {
                    academicYear = student.getAcademicYear();
                }
            }
        }
        if (academicYear == null) {
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student != null) {
                academicYear = student.getAcademicYear();
            }
        }

        List<SystemNotification> notifications = notificationService.getNotificationsForStudent(studentId, academicYear, email);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<SystemNotification> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        SystemNotification notification = notificationService.markAsRead(id, email);
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/student/{studentId}/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        int count = notificationService.markAllAsRead(studentId, email);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read", "count", count));
    }

    @GetMapping("/student/{studentId}/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        long count = notificationService.getUnreadCount(studentId, email);
        return ResponseEntity.ok(Map.of("studentId", studentId, "unreadCount", count));
    }
}
