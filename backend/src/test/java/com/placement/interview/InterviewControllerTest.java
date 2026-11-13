package com.placement.interview;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.application.ApplicationRepository;
import com.placement.application.ApplicationStatus;
import com.placement.application.PlacementApplication;
import com.placement.auth.AuthService;
import com.placement.auth.dto.UserResponse;
import com.placement.config.CorsConfig;
import com.placement.config.SecurityConfig;
import com.placement.interview.dto.InterviewResultRequest;
import com.placement.interview.dto.InterviewScheduleRequest;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InterviewController.class)
@Import({SecurityConfig.class, CorsConfig.class})
@DisplayName("InterviewController Authorization and Security Tests")
class InterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InterviewService interviewService;

    @MockBean
    private AuthService authService;

    @MockBean
    private StudentRepository studentRepository;

    @MockBean
    private ApplicationRepository applicationRepository;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private UserResponse studentUser;
    private Student studentProfile;
    private PlacementApplication ownApplication;
    private PlacementApplication otherApplication;
    private InterviewRound sampleRound;
    private RoundResult sampleResult;
    private InterviewScheduleRequest scheduleRequest;
    private InterviewResultRequest resultRequest;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper.findAndRegisterModules();

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

        // Own application (studentId = 100L)
        ownApplication = new PlacementApplication(100L, 10L, ApplicationStatus.APPLIED);
        ownApplication.setId(10L);

        // Another student's application (studentId = 200L)
        otherApplication = new PlacementApplication(200L, 10L, ApplicationStatus.APPLIED);
        otherApplication.setId(20L);

        sampleRound = new InterviewRound(10L, "Technical Round 1", LocalDateTime.of(2026, 12, 1, 10, 0), "ONLINE", "Google Meet", "SCHEDULED");
        sampleRound.setId(1L);

        sampleResult = new RoundResult(1L, "PASSED", 85.0, "Great performance");
        sampleResult.setId(50L);

        scheduleRequest = new InterviewScheduleRequest(10L, "Technical Round 1", LocalDateTime.of(2026, 12, 1, 10, 0), "ONLINE", "Google Meet");
        resultRequest = new InterviewResultRequest("PASSED", 85.0, "Great performance");
    }

    // ==========================================
    // POST /api/interviews/schedule tests
    // ==========================================

    @Test
    @DisplayName("POST /api/interviews/schedule - ADMIN is permitted to schedule interview (201 Created)")
    @WithMockUser(roles = "ADMIN")
    void schedule_admin_permitted() throws Exception {
        when(interviewService.scheduleInterview(any(InterviewScheduleRequest.class))).thenReturn(sampleRound);

        mockMvc.perform(post("/api/interviews/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isCreated());

        verify(interviewService, times(1)).scheduleInterview(any(InterviewScheduleRequest.class));
    }

    @Test
    @DisplayName("POST /api/interviews/schedule - OFFICER is permitted to schedule interview (201 Created)")
    @WithMockUser(roles = "OFFICER")
    void schedule_officer_permitted() throws Exception {
        when(interviewService.scheduleInterview(any(InterviewScheduleRequest.class))).thenReturn(sampleRound);

        mockMvc.perform(post("/api/interviews/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isCreated());

        verify(interviewService, times(1)).scheduleInterview(any(InterviewScheduleRequest.class));
    }

    @Test
    @DisplayName("POST /api/interviews/schedule - PLACEMENT_OFFICER is permitted to schedule interview (201 Created)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void schedule_placementOfficer_permitted() throws Exception {
        when(interviewService.scheduleInterview(any(InterviewScheduleRequest.class))).thenReturn(sampleRound);

        mockMvc.perform(post("/api/interviews/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isCreated());

        verify(interviewService, times(1)).scheduleInterview(any(InterviewScheduleRequest.class));
    }

    @Test
    @DisplayName("POST /api/interviews/schedule - STUDENT cannot schedule interview (403 Forbidden)")
    @WithMockUser(roles = "STUDENT")
    void schedule_student_forbidden() throws Exception {
        mockMvc.perform(post("/api/interviews/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isForbidden());

        verify(interviewService, never()).scheduleInterview(any());
    }

    @Test
    @DisplayName("POST /api/interviews/schedule - FACULTY cannot schedule interview (403 Forbidden)")
    @WithMockUser(roles = "FACULTY")
    void schedule_faculty_forbidden() throws Exception {
        mockMvc.perform(post("/api/interviews/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isForbidden());

        verify(interviewService, never()).scheduleInterview(any());
    }

    // ==========================================
    // GET /api/interviews/application/{applicationId} tests
    // ==========================================

    @Test
    @DisplayName("GET /api/interviews/application/{id} - Student viewing own application's interviews is permitted (200 OK)")
    @WithMockUser(roles = "STUDENT")
    void getInterviewsByApplication_studentOwnApplication_success() throws Exception {
        when(authService.getCurrentUser()).thenReturn(studentUser);
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(studentProfile));
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(ownApplication));
        when(interviewService.getInterviewsByApplication(10L)).thenReturn(Collections.singletonList(sampleRound));

        mockMvc.perform(get("/api/interviews/application/10"))
                .andExpect(status().isOk());

        verify(interviewService, times(1)).getInterviewsByApplication(10L);
    }

    @Test
    @DisplayName("GET /api/interviews/application/{id} - Student viewing another student's application's interviews is forbidden (403)")
    @WithMockUser(roles = "STUDENT")
    void getInterviewsByApplication_studentAnotherStudentApplication_forbidden() throws Exception {
        when(authService.getCurrentUser()).thenReturn(studentUser);
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(studentProfile));
        when(applicationRepository.findById(20L)).thenReturn(Optional.of(otherApplication));

        mockMvc.perform(get("/api/interviews/application/20"))
                .andExpect(status().isForbidden());

        verify(interviewService, never()).getInterviewsByApplication(any());
    }

    @Test
    @DisplayName("GET /api/interviews/application/{id} - Student with missing Student profile returns 404 Not Found")
    @WithMockUser(roles = "STUDENT")
    void getInterviewsByApplication_studentMissingProfile_notFound() throws Exception {
        when(authService.getCurrentUser()).thenReturn(studentUser);
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/interviews/application/10"))
                .andExpect(status().isNotFound());

        verify(interviewService, never()).getInterviewsByApplication(any());
    }

    @Test
    @DisplayName("GET /api/interviews/application/{id} - Student accessing non-existent application returns 404 Not Found")
    @WithMockUser(roles = "STUDENT")
    void getInterviewsByApplication_studentApplicationNotFound_notFound() throws Exception {
        when(authService.getCurrentUser()).thenReturn(studentUser);
        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(studentProfile));
        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/interviews/application/999"))
                .andExpect(status().isNotFound());

        verify(interviewService, never()).getInterviewsByApplication(any());
    }

    @Test
    @DisplayName("GET /api/interviews/application/{id} - ADMIN can view any application's interviews (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void getInterviewsByApplication_admin_success() throws Exception {
        UserResponse adminUser = UserResponse.builder().id(99L).role(UserRole.ROLE_ADMIN).build();
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(interviewService.getInterviewsByApplication(20L)).thenReturn(Collections.singletonList(sampleRound));

        mockMvc.perform(get("/api/interviews/application/20"))
                .andExpect(status().isOk());

        verify(interviewService, times(1)).getInterviewsByApplication(20L);
    }

    @Test
    @DisplayName("GET /api/interviews/application/{id} - OFFICER can view any application's interviews (200 OK)")
    @WithMockUser(roles = "OFFICER")
    void getInterviewsByApplication_officer_success() throws Exception {
        UserResponse officerUser = UserResponse.builder().id(98L).role(UserRole.ROLE_OFFICER).build();
        when(authService.getCurrentUser()).thenReturn(officerUser);
        when(interviewService.getInterviewsByApplication(20L)).thenReturn(Collections.singletonList(sampleRound));

        mockMvc.perform(get("/api/interviews/application/20"))
                .andExpect(status().isOk());

        verify(interviewService, times(1)).getInterviewsByApplication(20L);
    }

    @Test
    @DisplayName("GET /api/interviews/application/{id} - PLACEMENT_OFFICER can view any application's interviews (200 OK)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void getInterviewsByApplication_placementOfficer_success() throws Exception {
        UserResponse poUser = UserResponse.builder().id(97L).role(UserRole.ROLE_PLACEMENT_OFFICER).build();
        when(authService.getCurrentUser()).thenReturn(poUser);
        when(interviewService.getInterviewsByApplication(20L)).thenReturn(Collections.singletonList(sampleRound));

        mockMvc.perform(get("/api/interviews/application/20"))
                .andExpect(status().isOk());

        verify(interviewService, times(1)).getInterviewsByApplication(20L);
    }

    @Test
    @DisplayName("GET /api/interviews/application/{id} - Unauthorized roles (FACULTY, HOD, COMPANY) are forbidden (403)")
    @WithMockUser(roles = "FACULTY")
    void getInterviewsByApplication_faculty_forbidden() throws Exception {
        mockMvc.perform(get("/api/interviews/application/10"))
                .andExpect(status().isForbidden());

        verify(interviewService, never()).getInterviewsByApplication(any());
    }

    @Test
    @DisplayName("GET /api/interviews/application/{id} - HOD is forbidden (403)")
    @WithMockUser(roles = "HOD")
    void getInterviewsByApplication_hod_forbidden() throws Exception {
        mockMvc.perform(get("/api/interviews/application/10"))
                .andExpect(status().isForbidden());

        verify(interviewService, never()).getInterviewsByApplication(any());
    }

    @Test
    @DisplayName("GET /api/interviews/application/{id} - COMPANY is forbidden (403)")
    @WithMockUser(roles = "COMPANY")
    void getInterviewsByApplication_company_forbidden() throws Exception {
        mockMvc.perform(get("/api/interviews/application/10"))
                .andExpect(status().isForbidden());

        verify(interviewService, never()).getInterviewsByApplication(any());
    }

    // ==========================================
    // POST /api/interviews/{roundId}/result tests
    // ==========================================

    @Test
    @DisplayName("POST /api/interviews/{roundId}/result - ADMIN is permitted to record result (201 Created)")
    @WithMockUser(roles = "ADMIN")
    void recordResult_admin_permitted() throws Exception {
        when(interviewService.recordResult(eq(1L), any(InterviewResultRequest.class))).thenReturn(sampleResult);

        mockMvc.perform(post("/api/interviews/1/result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resultRequest)))
                .andExpect(status().isCreated());

        verify(interviewService, times(1)).recordResult(eq(1L), any(InterviewResultRequest.class));
    }

    @Test
    @DisplayName("POST /api/interviews/{roundId}/result - OFFICER is permitted to record result (201 Created)")
    @WithMockUser(roles = "OFFICER")
    void recordResult_officer_permitted() throws Exception {
        when(interviewService.recordResult(eq(1L), any(InterviewResultRequest.class))).thenReturn(sampleResult);

        mockMvc.perform(post("/api/interviews/1/result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resultRequest)))
                .andExpect(status().isCreated());

        verify(interviewService, times(1)).recordResult(eq(1L), any(InterviewResultRequest.class));
    }

    @Test
    @DisplayName("POST /api/interviews/{roundId}/result - PLACEMENT_OFFICER is permitted to record result (201 Created)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void recordResult_placementOfficer_permitted() throws Exception {
        when(interviewService.recordResult(eq(1L), any(InterviewResultRequest.class))).thenReturn(sampleResult);

        mockMvc.perform(post("/api/interviews/1/result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resultRequest)))
                .andExpect(status().isCreated());

        verify(interviewService, times(1)).recordResult(eq(1L), any(InterviewResultRequest.class));
    }

    @Test
    @DisplayName("POST /api/interviews/{roundId}/result - STUDENT cannot record result (403 Forbidden)")
    @WithMockUser(roles = "STUDENT")
    void recordResult_student_forbidden() throws Exception {
        mockMvc.perform(post("/api/interviews/1/result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resultRequest)))
                .andExpect(status().isForbidden());

        verify(interviewService, never()).recordResult(any(), any());
    }

    @Test
    @DisplayName("POST /api/interviews/{roundId}/result - FACULTY cannot record result (403 Forbidden)")
    @WithMockUser(roles = "FACULTY")
    void recordResult_faculty_forbidden() throws Exception {
        mockMvc.perform(post("/api/interviews/1/result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resultRequest)))
                .andExpect(status().isForbidden());

        verify(interviewService, never()).recordResult(any(), any());
    }

    // ==========================================
    // Unauthenticated requests
    // ==========================================

    @Test
    @DisplayName("Unauthenticated request to any interview endpoint is rejected (401 or 403)")
    void unauthenticatedRequests_rejected() throws Exception {
        mockMvc.perform(post("/api/interviews/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/interviews/application/10"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/interviews/1/result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resultRequest)))
                .andExpect(status().isForbidden());
    }
}

