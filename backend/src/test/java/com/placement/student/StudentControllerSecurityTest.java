package com.placement.student;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.config.CorsConfig;
import com.placement.config.SecurityConfig;
import com.placement.security.CustomUserDetailsService;
import com.placement.security.JwtFilter;
import com.placement.security.JwtService;
import com.placement.student.dto.StudentProfileRequest;
import com.placement.student.dto.StudentProfileResponse;
import com.placement.student.dto.StudentUpdateRequest;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
@Import({SecurityConfig.class, CorsConfig.class})
@DisplayName("StudentController Security and Authorization Tests")
class StudentControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private StudentProfileRequest validProfileRequest;
    private StudentUpdateRequest validUpdateRequest;
    private StudentProfileResponse sampleResponse;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());

        validProfileRequest = StudentProfileRequest.builder()
                .rollNumber("CS2026-042")
                .enrollmentNumber("EN2022001")
                .department("Computer Science")
                .course("B.Tech")
                .currentSemester(6)
                .cgpa(8.85)
                .graduationYear(2026)
                .phoneNumber("9876543210")
                .activeBacklogs(0)
                .skills("Java, Spring Boot")
                .build();

        validUpdateRequest = StudentUpdateRequest.builder()
                .department("Computer Science")
                .course("B.Tech")
                .currentSemester(7)
                .cgpa(9.1)
                .graduationYear(2026)
                .phoneNumber("9876543210")
                .activeBacklogs(0)
                .skills("Java, Spring Boot, Microservices")
                .build();

        sampleResponse = StudentProfileResponse.builder()
                .id(100L)
                .userId(1L)
                .name("Student User")
                .email("student@college.edu")
                .role(UserRole.ROLE_STUDENT)
                .rollNumber("CS2026-042")
                .enrollmentNumber("EN2022001")
                .department("Computer Science")
                .course("B.Tech")
                .currentSemester(6)
                .cgpa(8.85)
                .graduationYear(2026)
                .phoneNumber("9876543210")
                .activeBacklogs(0)
                .skills("Java, Spring Boot")
                .build();
    }

    // ==========================================
    // 1. POST /api/students -> hasRole('STUDENT')
    // ==========================================

    @Test
    @DisplayName("POST /api/students - ROLE_STUDENT is permitted (201 Created)")
    @WithMockUser(username = "student@college.edu", roles = "STUDENT")
    void createProfile_student_permitted() throws Exception {
        when(studentService.createProfile(any(StudentProfileRequest.class), eq("student@college.edu")))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validProfileRequest)))
                .andExpect(status().isCreated());

        verify(studentService, times(1)).createProfile(any(StudentProfileRequest.class), eq("student@college.edu"));
    }

    @Test
    @DisplayName("POST /api/students - ROLE_ADMIN is forbidden (403 Forbidden)")
    @WithMockUser(username = "admin@college.edu", roles = "ADMIN")
    void createProfile_admin_forbidden() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validProfileRequest)))
                .andExpect(status().isForbidden());

        verify(studentService, never()).createProfile(any(), any());
    }

    @Test
    @DisplayName("POST /api/students - ROLE_FACULTY is forbidden (403 Forbidden)")
    @WithMockUser(username = "faculty@college.edu", roles = "FACULTY")
    void createProfile_faculty_forbidden() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validProfileRequest)))
                .andExpect(status().isForbidden());

        verify(studentService, never()).createProfile(any(), any());
    }

    @Test
    @DisplayName("POST /api/students - ROLE_COMPANY is forbidden (403 Forbidden)")
    @WithMockUser(username = "company@recruiter.com", roles = "COMPANY")
    void createProfile_company_forbidden() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validProfileRequest)))
                .andExpect(status().isForbidden());

        verify(studentService, never()).createProfile(any(), any());
    }

    @Test
    @DisplayName("POST /api/students - Unauthenticated request is rejected (401 or 403)")
    void createProfile_unauthenticated_rejected() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validProfileRequest)))
                .andExpect(status().isForbidden());

        verify(studentService, never()).createProfile(any(), any());
    }

    // ==========================================
    // 2. GET /api/students/me -> hasRole('STUDENT')
    // ==========================================

    @Test
    @DisplayName("GET /api/students/me - ROLE_STUDENT is permitted (200 OK)")
    @WithMockUser(username = "student@college.edu", roles = "STUDENT")
    void getCurrentProfile_student_permitted() throws Exception {
        when(studentService.getCurrentStudentProfile(eq("student@college.edu"))).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/students/me"))
                .andExpect(status().isOk());

        verify(studentService, times(1)).getCurrentStudentProfile(eq("student@college.edu"));
    }

    @Test
    @DisplayName("GET /api/students/me - ROLE_ADMIN is forbidden (403 Forbidden)")
    @WithMockUser(username = "admin@college.edu", roles = "ADMIN")
    void getCurrentProfile_admin_forbidden() throws Exception {
        mockMvc.perform(get("/api/students/me"))
                .andExpect(status().isForbidden());

        verify(studentService, never()).getCurrentStudentProfile(any());
    }

    @Test
    @DisplayName("GET /api/students/me - ROLE_FACULTY is forbidden (403 Forbidden)")
    @WithMockUser(username = "faculty@college.edu", roles = "FACULTY")
    void getCurrentProfile_faculty_forbidden() throws Exception {
        mockMvc.perform(get("/api/students/me"))
                .andExpect(status().isForbidden());

        verify(studentService, never()).getCurrentStudentProfile(any());
    }

    @Test
    @DisplayName("GET /api/students/me - ROLE_COMPANY is forbidden (403 Forbidden)")
    @WithMockUser(username = "company@recruiter.com", roles = "COMPANY")
    void getCurrentProfile_company_forbidden() throws Exception {
        mockMvc.perform(get("/api/students/me"))
                .andExpect(status().isForbidden());

        verify(studentService, never()).getCurrentStudentProfile(any());
    }

    @Test
    @DisplayName("GET /api/students/me - Unauthenticated request is rejected (401 or 403)")
    void getCurrentProfile_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/students/me"))
                .andExpect(status().isForbidden());

        verify(studentService, never()).getCurrentStudentProfile(any());
    }

    // ==========================================
    // 3. PUT /api/students/me -> hasRole('STUDENT')
    // ==========================================

    @Test
    @DisplayName("PUT /api/students/me - ROLE_STUDENT is permitted (200 OK)")
    @WithMockUser(username = "student@college.edu", roles = "STUDENT")
    void updateCurrentProfile_student_permitted() throws Exception {
        when(studentService.updateCurrentStudentProfile(any(StudentUpdateRequest.class), eq("student@college.edu")))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/students/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isOk());

        verify(studentService, times(1)).updateCurrentStudentProfile(any(StudentUpdateRequest.class), eq("student@college.edu"));
    }

    @Test
    @DisplayName("PUT /api/students/me - ROLE_ADMIN is forbidden (403 Forbidden)")
    @WithMockUser(username = "admin@college.edu", roles = "ADMIN")
    void updateCurrentProfile_admin_forbidden() throws Exception {
        mockMvc.perform(put("/api/students/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isForbidden());

        verify(studentService, never()).updateCurrentStudentProfile(any(), any());
    }

    @Test
    @DisplayName("PUT /api/students/me - ROLE_FACULTY is forbidden (403 Forbidden)")
    @WithMockUser(username = "faculty@college.edu", roles = "FACULTY")
    void updateCurrentProfile_faculty_forbidden() throws Exception {
        mockMvc.perform(put("/api/students/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isForbidden());

        verify(studentService, never()).updateCurrentStudentProfile(any(), any());
    }

    @Test
    @DisplayName("PUT /api/students/me - ROLE_COMPANY is forbidden (403 Forbidden)")
    @WithMockUser(username = "company@recruiter.com", roles = "COMPANY")
    void updateCurrentProfile_company_forbidden() throws Exception {
        mockMvc.perform(put("/api/students/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isForbidden());

        verify(studentService, never()).updateCurrentStudentProfile(any(), any());
    }

    @Test
    @DisplayName("PUT /api/students/me - Unauthenticated request is rejected (401 or 403)")
    void updateCurrentProfile_unauthenticated_rejected() throws Exception {
        mockMvc.perform(put("/api/students/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isForbidden());

        verify(studentService, never()).updateCurrentStudentProfile(any(), any());
    }

    // ==========================================
    // 4. GET /api/students/{id} -> hasAnyRole('STUDENT', 'FACULTY', 'HOD', 'OFFICER', 'PLACEMENT_OFFICER', 'ADMIN')
    // ==========================================

    @Test
    @DisplayName("GET /api/students/{id} - ROLE_STUDENT is permitted (200 OK)")
    @WithMockUser(username = "student@college.edu", roles = "STUDENT")
    void getProfileById_student_permitted() throws Exception {
        when(studentService.getProfileById(eq(100L), eq("student@college.edu"))).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/students/100"))
                .andExpect(status().isOk());

        verify(studentService, times(1)).getProfileById(eq(100L), eq("student@college.edu"));
    }

    @Test
    @DisplayName("GET /api/students/{id} - ROLE_FACULTY is permitted (200 OK)")
    @WithMockUser(username = "faculty@college.edu", roles = "FACULTY")
    void getProfileById_faculty_permitted() throws Exception {
        when(studentService.getProfileById(eq(100L), eq("faculty@college.edu"))).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/students/100"))
                .andExpect(status().isOk());

        verify(studentService, times(1)).getProfileById(eq(100L), eq("faculty@college.edu"));
    }

    @Test
    @DisplayName("GET /api/students/{id} - ROLE_HOD is permitted (200 OK)")
    @WithMockUser(username = "hod@college.edu", roles = "HOD")
    void getProfileById_hod_permitted() throws Exception {
        when(studentService.getProfileById(eq(100L), eq("hod@college.edu"))).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/students/100"))
                .andExpect(status().isOk());

        verify(studentService, times(1)).getProfileById(eq(100L), eq("hod@college.edu"));
    }

    @Test
    @DisplayName("GET /api/students/{id} - ROLE_OFFICER is permitted (200 OK)")
    @WithMockUser(username = "officer@college.edu", roles = "OFFICER")
    void getProfileById_officer_permitted() throws Exception {
        when(studentService.getProfileById(eq(100L), eq("officer@college.edu"))).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/students/100"))
                .andExpect(status().isOk());

        verify(studentService, times(1)).getProfileById(eq(100L), eq("officer@college.edu"));
    }

    @Test
    @DisplayName("GET /api/students/{id} - ROLE_PLACEMENT_OFFICER is permitted (200 OK)")
    @WithMockUser(username = "po@college.edu", roles = "PLACEMENT_OFFICER")
    void getProfileById_placementOfficer_permitted() throws Exception {
        when(studentService.getProfileById(eq(100L), eq("po@college.edu"))).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/students/100"))
                .andExpect(status().isOk());

        verify(studentService, times(1)).getProfileById(eq(100L), eq("po@college.edu"));
    }

    @Test
    @DisplayName("GET /api/students/{id} - ROLE_ADMIN is permitted (200 OK)")
    @WithMockUser(username = "admin@college.edu", roles = "ADMIN")
    void getProfileById_admin_permitted() throws Exception {
        when(studentService.getProfileById(eq(100L), eq("admin@college.edu"))).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/students/100"))
                .andExpect(status().isOk());

        verify(studentService, times(1)).getProfileById(eq(100L), eq("admin@college.edu"));
    }

    @Test
    @DisplayName("GET /api/students/{id} - ROLE_COMPANY is forbidden (403 Forbidden)")
    @WithMockUser(username = "company@recruiter.com", roles = "COMPANY")
    void getProfileById_company_forbidden() throws Exception {
        mockMvc.perform(get("/api/students/100"))
                .andExpect(status().isForbidden());

        verify(studentService, never()).getProfileById(any(), any());
    }

    @Test
    @DisplayName("GET /api/students/{id} - Unauthenticated request is rejected (401 or 403)")
    void getProfileById_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/students/100"))
                .andExpect(status().isForbidden());

        verify(studentService, never()).getProfileById(any(), any());
    }

    // ==========================================
    // 5. GET /api/students -> hasAnyRole('ADMIN', 'OFFICER', 'PLACEMENT_OFFICER', 'FACULTY', 'HOD')
    // ==========================================

    @Test
    @DisplayName("GET /api/students - ROLE_ADMIN is permitted (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void getAllStudents_admin_permitted() throws Exception {
        when(studentService.getAllStudents()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk());

        verify(studentService, times(1)).getAllStudents();
    }

    @Test
    @DisplayName("GET /api/students - ROLE_OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "OFFICER")
    void getAllStudents_officer_permitted() throws Exception {
        when(studentService.getAllStudents()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk());

        verify(studentService, times(1)).getAllStudents();
    }

    @Test
    @DisplayName("GET /api/students - ROLE_PLACEMENT_OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void getAllStudents_placementOfficer_permitted() throws Exception {
        when(studentService.getAllStudents()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk());

        verify(studentService, times(1)).getAllStudents();
    }

    @Test
    @DisplayName("GET /api/students - ROLE_FACULTY is permitted (200 OK)")
    @WithMockUser(roles = "FACULTY")
    void getAllStudents_faculty_permitted() throws Exception {
        when(studentService.getAllStudents()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk());

        verify(studentService, times(1)).getAllStudents();
    }

    @Test
    @DisplayName("GET /api/students - ROLE_HOD is permitted (200 OK)")
    @WithMockUser(roles = "HOD")
    void getAllStudents_hod_permitted() throws Exception {
        when(studentService.getAllStudents()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk());

        verify(studentService, times(1)).getAllStudents();
    }

    @Test
    @DisplayName("GET /api/students - ROLE_STUDENT is forbidden (403 Forbidden)")
    @WithMockUser(roles = "STUDENT")
    void getAllStudents_student_forbidden() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isForbidden());

        verify(studentService, never()).getAllStudents();
    }

    @Test
    @DisplayName("GET /api/students - ROLE_COMPANY is forbidden (403 Forbidden)")
    @WithMockUser(roles = "COMPANY")
    void getAllStudents_company_forbidden() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isForbidden());

        verify(studentService, never()).getAllStudents();
    }

    @Test
    @DisplayName("GET /api/students - Unauthenticated request is rejected (401 or 403)")
    void getAllStudents_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isForbidden());

        verify(studentService, never()).getAllStudents();
    }
}

