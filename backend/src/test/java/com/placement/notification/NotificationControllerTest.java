package com.placement.notification;

import com.placement.auth.AuthService;
import com.placement.auth.dto.UserResponse;
import com.placement.config.CorsConfig;
import com.placement.config.SecurityConfig;
import com.placement.security.CustomUserDetailsService;
import com.placement.security.JwtFilter;
import com.placement.security.JwtService;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
import com.placement.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import({SecurityConfig.class, CorsConfig.class})
@DisplayName("NotificationController Authorization and Security Tests")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private AuthService authService;

    @MockBean
    private StudentRepository studentRepository;

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private UserResponse studentUser;
    private Student studentProfile;
    private SystemNotification ownNotification;
    private SystemNotification otherNotification;
    private SystemNotification readNotification;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());

        studentUser = UserResponse.builder()
                .id(1L)
                .name("Student User")
                .email("student@college.edu")
                .role(UserRole.ROLE_STUDENT)
                .enabled(true)
                .build();

        studentProfile = Student.builder()
                .id(100L)
                .build();

        ownNotification = new SystemNotification(100L, "Your interview is scheduled");
        ownNotification.setId(10L);

        otherNotification = new SystemNotification(200L, "Another student's notification");
        otherNotification.setId(20L);

        readNotification = new SystemNotification(100L, "Your interview is scheduled");
        readNotification.setId(10L);
        readNotification.setIsRead(true);
    }

    // ==========================================
    // GET /api/notifications/student/{studentId} tests
    // ==========================================

    @Test
    @DisplayName("GET /api/notifications/student/{id} - Student can retrieve their own notifications (200 OK)")
    @WithMockUser(roles = "STUDENT")
    void getNotificationsByStudent_ownNotifications_success() throws Exception {
        when(authService.getCurrentUser()).thenReturn(studentUser);
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(studentProfile));
        when(notificationService.getNotificationsByStudent(100L)).thenReturn(Collections.singletonList(ownNotification));

        mockMvc.perform(get("/api/notifications/student/100"))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).getNotificationsByStudent(100L);
    }

    @Test
    @DisplayName("GET /api/notifications/student/{id} - Student cannot retrieve another student's notifications (403 Forbidden)")
    @WithMockUser(roles = "STUDENT")
    void getNotificationsByStudent_anotherStudentNotifications_forbidden() throws Exception {
        when(authService.getCurrentUser()).thenReturn(studentUser);
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(studentProfile));

        mockMvc.perform(get("/api/notifications/student/200"))
                .andExpect(status().isForbidden());

        verify(notificationService, never()).getNotificationsByStudent(any());
    }

    @Test
    @DisplayName("GET /api/notifications/student/{id} - Missing Student profile returns 404 Not Found")
    @WithMockUser(roles = "STUDENT")
    void getNotificationsByStudent_missingStudentProfile_notFound() throws Exception {
        when(authService.getCurrentUser()).thenReturn(studentUser);
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/notifications/student/100"))
                .andExpect(status().isNotFound());

        verify(notificationService, never()).getNotificationsByStudent(any());
    }

    @Test
    @DisplayName("GET /api/notifications/student/{id} - ADMIN cannot access (403 Forbidden)")
    @WithMockUser(roles = "ADMIN")
    void getNotificationsByStudent_admin_forbidden() throws Exception {
        mockMvc.perform(get("/api/notifications/student/100"))
                .andExpect(status().isForbidden());

        verify(notificationService, never()).getNotificationsByStudent(any());
    }

    @Test
    @DisplayName("GET /api/notifications/student/{id} - FACULTY cannot access (403 Forbidden)")
    @WithMockUser(roles = "FACULTY")
    void getNotificationsByStudent_faculty_forbidden() throws Exception {
        mockMvc.perform(get("/api/notifications/student/100"))
                .andExpect(status().isForbidden());

        verify(notificationService, never()).getNotificationsByStudent(any());
    }

    @Test
    @DisplayName("GET /api/notifications/student/{id} - Unauthenticated request is rejected (401 or 403)")
    void getNotificationsByStudent_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/notifications/student/100"))
                .andExpect(status().isForbidden());

        verify(notificationService, never()).getNotificationsByStudent(any());
    }

    // ==========================================
    // PUT /api/notifications/{id}/read tests
    // ==========================================

    @Test
    @DisplayName("PUT /api/notifications/{id}/read - Student can mark their own notification as read (200 OK)")
    @WithMockUser(roles = "STUDENT")
    void markAsRead_ownNotification_success() throws Exception {
        when(authService.getCurrentUser()).thenReturn(studentUser);
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(studentProfile));
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(ownNotification));
        when(notificationService.markAsRead(10L)).thenReturn(readNotification);

        mockMvc.perform(put("/api/notifications/10/read"))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).markAsRead(10L);
    }

    @Test
    @DisplayName("PUT /api/notifications/{id}/read - Student cannot mark another student's notification as read (403 Forbidden)")
    @WithMockUser(roles = "STUDENT")
    void markAsRead_anotherStudentNotification_forbidden() throws Exception {
        when(authService.getCurrentUser()).thenReturn(studentUser);
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(studentProfile));
        when(notificationRepository.findById(20L)).thenReturn(Optional.of(otherNotification));

        mockMvc.perform(put("/api/notifications/20/read"))
                .andExpect(status().isForbidden());

        verify(notificationService, never()).markAsRead(any());
    }

    @Test
    @DisplayName("PUT /api/notifications/{id}/read - Missing notification returns 404 Not Found")
    @WithMockUser(roles = "STUDENT")
    void markAsRead_missingNotification_notFound() throws Exception {
        when(authService.getCurrentUser()).thenReturn(studentUser);
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(studentProfile));
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/notifications/999/read"))
                .andExpect(status().isNotFound());

        verify(notificationService, never()).markAsRead(any());
    }

    @Test
    @DisplayName("PUT /api/notifications/{id}/read - Missing Student profile returns 404 Not Found")
    @WithMockUser(roles = "STUDENT")
    void markAsRead_missingStudentProfile_notFound() throws Exception {
        when(authService.getCurrentUser()).thenReturn(studentUser);
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/notifications/10/read"))
                .andExpect(status().isNotFound());

        verify(notificationService, never()).markAsRead(any());
    }

    @Test
    @DisplayName("PUT /api/notifications/{id}/read - ADMIN cannot access (403 Forbidden)")
    @WithMockUser(roles = "ADMIN")
    void markAsRead_admin_forbidden() throws Exception {
        mockMvc.perform(put("/api/notifications/10/read"))
                .andExpect(status().isForbidden());

        verify(notificationService, never()).markAsRead(any());
    }

    @Test
    @DisplayName("PUT /api/notifications/{id}/read - FACULTY cannot access (403 Forbidden)")
    @WithMockUser(roles = "FACULTY")
    void markAsRead_faculty_forbidden() throws Exception {
        mockMvc.perform(put("/api/notifications/10/read"))
                .andExpect(status().isForbidden());

        verify(notificationService, never()).markAsRead(any());
    }

    @Test
    @DisplayName("PUT /api/notifications/{id}/read - Unauthenticated request is rejected (401 or 403)")
    void markAsRead_unauthenticated_rejected() throws Exception {
        mockMvc.perform(put("/api/notifications/10/read"))
                .andExpect(status().isForbidden());

        verify(notificationService, never()).markAsRead(any());
    }
}

