package com.placement.notification;

import com.placement.exception.ResourceNotFoundException;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
import com.placement.user.User;
import com.placement.user.UserRepository;
import com.placement.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User studentUser;
    private Student studentProfile;
    private SystemNotification sampleNotification;

    @BeforeEach
    void setUp() {
        studentUser = User.builder()
                .id(1L)
                .email("student@college.edu")
                .role(UserRole.ROLE_STUDENT)
                .build();

        studentProfile = Student.builder()
                .id(10L)
                .user(studentUser)
                .academicYear(4)
                .build();

        sampleNotification = new SystemNotification(10L, "Test Message");
        sampleNotification.setId(100L);
        sampleNotification.setIsRead(false);
        sampleNotification.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create personal notification")
    void createNotification_success() {
        when(notificationRepository.save(any(SystemNotification.class))).thenAnswer(i -> i.getArgument(0));

        SystemNotification created = notificationService.createNotification(10L, "New interview scheduled");

        assertNotNull(created);
        assertEquals(10L, created.getStudentId());
        assertEquals("New interview scheduled", created.getMessage());
    }

    @Test
    @DisplayName("Should mark own notification as read")
    void markAsRead_ownNotification_success() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(sampleNotification));
        when(userRepository.findByEmail("student@college.edu")).thenReturn(Optional.of(studentUser));
        when(studentRepository.findByUserEmail("student@college.edu")).thenReturn(Optional.of(studentProfile));
        when(notificationRepository.save(any(SystemNotification.class))).thenAnswer(i -> i.getArgument(0));

        SystemNotification updated = notificationService.markAsRead(100L, "student@college.edu");

        assertTrue(updated.getIsRead());
    }

    @Test
    @DisplayName("Should prevent student from marking another student's notification as read")
    void markAsRead_otherStudent_throwsAccessDenied() {
        Student otherStudent = Student.builder().id(999L).build();

        when(notificationRepository.findById(100L)).thenReturn(Optional.of(sampleNotification));
        when(userRepository.findByEmail("student@college.edu")).thenReturn(Optional.of(studentUser));
        when(studentRepository.findByUserEmail("student@college.edu")).thenReturn(Optional.of(otherStudent));

        assertThrows(AccessDeniedException.class, () ->
                notificationService.markAsRead(100L, "student@college.edu")
        );
    }

    @Test
    @DisplayName("Should calculate unread count for student with ownership check")
    void getUnreadCount_success() {
        when(userRepository.findByEmail("student@college.edu")).thenReturn(Optional.of(studentUser));
        when(studentRepository.findByUserEmail("student@college.edu")).thenReturn(Optional.of(studentProfile));
        when(notificationRepository.countByStudentIdAndIsReadFalse(10L)).thenReturn(5L);

        long count = notificationService.getUnreadCount(10L, "student@college.edu");

        assertEquals(5L, count);
    }

    @Test
    @DisplayName("Should mark all unread notifications as read for student")
    void markAllAsRead_success() {
        when(userRepository.findByEmail("student@college.edu")).thenReturn(Optional.of(studentUser));
        when(studentRepository.findByUserEmail("student@college.edu")).thenReturn(Optional.of(studentProfile));

        SystemNotification n1 = new SystemNotification(10L, "Msg 1");
        n1.setIsRead(false);
        SystemNotification n2 = new SystemNotification(10L, "Msg 2");
        n2.setIsRead(false);

        when(notificationRepository.findByStudentIdAndIsReadFalse(10L)).thenReturn(List.of(n1, n2));

        int updatedCount = notificationService.markAllAsRead(10L, "student@college.edu");

        assertEquals(2, updatedCount);
        assertTrue(n1.getIsRead());
        assertTrue(n2.getIsRead());
        verify(notificationRepository).saveAll(any());
    }
}

