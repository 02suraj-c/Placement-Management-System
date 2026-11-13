package com.placement.resume;

import com.placement.config.CorsConfig;
import com.placement.config.SecurityConfig;
import com.placement.resume.dto.ResumeResponse;
import com.placement.security.CustomUserDetailsService;
import com.placement.security.JwtFilter;
import com.placement.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResumeController.class)
@Import({SecurityConfig.class, CorsConfig.class})
@DisplayName("ResumeController Security and Authorization Tests")
class ResumeControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResumeService resumeService;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private ResumeResponse sampleResponse;
    private ResumeService.ResumeDownloadInfo sampleDownloadInfo;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());

        mockFile = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "%PDF-1.4 Mock Content".getBytes()
        );

        sampleResponse = ResumeResponse.builder()
                .id(10L)
                .studentId(100L)
                .studentName("Student User")
                .rollNumber("CS2026-042")
                .originalFileName("resume.pdf")
                .contentType("application/pdf")
                .fileSize(1024L)
                .formattedSize("1.0 KB")
                .active(true)
                .uploadedAt(LocalDateTime.now())
                .downloadUrl("/api/resumes/download/10")
                .build();

        sampleDownloadInfo = new ResumeService.ResumeDownloadInfo(
                new ByteArrayResource("%PDF-1.4 Mock Content".getBytes()),
                "resume.pdf",
                "application/pdf"
        );
    }

    // ==========================================
    // 1. POST /api/resumes/upload -> hasRole('STUDENT')
    // ==========================================

    @Test
    @DisplayName("POST /api/resumes/upload - ROLE_STUDENT is permitted (201 Created)")
    @WithMockUser(username = "student@college.edu", roles = "STUDENT")
    void uploadResume_student_permitted() throws Exception {
        when(resumeService.uploadResume(any(MultipartFile.class), eq("student@college.edu")))
                .thenReturn(sampleResponse);

        mockMvc.perform(multipart("/api/resumes/upload").file(mockFile))
                .andExpect(status().isCreated());

        verify(resumeService, times(1)).uploadResume(any(MultipartFile.class), eq("student@college.edu"));
    }

    @Test
    @DisplayName("POST /api/resumes/upload - ROLE_ADMIN is forbidden (403 Forbidden)")
    @WithMockUser(username = "admin@college.edu", roles = "ADMIN")
    void uploadResume_admin_forbidden() throws Exception {
        mockMvc.perform(multipart("/api/resumes/upload").file(mockFile))
                .andExpect(status().isForbidden());

        verify(resumeService, never()).uploadResume(any(), any());
    }

    @Test
    @DisplayName("POST /api/resumes/upload - ROLE_FACULTY is forbidden (403 Forbidden)")
    @WithMockUser(username = "faculty@college.edu", roles = "FACULTY")
    void uploadResume_faculty_forbidden() throws Exception {
        mockMvc.perform(multipart("/api/resumes/upload").file(mockFile))
                .andExpect(status().isForbidden());

        verify(resumeService, never()).uploadResume(any(), any());
    }

    @Test
    @DisplayName("POST /api/resumes/upload - ROLE_COMPANY is forbidden (403 Forbidden)")
    @WithMockUser(username = "company@recruiter.com", roles = "COMPANY")
    void uploadResume_company_forbidden() throws Exception {
        mockMvc.perform(multipart("/api/resumes/upload").file(mockFile))
                .andExpect(status().isForbidden());

        verify(resumeService, never()).uploadResume(any(), any());
    }

    @Test
    @DisplayName("POST /api/resumes/upload - Unauthenticated request is rejected (401 or 403)")
    void uploadResume_unauthenticated_rejected() throws Exception {
        mockMvc.perform(multipart("/api/resumes/upload").file(mockFile))
                .andExpect(status().isForbidden());

        verify(resumeService, never()).uploadResume(any(), any());
    }

    // ==========================================
    // 2. GET /api/resumes/me -> hasRole('STUDENT')
    // ==========================================

    @Test
    @DisplayName("GET /api/resumes/me - ROLE_STUDENT is permitted (200 OK)")
    @WithMockUser(username = "student@college.edu", roles = "STUDENT")
    void getCurrentResume_student_permitted() throws Exception {
        when(resumeService.getCurrentStudentResume(eq("student@college.edu"))).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/resumes/me"))
                .andExpect(status().isOk());

        verify(resumeService, times(1)).getCurrentStudentResume(eq("student@college.edu"));
    }

    @Test
    @DisplayName("GET /api/resumes/me - ROLE_ADMIN is forbidden (403 Forbidden)")
    @WithMockUser(username = "admin@college.edu", roles = "ADMIN")
    void getCurrentResume_admin_forbidden() throws Exception {
        mockMvc.perform(get("/api/resumes/me"))
                .andExpect(status().isForbidden());

        verify(resumeService, never()).getCurrentStudentResume(any());
    }

    @Test
    @DisplayName("GET /api/resumes/me - ROLE_FACULTY is forbidden (403 Forbidden)")
    @WithMockUser(username = "faculty@college.edu", roles = "FACULTY")
    void getCurrentResume_faculty_forbidden() throws Exception {
        mockMvc.perform(get("/api/resumes/me"))
                .andExpect(status().isForbidden());

        verify(resumeService, never()).getCurrentStudentResume(any());
    }

    @Test
    @DisplayName("GET /api/resumes/me - ROLE_COMPANY is forbidden (403 Forbidden)")
    @WithMockUser(username = "company@recruiter.com", roles = "COMPANY")
    void getCurrentResume_company_forbidden() throws Exception {
        mockMvc.perform(get("/api/resumes/me"))
                .andExpect(status().isForbidden());

        verify(resumeService, never()).getCurrentStudentResume(any());
    }

    @Test
    @DisplayName("GET /api/resumes/me - Unauthenticated request is rejected (401 or 403)")
    void getCurrentResume_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/resumes/me"))
                .andExpect(status().isForbidden());

        verify(resumeService, never()).getCurrentStudentResume(any());
    }

    // ==========================================
    // 3. GET /api/resumes/me/download -> hasRole('STUDENT')
    // ==========================================

    @Test
    @DisplayName("GET /api/resumes/me/download - ROLE_STUDENT is permitted (200 OK)")
    @WithMockUser(username = "student@college.edu", roles = "STUDENT")
    void downloadCurrentResume_student_permitted() throws Exception {
        when(resumeService.getCurrentStudentResumeFile(eq("student@college.edu"))).thenReturn(sampleDownloadInfo);

        mockMvc.perform(get("/api/resumes/me/download"))
                .andExpect(status().isOk());

        verify(resumeService, times(1)).getCurrentStudentResumeFile(eq("student@college.edu"));
    }

    @Test
    @DisplayName("GET /api/resumes/me/download - ROLE_ADMIN is forbidden (403 Forbidden)")
    @WithMockUser(username = "admin@college.edu", roles = "ADMIN")
    void downloadCurrentResume_admin_forbidden() throws Exception {
        mockMvc.perform(get("/api/resumes/me/download"))
                .andExpect(status().isForbidden());

        verify(resumeService, never()).getCurrentStudentResumeFile(any());
    }

    @Test
    @DisplayName("GET /api/resumes/me/download - ROLE_FACULTY is forbidden (403 Forbidden)")
    @WithMockUser(username = "faculty@college.edu", roles = "FACULTY")
    void downloadCurrentResume_faculty_forbidden() throws Exception {
        mockMvc.perform(get("/api/resumes/me/download"))
                .andExpect(status().isForbidden());

        verify(resumeService, never()).getCurrentStudentResumeFile(any());
    }

    @Test
    @DisplayName("GET /api/resumes/me/download - ROLE_COMPANY is forbidden (403 Forbidden)")
    @WithMockUser(username = "company@recruiter.com", roles = "COMPANY")
    void downloadCurrentResume_company_forbidden() throws Exception {
        mockMvc.perform(get("/api/resumes/me/download"))
                .andExpect(status().isForbidden());

        verify(resumeService, never()).getCurrentStudentResumeFile(any());
    }

    @Test
    @DisplayName("GET /api/resumes/me/download - Unauthenticated request is rejected (401 or 403)")
    void downloadCurrentResume_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/resumes/me/download"))
                .andExpect(status().isForbidden());

        verify(resumeService, never()).getCurrentStudentResumeFile(any());
    }

    // ==========================================
    // 4. GET /api/resumes/student/{studentId} -> hasAnyRole('STUDENT', 'FACULTY', 'HOD', 'OFFICER', 'PLACEMENT_OFFICER', 'ADMIN')
    // ==========================================

    @Test
    @DisplayName("GET /api/resumes/student/{id} - ROLE_STUDENT is permitted (200 OK)")
    @WithMockUser(username = "student@college.edu", roles = "STUDENT")
    void getResumeByStudentId_student_permitted() throws Exception {
        when(resumeService.getResumeByStudentId(eq(100L), eq("student@college.edu"))).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/resumes/student/100"))
                .andExpect(status().isOk());

        verify(resumeService, times(1)).getResumeByStudentId(eq(100L), eq("student@college.edu"));
    }

    @Test
    @DisplayName("GET /api/resumes/student/{id} - ROLE_FACULTY is permitted (200 OK)")
    @WithMockUser(username = "faculty@college.edu", roles = "FACULTY")
    void getResumeByStudentId_faculty_permitted() throws Exception {
        when(resumeService.getResumeByStudentId(eq(100L), eq("faculty@college.edu"))).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/resumes/student/100"))
                .andExpect(status().isOk());

        verify(resumeService, times(1)).getResumeByStudentId(eq(100L), eq("faculty@college.edu"));
    }

    @Test
    @DisplayName("GET /api/resumes/student/{id} - ROLE_HOD is permitted (200 OK)")
    @WithMockUser(username = "hod@college.edu", roles = "HOD")
    void getResumeByStudentId_hod_permitted() throws Exception {
        when(resumeService.getResumeByStudentId(eq(100L), eq("hod@college.edu"))).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/resumes/student/100"))
                .andExpect(status().isOk());

        verify(resumeService, times(1)).getResumeByStudentId(eq(100L), eq("hod@college.edu"));
    }

    @Test
    @DisplayName("GET /api/resumes/student/{id} - ROLE_OFFICER is permitted (200 OK)")
    @WithMockUser(username = "officer@college.edu", roles = "OFFICER")
    void getResumeByStudentId_officer_permitted() throws Exception {
        when(resumeService.getResumeByStudentId(eq(100L), eq("officer@college.edu"))).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/resumes/student/100"))
                .andExpect(status().isOk());

        verify(resumeService, times(1)).getResumeByStudentId(eq(100L), eq("officer@college.edu"));
    }

    @Test
    @DisplayName("GET /api/resumes/student/{id} - ROLE_PLACEMENT_OFFICER is permitted (200 OK)")
    @WithMockUser(username = "po@college.edu", roles = "PLACEMENT_OFFICER")
    void getResumeByStudentId_placementOfficer_permitted() throws Exception {
        when(resumeService.getResumeByStudentId(eq(100L), eq("po@college.edu"))).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/resumes/student/100"))
                .andExpect(status().isOk());

        verify(resumeService, times(1)).getResumeByStudentId(eq(100L), eq("po@college.edu"));
    }

    @Test
    @DisplayName("GET /api/resumes/student/{id} - ROLE_ADMIN is permitted (200 OK)")
    @WithMockUser(username = "admin@college.edu", roles = "ADMIN")
    void getResumeByStudentId_admin_permitted() throws Exception {
        when(resumeService.getResumeByStudentId(eq(100L), eq("admin@college.edu"))).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/resumes/student/100"))
                .andExpect(status().isOk());

        verify(resumeService, times(1)).getResumeByStudentId(eq(100L), eq("admin@college.edu"));
    }

    @Test
    @DisplayName("GET /api/resumes/student/{id} - ROLE_COMPANY is forbidden (403 Forbidden)")
    @WithMockUser(username = "company@recruiter.com", roles = "COMPANY")
    void getResumeByStudentId_company_forbidden() throws Exception {
        mockMvc.perform(get("/api/resumes/student/100"))
                .andExpect(status().isForbidden());

        verify(resumeService, never()).getResumeByStudentId(any(), any());
    }

    @Test
    @DisplayName("GET /api/resumes/student/{id} - Unauthenticated request is rejected (401 or 403)")
    void getResumeByStudentId_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/resumes/student/100"))
                .andExpect(status().isForbidden());

        verify(resumeService, never()).getResumeByStudentId(any(), any());
    }

    // ==========================================
    // 5. GET /api/resumes/download/{id} -> hasAnyRole('STUDENT', 'FACULTY', 'HOD', 'OFFICER', 'PLACEMENT_OFFICER', 'ADMIN')
    // ==========================================

    @Test
    @DisplayName("GET /api/resumes/download/{id} - ROLE_STUDENT is permitted (200 OK)")
    @WithMockUser(username = "student@college.edu", roles = "STUDENT")
    void downloadResumeById_student_permitted() throws Exception {
        when(resumeService.getResumeFile(eq(10L), eq("student@college.edu"))).thenReturn(sampleDownloadInfo);

        mockMvc.perform(get("/api/resumes/download/10"))
                .andExpect(status().isOk());

        verify(resumeService, times(1)).getResumeFile(eq(10L), eq("student@college.edu"));
    }

    @Test
    @DisplayName("GET /api/resumes/download/{id} - ROLE_FACULTY is permitted (200 OK)")
    @WithMockUser(username = "faculty@college.edu", roles = "FACULTY")
    void downloadResumeById_faculty_permitted() throws Exception {
        when(resumeService.getResumeFile(eq(10L), eq("faculty@college.edu"))).thenReturn(sampleDownloadInfo);

        mockMvc.perform(get("/api/resumes/download/10"))
                .andExpect(status().isOk());

        verify(resumeService, times(1)).getResumeFile(eq(10L), eq("faculty@college.edu"));
    }

    @Test
    @DisplayName("GET /api/resumes/download/{id} - ROLE_HOD is permitted (200 OK)")
    @WithMockUser(username = "hod@college.edu", roles = "HOD")
    void downloadResumeById_hod_permitted() throws Exception {
        when(resumeService.getResumeFile(eq(10L), eq("hod@college.edu"))).thenReturn(sampleDownloadInfo);

        mockMvc.perform(get("/api/resumes/download/10"))
                .andExpect(status().isOk());

        verify(resumeService, times(1)).getResumeFile(eq(10L), eq("hod@college.edu"));
    }

    @Test
    @DisplayName("GET /api/resumes/download/{id} - ROLE_OFFICER is permitted (200 OK)")
    @WithMockUser(username = "officer@college.edu", roles = "OFFICER")
    void downloadResumeById_officer_permitted() throws Exception {
        when(resumeService.getResumeFile(eq(10L), eq("officer@college.edu"))).thenReturn(sampleDownloadInfo);

        mockMvc.perform(get("/api/resumes/download/10"))
                .andExpect(status().isOk());

        verify(resumeService, times(1)).getResumeFile(eq(10L), eq("officer@college.edu"));
    }

    @Test
    @DisplayName("GET /api/resumes/download/{id} - ROLE_PLACEMENT_OFFICER is permitted (200 OK)")
    @WithMockUser(username = "po@college.edu", roles = "PLACEMENT_OFFICER")
    void downloadResumeById_placementOfficer_permitted() throws Exception {
        when(resumeService.getResumeFile(eq(10L), eq("po@college.edu"))).thenReturn(sampleDownloadInfo);

        mockMvc.perform(get("/api/resumes/download/10"))
                .andExpect(status().isOk());

        verify(resumeService, times(1)).getResumeFile(eq(10L), eq("po@college.edu"));
    }

    @Test
    @DisplayName("GET /api/resumes/download/{id} - ROLE_ADMIN is permitted (200 OK)")
    @WithMockUser(username = "admin@college.edu", roles = "ADMIN")
    void downloadResumeById_admin_permitted() throws Exception {
        when(resumeService.getResumeFile(eq(10L), eq("admin@college.edu"))).thenReturn(sampleDownloadInfo);

        mockMvc.perform(get("/api/resumes/download/10"))
                .andExpect(status().isOk());

        verify(resumeService, times(1)).getResumeFile(eq(10L), eq("admin@college.edu"));
    }

    @Test
    @DisplayName("GET /api/resumes/download/{id} - ROLE_COMPANY is forbidden (403 Forbidden)")
    @WithMockUser(username = "company@recruiter.com", roles = "COMPANY")
    void downloadResumeById_company_forbidden() throws Exception {
        mockMvc.perform(get("/api/resumes/download/10"))
                .andExpect(status().isForbidden());

        verify(resumeService, never()).getResumeFile(any(), any());
    }

    @Test
    @DisplayName("GET /api/resumes/download/{id} - Unauthenticated request is rejected (401 or 403)")
    void downloadResumeById_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/resumes/download/10"))
                .andExpect(status().isForbidden());

        verify(resumeService, never()).getResumeFile(any(), any());
    }
}

