package com.placement.placement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.company.Company;
import com.placement.config.CorsConfig;
import com.placement.config.SecurityConfig;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlacementController.class)
@Import({SecurityConfig.class, CorsConfig.class})
@DisplayName("PlacementController Security and Authorization Tests")
class PlacementControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlacementService placementService;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private PlacementDrive sampleDrive;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper.findAndRegisterModules();

        doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());

        Company sampleCompany = new Company(
                1L,
                "Acme Corp",
                "Leading Tech Solutions",
                "IT",
                "https://acme.example.com",
                "Bangalore",
                "careers@acme.example.com",
                "+91 9876543210",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        sampleDrive = new PlacementDrive();
        sampleDrive.setId(10L);
        sampleDrive.setCompany(sampleCompany);
        sampleDrive.setJobTitle("Software Engineer");
        sampleDrive.setJobDescription("Full-stack engineering role");
        sampleDrive.setJobLocation("Bangalore");
        sampleDrive.setSalaryPackage(12.0);
        sampleDrive.setEmploymentType("FULL_TIME");
        sampleDrive.setApplicationStartDate(LocalDate.of(2026, 11, 1));
        sampleDrive.setApplicationEndDate(LocalDate.of(2026, 11, 15));
        sampleDrive.setDriveDate(LocalDate.of(2026, 11, 20));
        sampleDrive.setVacancies(8);
        sampleDrive.setStatus(DriveStatus.UPCOMING);
        sampleDrive.setMinimumCgpa(7.0);
        sampleDrive.setMaximumAllowedBacklogs(0);
    }

    // =========================================================================
    // 1. POST /api/placement-drives
    // =========================================================================

    @Test
    @DisplayName("POST /api/placement-drives - OFFICER is permitted (201 Created)")
    @WithMockUser(roles = "OFFICER")
    void createPlacementDrive_officer_permitted() throws Exception {
        when(placementService.createPlacementDrive(any(PlacementDrive.class))).thenReturn(sampleDrive);

        mockMvc.perform(post("/api/placement-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDrive)))
                .andExpect(status().isCreated());

        verify(placementService, times(1)).createPlacementDrive(any(PlacementDrive.class));
    }

    @Test
    @DisplayName("POST /api/placement-drives - PLACEMENT_OFFICER is permitted (201 Created)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void createPlacementDrive_placementOfficer_permitted() throws Exception {
        when(placementService.createPlacementDrive(any(PlacementDrive.class))).thenReturn(sampleDrive);

        mockMvc.perform(post("/api/placement-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDrive)))
                .andExpect(status().isCreated());

        verify(placementService, times(1)).createPlacementDrive(any(PlacementDrive.class));
    }

    @Test
    @DisplayName("POST /api/placement-drives - ADMIN is permitted (201 Created)")
    @WithMockUser(roles = "ADMIN")
    void createPlacementDrive_admin_permitted() throws Exception {
        when(placementService.createPlacementDrive(any(PlacementDrive.class))).thenReturn(sampleDrive);

        mockMvc.perform(post("/api/placement-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDrive)))
                .andExpect(status().isCreated());

        verify(placementService, times(1)).createPlacementDrive(any(PlacementDrive.class));
    }

    @Test
    @DisplayName("POST /api/placement-drives - STUDENT is forbidden (403 Forbidden)")
    @WithMockUser(roles = "STUDENT")
    void createPlacementDrive_student_forbidden() throws Exception {
        mockMvc.perform(post("/api/placement-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDrive)))
                .andExpect(status().isForbidden());

        verify(placementService, never()).createPlacementDrive(any());
    }

    @Test
    @DisplayName("POST /api/placement-drives - FACULTY is forbidden (403 Forbidden)")
    @WithMockUser(roles = "FACULTY")
    void createPlacementDrive_faculty_forbidden() throws Exception {
        mockMvc.perform(post("/api/placement-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDrive)))
                .andExpect(status().isForbidden());

        verify(placementService, never()).createPlacementDrive(any());
    }

    @Test
    @DisplayName("POST /api/placement-drives - HOD is forbidden (403 Forbidden)")
    @WithMockUser(roles = "HOD")
    void createPlacementDrive_hod_forbidden() throws Exception {
        mockMvc.perform(post("/api/placement-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDrive)))
                .andExpect(status().isForbidden());

        verify(placementService, never()).createPlacementDrive(any());
    }

    @Test
    @DisplayName("POST /api/placement-drives - COMPANY is forbidden (403 Forbidden)")
    @WithMockUser(roles = "COMPANY")
    void createPlacementDrive_company_forbidden() throws Exception {
        mockMvc.perform(post("/api/placement-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDrive)))
                .andExpect(status().isForbidden());

        verify(placementService, never()).createPlacementDrive(any());
    }

    @Test
    @DisplayName("POST /api/placement-drives - Unauthenticated request is rejected (401 or 403)")
    void createPlacementDrive_unauthenticated_rejected() throws Exception {
        mockMvc.perform(post("/api/placement-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDrive)))
                .andExpect(status().isForbidden());

        verify(placementService, never()).createPlacementDrive(any());
    }

    // =========================================================================
    // 2. PUT /api/placement-drives/{id}
    // =========================================================================

    @Test
    @DisplayName("PUT /api/placement-drives/{id} - OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "OFFICER")
    void updatePlacementDrive_officer_permitted() throws Exception {
        when(placementService.updatePlacementDrive(eq(10L), any(PlacementDrive.class))).thenReturn(sampleDrive);

        mockMvc.perform(put("/api/placement-drives/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDrive)))
                .andExpect(status().isOk());

        verify(placementService, times(1)).updatePlacementDrive(eq(10L), any(PlacementDrive.class));
    }

    @Test
    @DisplayName("PUT /api/placement-drives/{id} - PLACEMENT_OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void updatePlacementDrive_placementOfficer_permitted() throws Exception {
        when(placementService.updatePlacementDrive(eq(10L), any(PlacementDrive.class))).thenReturn(sampleDrive);

        mockMvc.perform(put("/api/placement-drives/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDrive)))
                .andExpect(status().isOk());

        verify(placementService, times(1)).updatePlacementDrive(eq(10L), any(PlacementDrive.class));
    }

    @Test
    @DisplayName("PUT /api/placement-drives/{id} - ADMIN is permitted (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void updatePlacementDrive_admin_permitted() throws Exception {
        when(placementService.updatePlacementDrive(eq(10L), any(PlacementDrive.class))).thenReturn(sampleDrive);

        mockMvc.perform(put("/api/placement-drives/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDrive)))
                .andExpect(status().isOk());

        verify(placementService, times(1)).updatePlacementDrive(eq(10L), any(PlacementDrive.class));
    }

    @Test
    @DisplayName("PUT /api/placement-drives/{id} - STUDENT is forbidden (403 Forbidden)")
    @WithMockUser(roles = "STUDENT")
    void updatePlacementDrive_student_forbidden() throws Exception {
        mockMvc.perform(put("/api/placement-drives/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDrive)))
                .andExpect(status().isForbidden());

        verify(placementService, never()).updatePlacementDrive(any(), any());
    }

    @Test
    @DisplayName("PUT /api/placement-drives/{id} - FACULTY is forbidden (403 Forbidden)")
    @WithMockUser(roles = "FACULTY")
    void updatePlacementDrive_faculty_forbidden() throws Exception {
        mockMvc.perform(put("/api/placement-drives/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDrive)))
                .andExpect(status().isForbidden());

        verify(placementService, never()).updatePlacementDrive(any(), any());
    }

    @Test
    @DisplayName("PUT /api/placement-drives/{id} - HOD is forbidden (403 Forbidden)")
    @WithMockUser(roles = "HOD")
    void updatePlacementDrive_hod_forbidden() throws Exception {
        mockMvc.perform(put("/api/placement-drives/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDrive)))
                .andExpect(status().isForbidden());

        verify(placementService, never()).updatePlacementDrive(any(), any());
    }

    @Test
    @DisplayName("PUT /api/placement-drives/{id} - COMPANY is forbidden (403 Forbidden)")
    @WithMockUser(roles = "COMPANY")
    void updatePlacementDrive_company_forbidden() throws Exception {
        mockMvc.perform(put("/api/placement-drives/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDrive)))
                .andExpect(status().isForbidden());

        verify(placementService, never()).updatePlacementDrive(any(), any());
    }

    @Test
    @DisplayName("PUT /api/placement-drives/{id} - Unauthenticated request is rejected (401 or 403)")
    void updatePlacementDrive_unauthenticated_rejected() throws Exception {
        mockMvc.perform(put("/api/placement-drives/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDrive)))
                .andExpect(status().isForbidden());

        verify(placementService, never()).updatePlacementDrive(any(), any());
    }

    // =========================================================================
    // 3. DELETE /api/placement-drives/{id}
    // =========================================================================

    @Test
    @DisplayName("DELETE /api/placement-drives/{id} - OFFICER is permitted (204 No Content)")
    @WithMockUser(roles = "OFFICER")
    void deletePlacementDrive_officer_permitted() throws Exception {
        doNothing().when(placementService).deletePlacementDrive(10L);

        mockMvc.perform(delete("/api/placement-drives/10"))
                .andExpect(status().isNoContent());

        verify(placementService, times(1)).deletePlacementDrive(10L);
    }

    @Test
    @DisplayName("DELETE /api/placement-drives/{id} - PLACEMENT_OFFICER is permitted (204 No Content)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void deletePlacementDrive_placementOfficer_permitted() throws Exception {
        doNothing().when(placementService).deletePlacementDrive(10L);

        mockMvc.perform(delete("/api/placement-drives/10"))
                .andExpect(status().isNoContent());

        verify(placementService, times(1)).deletePlacementDrive(10L);
    }

    @Test
    @DisplayName("DELETE /api/placement-drives/{id} - ADMIN is permitted (204 No Content)")
    @WithMockUser(roles = "ADMIN")
    void deletePlacementDrive_admin_permitted() throws Exception {
        doNothing().when(placementService).deletePlacementDrive(10L);

        mockMvc.perform(delete("/api/placement-drives/10"))
                .andExpect(status().isNoContent());

        verify(placementService, times(1)).deletePlacementDrive(10L);
    }

    @Test
    @DisplayName("DELETE /api/placement-drives/{id} - STUDENT is forbidden (403 Forbidden)")
    @WithMockUser(roles = "STUDENT")
    void deletePlacementDrive_student_forbidden() throws Exception {
        mockMvc.perform(delete("/api/placement-drives/10"))
                .andExpect(status().isForbidden());

        verify(placementService, never()).deletePlacementDrive(any());
    }

    @Test
    @DisplayName("DELETE /api/placement-drives/{id} - FACULTY is forbidden (403 Forbidden)")
    @WithMockUser(roles = "FACULTY")
    void deletePlacementDrive_faculty_forbidden() throws Exception {
        mockMvc.perform(delete("/api/placement-drives/10"))
                .andExpect(status().isForbidden());

        verify(placementService, never()).deletePlacementDrive(any());
    }

    @Test
    @DisplayName("DELETE /api/placement-drives/{id} - HOD is forbidden (403 Forbidden)")
    @WithMockUser(roles = "HOD")
    void deletePlacementDrive_hod_forbidden() throws Exception {
        mockMvc.perform(delete("/api/placement-drives/10"))
                .andExpect(status().isForbidden());

        verify(placementService, never()).deletePlacementDrive(any());
    }

    @Test
    @DisplayName("DELETE /api/placement-drives/{id} - COMPANY is forbidden (403 Forbidden)")
    @WithMockUser(roles = "COMPANY")
    void deletePlacementDrive_company_forbidden() throws Exception {
        mockMvc.perform(delete("/api/placement-drives/10"))
                .andExpect(status().isForbidden());

        verify(placementService, never()).deletePlacementDrive(any());
    }

    @Test
    @DisplayName("DELETE /api/placement-drives/{id} - Unauthenticated request is rejected (401 or 403)")
    void deletePlacementDrive_unauthenticated_rejected() throws Exception {
        mockMvc.perform(delete("/api/placement-drives/10"))
                .andExpect(status().isForbidden());

        verify(placementService, never()).deletePlacementDrive(any());
    }

    // =========================================================================
    // 4. GET /api/placement-drives
    // =========================================================================

    @Test
    @DisplayName("GET /api/placement-drives - STUDENT is permitted (200 OK)")
    @WithMockUser(roles = "STUDENT")
    void getAllPlacementDrives_student_permitted() throws Exception {
        when(placementService.getAllPlacementDrives()).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getAllPlacementDrives();
    }

    @Test
    @DisplayName("GET /api/placement-drives - FACULTY is permitted (200 OK)")
    @WithMockUser(roles = "FACULTY")
    void getAllPlacementDrives_faculty_permitted() throws Exception {
        when(placementService.getAllPlacementDrives()).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getAllPlacementDrives();
    }

    @Test
    @DisplayName("GET /api/placement-drives - HOD is permitted (200 OK)")
    @WithMockUser(roles = "HOD")
    void getAllPlacementDrives_hod_permitted() throws Exception {
        when(placementService.getAllPlacementDrives()).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getAllPlacementDrives();
    }

    @Test
    @DisplayName("GET /api/placement-drives - OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "OFFICER")
    void getAllPlacementDrives_officer_permitted() throws Exception {
        when(placementService.getAllPlacementDrives()).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getAllPlacementDrives();
    }

    @Test
    @DisplayName("GET /api/placement-drives - PLACEMENT_OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void getAllPlacementDrives_placementOfficer_permitted() throws Exception {
        when(placementService.getAllPlacementDrives()).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getAllPlacementDrives();
    }

    @Test
    @DisplayName("GET /api/placement-drives - COMPANY is permitted (200 OK)")
    @WithMockUser(roles = "COMPANY")
    void getAllPlacementDrives_company_permitted() throws Exception {
        when(placementService.getAllPlacementDrives()).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getAllPlacementDrives();
    }

    @Test
    @DisplayName("GET /api/placement-drives - ADMIN is permitted (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void getAllPlacementDrives_admin_permitted() throws Exception {
        when(placementService.getAllPlacementDrives()).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getAllPlacementDrives();
    }

    @Test
    @DisplayName("GET /api/placement-drives - Unauthenticated request is rejected (401 or 403)")
    void getAllPlacementDrives_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/placement-drives"))
                .andExpect(status().isForbidden());

        verify(placementService, never()).getAllPlacementDrives();
    }

    // =========================================================================
    // 5. GET /api/placement-drives/{id}
    // =========================================================================

    @Test
    @DisplayName("GET /api/placement-drives/{id} - STUDENT is permitted (200 OK)")
    @WithMockUser(roles = "STUDENT")
    void getPlacementDriveById_student_permitted() throws Exception {
        when(placementService.getPlacementDriveById(10L)).thenReturn(sampleDrive);

        mockMvc.perform(get("/api/placement-drives/10"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getPlacementDriveById(10L);
    }

    @Test
    @DisplayName("GET /api/placement-drives/{id} - FACULTY is permitted (200 OK)")
    @WithMockUser(roles = "FACULTY")
    void getPlacementDriveById_faculty_permitted() throws Exception {
        when(placementService.getPlacementDriveById(10L)).thenReturn(sampleDrive);

        mockMvc.perform(get("/api/placement-drives/10"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getPlacementDriveById(10L);
    }

    @Test
    @DisplayName("GET /api/placement-drives/{id} - HOD is permitted (200 OK)")
    @WithMockUser(roles = "HOD")
    void getPlacementDriveById_hod_permitted() throws Exception {
        when(placementService.getPlacementDriveById(10L)).thenReturn(sampleDrive);

        mockMvc.perform(get("/api/placement-drives/10"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getPlacementDriveById(10L);
    }

    @Test
    @DisplayName("GET /api/placement-drives/{id} - OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "OFFICER")
    void getPlacementDriveById_officer_permitted() throws Exception {
        when(placementService.getPlacementDriveById(10L)).thenReturn(sampleDrive);

        mockMvc.perform(get("/api/placement-drives/10"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getPlacementDriveById(10L);
    }

    @Test
    @DisplayName("GET /api/placement-drives/{id} - PLACEMENT_OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void getPlacementDriveById_placementOfficer_permitted() throws Exception {
        when(placementService.getPlacementDriveById(10L)).thenReturn(sampleDrive);

        mockMvc.perform(get("/api/placement-drives/10"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getPlacementDriveById(10L);
    }

    @Test
    @DisplayName("GET /api/placement-drives/{id} - COMPANY is permitted (200 OK)")
    @WithMockUser(roles = "COMPANY")
    void getPlacementDriveById_company_permitted() throws Exception {
        when(placementService.getPlacementDriveById(10L)).thenReturn(sampleDrive);

        mockMvc.perform(get("/api/placement-drives/10"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getPlacementDriveById(10L);
    }

    @Test
    @DisplayName("GET /api/placement-drives/{id} - ADMIN is permitted (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void getPlacementDriveById_admin_permitted() throws Exception {
        when(placementService.getPlacementDriveById(10L)).thenReturn(sampleDrive);

        mockMvc.perform(get("/api/placement-drives/10"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getPlacementDriveById(10L);
    }

    @Test
    @DisplayName("GET /api/placement-drives/{id} - Unauthenticated request is rejected (401 or 403)")
    void getPlacementDriveById_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/placement-drives/10"))
                .andExpect(status().isForbidden());

        verify(placementService, never()).getPlacementDriveById(any());
    }

    // =========================================================================
    // 6. GET /api/placement-drives/company/{companyId}
    // =========================================================================

    @Test
    @DisplayName("GET /api/placement-drives/company/{companyId} - STUDENT is permitted (200 OK)")
    @WithMockUser(roles = "STUDENT")
    void getDrivesByCompany_student_permitted() throws Exception {
        when(placementService.getDrivesByCompany(1L)).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives/company/1"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getDrivesByCompany(1L);
    }

    @Test
    @DisplayName("GET /api/placement-drives/company/{companyId} - FACULTY is permitted (200 OK)")
    @WithMockUser(roles = "FACULTY")
    void getDrivesByCompany_faculty_permitted() throws Exception {
        when(placementService.getDrivesByCompany(1L)).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives/company/1"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getDrivesByCompany(1L);
    }

    @Test
    @DisplayName("GET /api/placement-drives/company/{companyId} - HOD is permitted (200 OK)")
    @WithMockUser(roles = "HOD")
    void getDrivesByCompany_hod_permitted() throws Exception {
        when(placementService.getDrivesByCompany(1L)).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives/company/1"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getDrivesByCompany(1L);
    }

    @Test
    @DisplayName("GET /api/placement-drives/company/{companyId} - OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "OFFICER")
    void getDrivesByCompany_officer_permitted() throws Exception {
        when(placementService.getDrivesByCompany(1L)).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives/company/1"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getDrivesByCompany(1L);
    }

    @Test
    @DisplayName("GET /api/placement-drives/company/{companyId} - PLACEMENT_OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void getDrivesByCompany_placementOfficer_permitted() throws Exception {
        when(placementService.getDrivesByCompany(1L)).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives/company/1"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getDrivesByCompany(1L);
    }

    @Test
    @DisplayName("GET /api/placement-drives/company/{companyId} - COMPANY is permitted (200 OK)")
    @WithMockUser(roles = "COMPANY")
    void getDrivesByCompany_company_permitted() throws Exception {
        when(placementService.getDrivesByCompany(1L)).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives/company/1"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getDrivesByCompany(1L);
    }

    @Test
    @DisplayName("GET /api/placement-drives/company/{companyId} - ADMIN is permitted (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void getDrivesByCompany_admin_permitted() throws Exception {
        when(placementService.getDrivesByCompany(1L)).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives/company/1"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getDrivesByCompany(1L);
    }

    @Test
    @DisplayName("GET /api/placement-drives/company/{companyId} - Unauthenticated request is rejected (401 or 403)")
    void getDrivesByCompany_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/placement-drives/company/1"))
                .andExpect(status().isForbidden());

        verify(placementService, never()).getDrivesByCompany(any());
    }

    // =========================================================================
    // 7. GET /api/placement-drives/status/{status}
    // =========================================================================

    @Test
    @DisplayName("GET /api/placement-drives/status/{status} - STUDENT is permitted (200 OK)")
    @WithMockUser(roles = "STUDENT")
    void getDrivesByStatus_student_permitted() throws Exception {
        when(placementService.getDrivesByStatus(DriveStatus.UPCOMING)).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives/status/UPCOMING"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getDrivesByStatus(DriveStatus.UPCOMING);
    }

    @Test
    @DisplayName("GET /api/placement-drives/status/{status} - FACULTY is permitted (200 OK)")
    @WithMockUser(roles = "FACULTY")
    void getDrivesByStatus_faculty_permitted() throws Exception {
        when(placementService.getDrivesByStatus(DriveStatus.UPCOMING)).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives/status/UPCOMING"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getDrivesByStatus(DriveStatus.UPCOMING);
    }

    @Test
    @DisplayName("GET /api/placement-drives/status/{status} - HOD is permitted (200 OK)")
    @WithMockUser(roles = "HOD")
    void getDrivesByStatus_hod_permitted() throws Exception {
        when(placementService.getDrivesByStatus(DriveStatus.UPCOMING)).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives/status/UPCOMING"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getDrivesByStatus(DriveStatus.UPCOMING);
    }

    @Test
    @DisplayName("GET /api/placement-drives/status/{status} - OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "OFFICER")
    void getDrivesByStatus_officer_permitted() throws Exception {
        when(placementService.getDrivesByStatus(DriveStatus.UPCOMING)).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives/status/UPCOMING"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getDrivesByStatus(DriveStatus.UPCOMING);
    }

    @Test
    @DisplayName("GET /api/placement-drives/status/{status} - PLACEMENT_OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void getDrivesByStatus_placementOfficer_permitted() throws Exception {
        when(placementService.getDrivesByStatus(DriveStatus.UPCOMING)).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives/status/UPCOMING"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getDrivesByStatus(DriveStatus.UPCOMING);
    }

    @Test
    @DisplayName("GET /api/placement-drives/status/{status} - COMPANY is permitted (200 OK)")
    @WithMockUser(roles = "COMPANY")
    void getDrivesByStatus_company_permitted() throws Exception {
        when(placementService.getDrivesByStatus(DriveStatus.UPCOMING)).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives/status/UPCOMING"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getDrivesByStatus(DriveStatus.UPCOMING);
    }

    @Test
    @DisplayName("GET /api/placement-drives/status/{status} - ADMIN is permitted (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void getDrivesByStatus_admin_permitted() throws Exception {
        when(placementService.getDrivesByStatus(DriveStatus.UPCOMING)).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives/status/UPCOMING"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).getDrivesByStatus(DriveStatus.UPCOMING);
    }

    @Test
    @DisplayName("GET /api/placement-drives/status/{status} - Unauthenticated request is rejected (401 or 403)")
    void getDrivesByStatus_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/placement-drives/status/UPCOMING"))
                .andExpect(status().isForbidden());

        verify(placementService, never()).getDrivesByStatus(any());
    }

    // =========================================================================
    // 8. GET /api/placement-drives/search
    // =========================================================================

    @Test
    @DisplayName("GET /api/placement-drives/search - STUDENT is permitted (200 OK)")
    @WithMockUser(roles = "STUDENT")
    void searchDrivesByJobTitle_student_permitted() throws Exception {
        when(placementService.searchDrivesByJobTitle(eq("Engineer"))).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives/search").param("jobTitle", "Engineer"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).searchDrivesByJobTitle(eq("Engineer"));
    }

    @Test
    @DisplayName("GET /api/placement-drives/search - FACULTY is permitted (200 OK)")
    @WithMockUser(roles = "FACULTY")
    void searchDrivesByJobTitle_faculty_permitted() throws Exception {
        when(placementService.searchDrivesByJobTitle(eq("Engineer"))).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives/search").param("jobTitle", "Engineer"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).searchDrivesByJobTitle(eq("Engineer"));
    }

    @Test
    @DisplayName("GET /api/placement-drives/search - HOD is permitted (200 OK)")
    @WithMockUser(roles = "HOD")
    void searchDrivesByJobTitle_hod_permitted() throws Exception {
        when(placementService.searchDrivesByJobTitle(eq("Engineer"))).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives/search").param("jobTitle", "Engineer"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).searchDrivesByJobTitle(eq("Engineer"));
    }

    @Test
    @DisplayName("GET /api/placement-drives/search - OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "OFFICER")
    void searchDrivesByJobTitle_officer_permitted() throws Exception {
        when(placementService.searchDrivesByJobTitle(eq("Engineer"))).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives/search").param("jobTitle", "Engineer"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).searchDrivesByJobTitle(eq("Engineer"));
    }

    @Test
    @DisplayName("GET /api/placement-drives/search - PLACEMENT_OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void searchDrivesByJobTitle_placementOfficer_permitted() throws Exception {
        when(placementService.searchDrivesByJobTitle(eq("Engineer"))).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives/search").param("jobTitle", "Engineer"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).searchDrivesByJobTitle(eq("Engineer"));
    }

    @Test
    @DisplayName("GET /api/placement-drives/search - COMPANY is permitted (200 OK)")
    @WithMockUser(roles = "COMPANY")
    void searchDrivesByJobTitle_company_permitted() throws Exception {
        when(placementService.searchDrivesByJobTitle(eq("Engineer"))).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives/search").param("jobTitle", "Engineer"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).searchDrivesByJobTitle(eq("Engineer"));
    }

    @Test
    @DisplayName("GET /api/placement-drives/search - ADMIN is permitted (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void searchDrivesByJobTitle_admin_permitted() throws Exception {
        when(placementService.searchDrivesByJobTitle(eq("Engineer"))).thenReturn(List.of(sampleDrive));

        mockMvc.perform(get("/api/placement-drives/search").param("jobTitle", "Engineer"))
                .andExpect(status().isOk());

        verify(placementService, times(1)).searchDrivesByJobTitle(eq("Engineer"));
    }

    @Test
    @DisplayName("GET /api/placement-drives/search - Unauthenticated request is rejected (401 or 403)")
    void searchDrivesByJobTitle_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/placement-drives/search").param("jobTitle", "Engineer"))
                .andExpect(status().isForbidden());

        verify(placementService, never()).searchDrivesByJobTitle(any());
    }
}

