package com.placement.company;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyController.class)
@Import({SecurityConfig.class, CorsConfig.class})
@DisplayName("CompanyController Security and Authorization Tests")
class CompanyControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyService companyService;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private Company sampleCompany;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper.findAndRegisterModules();

        doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());

        sampleCompany = new Company(
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
    }

    // =========================================================================
    // 1. POST /api/companies
    // =========================================================================

    @Test
    @DisplayName("POST /api/companies - OFFICER is permitted (201 Created)")
    @WithMockUser(roles = "OFFICER")
    void createCompany_officer_permitted() throws Exception {
        when(companyService.createCompany(any(Company.class))).thenReturn(sampleCompany);

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCompany)))
                .andExpect(status().isCreated());

        verify(companyService, times(1)).createCompany(any(Company.class));
    }

    @Test
    @DisplayName("POST /api/companies - PLACEMENT_OFFICER is permitted (201 Created)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void createCompany_placementOfficer_permitted() throws Exception {
        when(companyService.createCompany(any(Company.class))).thenReturn(sampleCompany);

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCompany)))
                .andExpect(status().isCreated());

        verify(companyService, times(1)).createCompany(any(Company.class));
    }

    @Test
    @DisplayName("POST /api/companies - ADMIN is permitted (201 Created)")
    @WithMockUser(roles = "ADMIN")
    void createCompany_admin_permitted() throws Exception {
        when(companyService.createCompany(any(Company.class))).thenReturn(sampleCompany);

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCompany)))
                .andExpect(status().isCreated());

        verify(companyService, times(1)).createCompany(any(Company.class));
    }

    @Test
    @DisplayName("POST /api/companies - STUDENT is forbidden (403 Forbidden)")
    @WithMockUser(roles = "STUDENT")
    void createCompany_student_forbidden() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCompany)))
                .andExpect(status().isForbidden());

        verify(companyService, never()).createCompany(any());
    }

    @Test
    @DisplayName("POST /api/companies - FACULTY is forbidden (403 Forbidden)")
    @WithMockUser(roles = "FACULTY")
    void createCompany_faculty_forbidden() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCompany)))
                .andExpect(status().isForbidden());

        verify(companyService, never()).createCompany(any());
    }

    @Test
    @DisplayName("POST /api/companies - HOD is forbidden (403 Forbidden)")
    @WithMockUser(roles = "HOD")
    void createCompany_hod_forbidden() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCompany)))
                .andExpect(status().isForbidden());

        verify(companyService, never()).createCompany(any());
    }

    @Test
    @DisplayName("POST /api/companies - COMPANY is forbidden (403 Forbidden)")
    @WithMockUser(roles = "COMPANY")
    void createCompany_company_forbidden() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCompany)))
                .andExpect(status().isForbidden());

        verify(companyService, never()).createCompany(any());
    }

    @Test
    @DisplayName("POST /api/companies - Unauthenticated request is rejected (403 Forbidden)")
    void createCompany_unauthenticated_rejected() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCompany)))
                .andExpect(status().isForbidden());

        verify(companyService, never()).createCompany(any());
    }

    // =========================================================================
    // 2. PUT /api/companies/{id}
    // =========================================================================

    @Test
    @DisplayName("PUT /api/companies/{id} - OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "OFFICER")
    void updateCompany_officer_permitted() throws Exception {
        when(companyService.updateCompany(eq(1L), any(Company.class))).thenReturn(sampleCompany);

        mockMvc.perform(put("/api/companies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCompany)))
                .andExpect(status().isOk());

        verify(companyService, times(1)).updateCompany(eq(1L), any(Company.class));
    }

    @Test
    @DisplayName("PUT /api/companies/{id} - PLACEMENT_OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void updateCompany_placementOfficer_permitted() throws Exception {
        when(companyService.updateCompany(eq(1L), any(Company.class))).thenReturn(sampleCompany);

        mockMvc.perform(put("/api/companies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCompany)))
                .andExpect(status().isOk());

        verify(companyService, times(1)).updateCompany(eq(1L), any(Company.class));
    }

    @Test
    @DisplayName("PUT /api/companies/{id} - ADMIN is permitted (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void updateCompany_admin_permitted() throws Exception {
        when(companyService.updateCompany(eq(1L), any(Company.class))).thenReturn(sampleCompany);

        mockMvc.perform(put("/api/companies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCompany)))
                .andExpect(status().isOk());

        verify(companyService, times(1)).updateCompany(eq(1L), any(Company.class));
    }

    @Test
    @DisplayName("PUT /api/companies/{id} - STUDENT is forbidden (403 Forbidden)")
    @WithMockUser(roles = "STUDENT")
    void updateCompany_student_forbidden() throws Exception {
        mockMvc.perform(put("/api/companies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCompany)))
                .andExpect(status().isForbidden());

        verify(companyService, never()).updateCompany(any(), any());
    }

    @Test
    @DisplayName("PUT /api/companies/{id} - FACULTY is forbidden (403 Forbidden)")
    @WithMockUser(roles = "FACULTY")
    void updateCompany_faculty_forbidden() throws Exception {
        mockMvc.perform(put("/api/companies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCompany)))
                .andExpect(status().isForbidden());

        verify(companyService, never()).updateCompany(any(), any());
    }

    @Test
    @DisplayName("PUT /api/companies/{id} - HOD is forbidden (403 Forbidden)")
    @WithMockUser(roles = "HOD")
    void updateCompany_hod_forbidden() throws Exception {
        mockMvc.perform(put("/api/companies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCompany)))
                .andExpect(status().isForbidden());

        verify(companyService, never()).updateCompany(any(), any());
    }

    @Test
    @DisplayName("PUT /api/companies/{id} - COMPANY is forbidden (403 Forbidden)")
    @WithMockUser(roles = "COMPANY")
    void updateCompany_company_forbidden() throws Exception {
        mockMvc.perform(put("/api/companies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCompany)))
                .andExpect(status().isForbidden());

        verify(companyService, never()).updateCompany(any(), any());
    }

    @Test
    @DisplayName("PUT /api/companies/{id} - Unauthenticated request is rejected (403 Forbidden)")
    void updateCompany_unauthenticated_rejected() throws Exception {
        mockMvc.perform(put("/api/companies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCompany)))
                .andExpect(status().isForbidden());

        verify(companyService, never()).updateCompany(any(), any());
    }

    // =========================================================================
    // 3. DELETE /api/companies/{id}
    // =========================================================================

    @Test
    @DisplayName("DELETE /api/companies/{id} - OFFICER is permitted (204 No Content)")
    @WithMockUser(roles = "OFFICER")
    void deleteCompany_officer_permitted() throws Exception {
        doNothing().when(companyService).deleteCompany(1L);

        mockMvc.perform(delete("/api/companies/1"))
                .andExpect(status().isNoContent());

        verify(companyService, times(1)).deleteCompany(1L);
    }

    @Test
    @DisplayName("DELETE /api/companies/{id} - PLACEMENT_OFFICER is permitted (204 No Content)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void deleteCompany_placementOfficer_permitted() throws Exception {
        doNothing().when(companyService).deleteCompany(1L);

        mockMvc.perform(delete("/api/companies/1"))
                .andExpect(status().isNoContent());

        verify(companyService, times(1)).deleteCompany(1L);
    }

    @Test
    @DisplayName("DELETE /api/companies/{id} - ADMIN is permitted (204 No Content)")
    @WithMockUser(roles = "ADMIN")
    void deleteCompany_admin_permitted() throws Exception {
        doNothing().when(companyService).deleteCompany(1L);

        mockMvc.perform(delete("/api/companies/1"))
                .andExpect(status().isNoContent());

        verify(companyService, times(1)).deleteCompany(1L);
    }

    @Test
    @DisplayName("DELETE /api/companies/{id} - STUDENT is forbidden (403 Forbidden)")
    @WithMockUser(roles = "STUDENT")
    void deleteCompany_student_forbidden() throws Exception {
        mockMvc.perform(delete("/api/companies/1"))
                .andExpect(status().isForbidden());

        verify(companyService, never()).deleteCompany(any());
    }

    @Test
    @DisplayName("DELETE /api/companies/{id} - FACULTY is forbidden (403 Forbidden)")
    @WithMockUser(roles = "FACULTY")
    void deleteCompany_faculty_forbidden() throws Exception {
        mockMvc.perform(delete("/api/companies/1"))
                .andExpect(status().isForbidden());

        verify(companyService, never()).deleteCompany(any());
    }

    @Test
    @DisplayName("DELETE /api/companies/{id} - HOD is forbidden (403 Forbidden)")
    @WithMockUser(roles = "HOD")
    void deleteCompany_hod_forbidden() throws Exception {
        mockMvc.perform(delete("/api/companies/1"))
                .andExpect(status().isForbidden());

        verify(companyService, never()).deleteCompany(any());
    }

    @Test
    @DisplayName("DELETE /api/companies/{id} - COMPANY is forbidden (403 Forbidden)")
    @WithMockUser(roles = "COMPANY")
    void deleteCompany_company_forbidden() throws Exception {
        mockMvc.perform(delete("/api/companies/1"))
                .andExpect(status().isForbidden());

        verify(companyService, never()).deleteCompany(any());
    }

    @Test
    @DisplayName("DELETE /api/companies/{id} - Unauthenticated request is rejected (403 Forbidden)")
    void deleteCompany_unauthenticated_rejected() throws Exception {
        mockMvc.perform(delete("/api/companies/1"))
                .andExpect(status().isForbidden());

        verify(companyService, never()).deleteCompany(any());
    }

    // =========================================================================
    // 4. GET /api/companies
    // =========================================================================

    @Test
    @DisplayName("GET /api/companies - STUDENT is permitted (200 OK)")
    @WithMockUser(roles = "STUDENT")
    void getAllCompanies_student_permitted() throws Exception {
        when(companyService.getAllCompanies()).thenReturn(List.of(sampleCompany));

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk());

        verify(companyService, times(1)).getAllCompanies();
    }

    @Test
    @DisplayName("GET /api/companies - FACULTY is permitted (200 OK)")
    @WithMockUser(roles = "FACULTY")
    void getAllCompanies_faculty_permitted() throws Exception {
        when(companyService.getAllCompanies()).thenReturn(List.of(sampleCompany));

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk());

        verify(companyService, times(1)).getAllCompanies();
    }

    @Test
    @DisplayName("GET /api/companies - HOD is permitted (200 OK)")
    @WithMockUser(roles = "HOD")
    void getAllCompanies_hod_permitted() throws Exception {
        when(companyService.getAllCompanies()).thenReturn(List.of(sampleCompany));

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk());

        verify(companyService, times(1)).getAllCompanies();
    }

    @Test
    @DisplayName("GET /api/companies - OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "OFFICER")
    void getAllCompanies_officer_permitted() throws Exception {
        when(companyService.getAllCompanies()).thenReturn(List.of(sampleCompany));

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk());

        verify(companyService, times(1)).getAllCompanies();
    }

    @Test
    @DisplayName("GET /api/companies - PLACEMENT_OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void getAllCompanies_placementOfficer_permitted() throws Exception {
        when(companyService.getAllCompanies()).thenReturn(List.of(sampleCompany));

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk());

        verify(companyService, times(1)).getAllCompanies();
    }

    @Test
    @DisplayName("GET /api/companies - COMPANY is permitted (200 OK)")
    @WithMockUser(roles = "COMPANY")
    void getAllCompanies_company_permitted() throws Exception {
        when(companyService.getAllCompanies()).thenReturn(List.of(sampleCompany));

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk());

        verify(companyService, times(1)).getAllCompanies();
    }

    @Test
    @DisplayName("GET /api/companies - ADMIN is permitted (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void getAllCompanies_admin_permitted() throws Exception {
        when(companyService.getAllCompanies()).thenReturn(List.of(sampleCompany));

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk());

        verify(companyService, times(1)).getAllCompanies();
    }

    @Test
    @DisplayName("GET /api/companies - Unauthenticated request is rejected (403 Forbidden)")
    void getAllCompanies_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isForbidden());

        verify(companyService, never()).getAllCompanies();
    }

    // =========================================================================
    // 5. GET /api/companies/{id}
    // =========================================================================

    @Test
    @DisplayName("GET /api/companies/{id} - STUDENT is permitted (200 OK)")
    @WithMockUser(roles = "STUDENT")
    void getCompanyById_student_permitted() throws Exception {
        when(companyService.getCompanyById(1L)).thenReturn(sampleCompany);

        mockMvc.perform(get("/api/companies/1"))
                .andExpect(status().isOk());

        verify(companyService, times(1)).getCompanyById(1L);
    }

    @Test
    @DisplayName("GET /api/companies/{id} - FACULTY is permitted (200 OK)")
    @WithMockUser(roles = "FACULTY")
    void getCompanyById_faculty_permitted() throws Exception {
        when(companyService.getCompanyById(1L)).thenReturn(sampleCompany);

        mockMvc.perform(get("/api/companies/1"))
                .andExpect(status().isOk());

        verify(companyService, times(1)).getCompanyById(1L);
    }

    @Test
    @DisplayName("GET /api/companies/{id} - HOD is permitted (200 OK)")
    @WithMockUser(roles = "HOD")
    void getCompanyById_hod_permitted() throws Exception {
        when(companyService.getCompanyById(1L)).thenReturn(sampleCompany);

        mockMvc.perform(get("/api/companies/1"))
                .andExpect(status().isOk());

        verify(companyService, times(1)).getCompanyById(1L);
    }

    @Test
    @DisplayName("GET /api/companies/{id} - OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "OFFICER")
    void getCompanyById_officer_permitted() throws Exception {
        when(companyService.getCompanyById(1L)).thenReturn(sampleCompany);

        mockMvc.perform(get("/api/companies/1"))
                .andExpect(status().isOk());

        verify(companyService, times(1)).getCompanyById(1L);
    }

    @Test
    @DisplayName("GET /api/companies/{id} - PLACEMENT_OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void getCompanyById_placementOfficer_permitted() throws Exception {
        when(companyService.getCompanyById(1L)).thenReturn(sampleCompany);

        mockMvc.perform(get("/api/companies/1"))
                .andExpect(status().isOk());

        verify(companyService, times(1)).getCompanyById(1L);
    }

    @Test
    @DisplayName("GET /api/companies/{id} - COMPANY is permitted (200 OK)")
    @WithMockUser(roles = "COMPANY")
    void getCompanyById_company_permitted() throws Exception {
        when(companyService.getCompanyById(1L)).thenReturn(sampleCompany);

        mockMvc.perform(get("/api/companies/1"))
                .andExpect(status().isOk());

        verify(companyService, times(1)).getCompanyById(1L);
    }

    @Test
    @DisplayName("GET /api/companies/{id} - ADMIN is permitted (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void getCompanyById_admin_permitted() throws Exception {
        when(companyService.getCompanyById(1L)).thenReturn(sampleCompany);

        mockMvc.perform(get("/api/companies/1"))
                .andExpect(status().isOk());

        verify(companyService, times(1)).getCompanyById(1L);
    }

    @Test
    @DisplayName("GET /api/companies/{id} - Unauthenticated request is rejected (403 Forbidden)")
    void getCompanyById_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/companies/1"))
                .andExpect(status().isForbidden());

        verify(companyService, never()).getCompanyById(any());
    }

    // =========================================================================
    // 6. GET /api/companies/search
    // =========================================================================

    @Test
    @DisplayName("GET /api/companies/search - STUDENT is permitted (200 OK)")
    @WithMockUser(roles = "STUDENT")
    void searchCompaniesByName_student_permitted() throws Exception {
        when(companyService.searchCompaniesByName("Acme")).thenReturn(List.of(sampleCompany));

        mockMvc.perform(get("/api/companies/search").param("name", "Acme"))
                .andExpect(status().isOk());

        verify(companyService, times(1)).searchCompaniesByName("Acme");
    }

    @Test
    @DisplayName("GET /api/companies/search - FACULTY is permitted (200 OK)")
    @WithMockUser(roles = "FACULTY")
    void searchCompaniesByName_faculty_permitted() throws Exception {
        when(companyService.searchCompaniesByName("Acme")).thenReturn(List.of(sampleCompany));

        mockMvc.perform(get("/api/companies/search").param("name", "Acme"))
                .andExpect(status().isOk());

        verify(companyService, times(1)).searchCompaniesByName("Acme");
    }

    @Test
    @DisplayName("GET /api/companies/search - HOD is permitted (200 OK)")
    @WithMockUser(roles = "HOD")
    void searchCompaniesByName_hod_permitted() throws Exception {
        when(companyService.searchCompaniesByName("Acme")).thenReturn(List.of(sampleCompany));

        mockMvc.perform(get("/api/companies/search").param("name", "Acme"))
                .andExpect(status().isOk());

        verify(companyService, times(1)).searchCompaniesByName("Acme");
    }

    @Test
    @DisplayName("GET /api/companies/search - OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "OFFICER")
    void searchCompaniesByName_officer_permitted() throws Exception {
        when(companyService.searchCompaniesByName("Acme")).thenReturn(List.of(sampleCompany));

        mockMvc.perform(get("/api/companies/search").param("name", "Acme"))
                .andExpect(status().isOk());

        verify(companyService, times(1)).searchCompaniesByName("Acme");
    }

    @Test
    @DisplayName("GET /api/companies/search - PLACEMENT_OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void searchCompaniesByName_placementOfficer_permitted() throws Exception {
        when(companyService.searchCompaniesByName("Acme")).thenReturn(List.of(sampleCompany));

        mockMvc.perform(get("/api/companies/search").param("name", "Acme"))
                .andExpect(status().isOk());

        verify(companyService, times(1)).searchCompaniesByName("Acme");
    }

    @Test
    @DisplayName("GET /api/companies/search - COMPANY is permitted (200 OK)")
    @WithMockUser(roles = "COMPANY")
    void searchCompaniesByName_company_permitted() throws Exception {
        when(companyService.searchCompaniesByName("Acme")).thenReturn(List.of(sampleCompany));

        mockMvc.perform(get("/api/companies/search").param("name", "Acme"))
                .andExpect(status().isOk());

        verify(companyService, times(1)).searchCompaniesByName("Acme");
    }

    @Test
    @DisplayName("GET /api/companies/search - ADMIN is permitted (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void searchCompaniesByName_admin_permitted() throws Exception {
        when(companyService.searchCompaniesByName("Acme")).thenReturn(List.of(sampleCompany));

        mockMvc.perform(get("/api/companies/search").param("name", "Acme"))
                .andExpect(status().isOk());

        verify(companyService, times(1)).searchCompaniesByName("Acme");
    }

    @Test
    @DisplayName("GET /api/companies/search - Unauthenticated request is rejected (403 Forbidden)")
    void searchCompaniesByName_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/companies/search").param("name", "Acme"))
                .andExpect(status().isForbidden());

        verify(companyService, never()).searchCompaniesByName(any());
    }
}

