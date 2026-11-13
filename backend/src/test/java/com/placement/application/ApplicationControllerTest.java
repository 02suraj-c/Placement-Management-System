package com.placement.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.application.dto.ApplicationRequest;
import com.placement.application.dto.ApplicationResponse;
import com.placement.application.dto.ApplicationStatusUpdateRequest;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
@Import({SecurityConfig.class, CorsConfig.class})
@DisplayName("ApplicationController Authorization and Security Tests")
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private AuthService authService;

    @MockBean
    private StudentRepository studentRepository;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private UserResponse studentUser;
    private Student studentProfile;
    private ApplicationResponse sampleResponse;

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

        sampleResponse = new ApplicationResponse(1L, 100L, 10L, ApplicationStatus.APPLIED, null, null);
    }

    // ==========================================
    // POST /api/applications tests
    // ==========================================

    @Test
    @DisplayName("POST /api/applications - Student applying with own Student profile ID is permitted (201 Created)")
    @WithMockUser(roles = "STUDENT")
    void apply_studentWithOwnProfileId_success() throws Exception {
        ApplicationRequest request = new ApplicationRequest(100L, 10L);

        when(authService.getCurrentUser()).thenReturn(studentUser);
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(studentProfile));
        when(applicationService.createApplication(any(ApplicationRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(applicationService, times(1)).createApplication(any(ApplicationRequest.class));
    }

    @Test
    @DisplayName("POST /api/applications - Student attempting to apply using another student ID is forbidden (403)")
    @WithMockUser(roles = "STUDENT")
    void apply_studentWithAnotherStudentId_forbidden() throws Exception {
        ApplicationRequest request = new ApplicationRequest(200L, 10L);

        when(authService.getCurrentUser()).thenReturn(studentUser);
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(studentProfile));

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(applicationService, never()).createApplication(any(ApplicationRequest.class));
    }

    @Test
    @DisplayName("POST /api/applications - Student without a Student profile is rejected with 404 Not Found")
    @WithMockUser(roles = "STUDENT")
    void apply_studentMissingProfile_notFound() throws Exception {
        ApplicationRequest request = new ApplicationRequest(100L, 10L);

        when(authService.getCurrentUser()).thenReturn(studentUser);
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(applicationService, never()).createApplication(any(ApplicationRequest.class));
    }

    @Test
    @DisplayName("POST /api/applications - Non-student roles cannot submit applications (403 Forbidden)")
    @WithMockUser(roles = "FACULTY")
    void apply_nonStudent_forbidden() throws Exception {
        ApplicationRequest request = new ApplicationRequest(100L, 10L);

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(applicationService, never()).createApplication(any(ApplicationRequest.class));
    }

    // ==========================================
    // GET /api/applications/student/{studentId} tests
    // ==========================================

    @Test
    @DisplayName("GET /api/applications/student/{id} - Student viewing own applications is permitted (200 OK)")
    @WithMockUser(roles = "STUDENT")
    void getApplicationsByStudent_studentViewingOwn_success() throws Exception {
        when(authService.getCurrentUser()).thenReturn(studentUser);
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(studentProfile));
        when(applicationService.getApplicationsByStudent(100L)).thenReturn(Collections.singletonList(sampleResponse));

        mockMvc.perform(get("/api/applications/student/100"))
                .andExpect(status().isOk());

        verify(applicationService, times(1)).getApplicationsByStudent(100L);
    }

    @Test
    @DisplayName("GET /api/applications/student/{id} - Student viewing another student's applications is forbidden (403)")
    @WithMockUser(roles = "STUDENT")
    void getApplicationsByStudent_studentViewingAnotherStudent_forbidden() throws Exception {
        when(authService.getCurrentUser()).thenReturn(studentUser);
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(studentProfile));

        mockMvc.perform(get("/api/applications/student/200"))
                .andExpect(status().isForbidden());

        verify(applicationService, never()).getApplicationsByStudent(any());
    }

    @Test
    @DisplayName("GET /api/applications/student/{id} - Student without profile is rejected with 404 Not Found")
    @WithMockUser(roles = "STUDENT")
    void getApplicationsByStudent_studentMissingProfile_notFound() throws Exception {
        when(authService.getCurrentUser()).thenReturn(studentUser);
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/applications/student/100"))
                .andExpect(status().isNotFound());

        verify(applicationService, never()).getApplicationsByStudent(any());
    }

    @Test
    @DisplayName("GET /api/applications/student/{id} - ADMIN can view any student's applications (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void getApplicationsByStudent_admin_success() throws Exception {
        UserResponse adminUser = UserResponse.builder().id(99L).role(UserRole.ROLE_ADMIN).build();
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(applicationService.getApplicationsByStudent(200L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/applications/student/200"))
                .andExpect(status().isOk());

        verify(applicationService, times(1)).getApplicationsByStudent(200L);
    }

    @Test
    @DisplayName("GET /api/applications/student/{id} - OFFICER can view any student's applications (200 OK)")
    @WithMockUser(roles = "OFFICER")
    void getApplicationsByStudent_officer_success() throws Exception {
        UserResponse officerUser = UserResponse.builder().id(98L).role(UserRole.ROLE_OFFICER).build();
        when(authService.getCurrentUser()).thenReturn(officerUser);
        when(applicationService.getApplicationsByStudent(200L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/applications/student/200"))
                .andExpect(status().isOk());

        verify(applicationService, times(1)).getApplicationsByStudent(200L);
    }

    @Test
    @DisplayName("GET /api/applications/student/{id} - PLACEMENT_OFFICER can view any student's applications (200 OK)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void getApplicationsByStudent_placementOfficer_success() throws Exception {
        UserResponse poUser = UserResponse.builder().id(97L).role(UserRole.ROLE_PLACEMENT_OFFICER).build();
        when(authService.getCurrentUser()).thenReturn(poUser);
        when(applicationService.getApplicationsByStudent(200L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/applications/student/200"))
                .andExpect(status().isOk());

        verify(applicationService, times(1)).getApplicationsByStudent(200L);
    }

    @Test
    @DisplayName("GET /api/applications/student/{id} - Unauthorized roles (FACULTY, HOD, COMPANY) are forbidden (403)")
    @WithMockUser(roles = "FACULTY")
    void getApplicationsByStudent_unauthorizedRole_forbidden() throws Exception {
        mockMvc.perform(get("/api/applications/student/100"))
                .andExpect(status().isForbidden());

        verify(applicationService, never()).getApplicationsByStudent(any());
    }

    // ==========================================
    // GET /api/applications/drive/{driveId} tests
    // ==========================================

    @Test
    @DisplayName("GET /api/applications/drive/{id} - ADMIN, OFFICER, PLACEMENT_OFFICER are permitted (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void getApplicationsByDrive_admin_permitted() throws Exception {
        when(applicationService.getApplicationsByDrive(10L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/applications/drive/10"))
                .andExpect(status().isOk());

        verify(applicationService, times(1)).getApplicationsByDrive(10L);
    }

    @Test
    @DisplayName("GET /api/applications/drive/{id} - OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "OFFICER")
    void getApplicationsByDrive_officer_permitted() throws Exception {
        when(applicationService.getApplicationsByDrive(10L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/applications/drive/10"))
                .andExpect(status().isOk());

        verify(applicationService, times(1)).getApplicationsByDrive(10L);
    }

    @Test
    @DisplayName("GET /api/applications/drive/{id} - PLACEMENT_OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void getApplicationsByDrive_placementOfficer_permitted() throws Exception {
        when(applicationService.getApplicationsByDrive(10L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/applications/drive/10"))
                .andExpect(status().isOk());

        verify(applicationService, times(1)).getApplicationsByDrive(10L);
    }

    @Test
    @DisplayName("GET /api/applications/drive/{id} - STUDENT and other roles are forbidden (403)")
    @WithMockUser(roles = "STUDENT")
    void getApplicationsByDrive_student_forbidden() throws Exception {
        mockMvc.perform(get("/api/applications/drive/10"))
                .andExpect(status().isForbidden());

        verify(applicationService, never()).getApplicationsByDrive(any());
    }

    // ==========================================
    // PUT /api/applications/{id}/status tests
    // ==========================================

    @Test
    @DisplayName("PUT /api/applications/{id}/status - ADMIN is permitted (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void updateStatus_admin_permitted() throws Exception {
        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest(ApplicationStatus.SHORTLISTED);
        when(applicationService.updateStatus(eq(1L), any(ApplicationStatusUpdateRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/applications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(applicationService, times(1)).updateStatus(eq(1L), any(ApplicationStatusUpdateRequest.class));
    }

    @Test
    @DisplayName("PUT /api/applications/{id}/status - OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "OFFICER")
    void updateStatus_officer_permitted() throws Exception {
        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest(ApplicationStatus.SHORTLISTED);
        when(applicationService.updateStatus(eq(1L), any(ApplicationStatusUpdateRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/applications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(applicationService, times(1)).updateStatus(eq(1L), any(ApplicationStatusUpdateRequest.class));
    }

    @Test
    @DisplayName("PUT /api/applications/{id}/status - PLACEMENT_OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void updateStatus_placementOfficer_permitted() throws Exception {
        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest(ApplicationStatus.SHORTLISTED);
        when(applicationService.updateStatus(eq(1L), any(ApplicationStatusUpdateRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/applications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(applicationService, times(1)).updateStatus(eq(1L), any(ApplicationStatusUpdateRequest.class));
    }

    @Test
    @DisplayName("PUT /api/applications/{id}/status - STUDENT is forbidden (403)")
    @WithMockUser(roles = "STUDENT")
    void updateStatus_student_forbidden() throws Exception {
        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest(ApplicationStatus.SHORTLISTED);

        mockMvc.perform(put("/api/applications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(applicationService, never()).updateStatus(any(), any());
    }

    @Test
    @DisplayName("PUT /api/applications/{id}/status - FACULTY is forbidden (403)")
    @WithMockUser(roles = "FACULTY")
    void updateStatus_faculty_forbidden() throws Exception {
        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest(ApplicationStatus.SHORTLISTED);

        mockMvc.perform(put("/api/applications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(applicationService, never()).updateStatus(any(), any());
    }

    // ==========================================
    // Unauthenticated requests
    // ==========================================

    @Test
    @DisplayName("Unauthenticated request to any application endpoint is rejected (401 or 403)")
    void unauthenticatedRequests_rejected() throws Exception {
        mockMvc.perform(get("/api/applications/drive/10"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/applications/student/100"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ApplicationRequest(100L, 10L))))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/applications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ApplicationStatusUpdateRequest(ApplicationStatus.SHORTLISTED))))
                .andExpect(status().isForbidden());
    }
}

