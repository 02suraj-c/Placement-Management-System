package com.placement.notification;

import com.placement.exception.ResourceNotFoundException;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
import com.placement.user.User;
import com.placement.user.UserRepository;
import com.placement.user.UserRole;
import com.placement.util.YearUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public SystemNotification createNotification(Long studentId, String message) {
        SystemNotification notification = new SystemNotification(studentId, message);
        return notificationRepository.save(notification);
    }

    public SystemNotification broadcastToYear(String targetYears, String message) {
        SystemNotification notification = new SystemNotification(null, targetYears, message);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<SystemNotification> getNotificationsByStudent(Long studentId) {
        return getNotificationsByStudent(studentId, null);
    }

    @Transactional(readOnly = true)
    public List<SystemNotification> getNotificationsByStudent(Long studentId, String currentUserEmail) {
        validateStudentOwnership(studentId, currentUserEmail);
        return notificationRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Transactional(readOnly = true)
    public List<SystemNotification> getNotificationsForStudent(Long studentId, Integer academicYear) {
        return getNotificationsForStudent(studentId, academicYear, null);
    }

    @Transactional(readOnly = true)
    public List<SystemNotification> getNotificationsForStudent(Long studentId, Integer academicYear, String currentUserEmail) {
        validateStudentOwnership(studentId, currentUserEmail);

        List<SystemNotification> result = new ArrayList<>(notificationRepository.findByStudentIdOrderByCreatedAtDesc(studentId));
        List<SystemNotification> broadcasts = notificationRepository.findByStudentIdIsNullOrderByCreatedAtDesc();

        for (SystemNotification n : broadcasts) {
            if (academicYear == null || YearUtils.matchesYear(n.getTargetYears(), academicYear)) {
                result.add(n);
            }
        }
        result.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return result;
    }

    public SystemNotification markAsRead(Long id) {
        return markAsRead(id, null);
    }

    public SystemNotification markAsRead(Long id, String currentUserEmail) {
        SystemNotification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SystemNotification", "id", id));

        if (currentUserEmail != null && notification.getStudentId() != null) {
            validateStudentOwnership(notification.getStudentId(), currentUserEmail);
        }

        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    public int markAllAsRead(Long studentId, String currentUserEmail) {
        validateStudentOwnership(studentId, currentUserEmail);

        List<SystemNotification> unread = notificationRepository.findByStudentIdAndIsReadFalse(studentId);
        for (SystemNotification n : unread) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(unread);
        return unread.size();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long studentId, String currentUserEmail) {
        validateStudentOwnership(studentId, currentUserEmail);
        return notificationRepository.countByStudentIdAndIsReadFalse(studentId);
    }

    private void validateStudentOwnership(Long studentId, String currentUserEmail) {
        if (currentUserEmail == null || studentId == null) {
            return;
        }
        User user = userRepository.findByEmail(currentUserEmail).orElse(null);
        if (user != null && user.getRole() == UserRole.ROLE_STUDENT) {
            Student student = studentRepository.findByUserEmail(currentUserEmail).orElse(null);
            if (student != null && !student.getId().equals(studentId)) {
                throw new AccessDeniedException("You are not authorized to view or manage notifications for another student");
            }
        }
    }
}
