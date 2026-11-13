package com.placement.questionbank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.config.CorsConfig;
import com.placement.config.SecurityConfig;
import com.placement.questionbank.dto.QuestionFilterRequest;
import com.placement.questionbank.dto.QuestionRequest;
import com.placement.questionbank.dto.QuestionResponse;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestionBankController.class)
@Import({SecurityConfig.class, CorsConfig.class})
@DisplayName("QuestionBankController Security and Authorization Tests")
class QuestionBankControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuestionBankService questionBankService;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private QuestionRequest validRequest;
    private QuestionResponse sampleResponse;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper.findAndRegisterModules();

        doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());

        validRequest = QuestionRequest.builder()
                .questionText("Explain the JVM memory model")
                .topic("Java")
                .company("Google")
                .targetRole("Software Engineer")
                .difficulty(InterviewQuestion.Difficulty.MEDIUM)
                .questionType(InterviewQuestion.QuestionType.TECHNICAL)
                .tags("jvm,memory")
                .build();

        sampleResponse = QuestionResponse.builder()
                .id(1L)
                .questionText("Explain the JVM memory model")
                .topic("Java")
                .company("Google")
                .difficulty(InterviewQuestion.Difficulty.MEDIUM)
                .questionType(InterviewQuestion.QuestionType.TECHNICAL)
                .createdBy("faculty@college.edu")
                .build();
    }

    // =========================================================================
    // 1. POST /api/questions
    // =========================================================================

    @Test
    @DisplayName("POST /api/questions - FACULTY is permitted (201 Created)")
    @WithMockUser(username = "faculty@college.edu", roles = "FACULTY")
    void createQuestion_faculty_permitted() throws Exception {
        when(questionBankService.create(any(QuestionRequest.class), anyString())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());

        verify(questionBankService, times(1)).create(any(QuestionRequest.class), anyString());
    }

    @Test
    @DisplayName("POST /api/questions - OFFICER is permitted (201 Created)")
    @WithMockUser(username = "officer@college.edu", roles = "OFFICER")
    void createQuestion_officer_permitted() throws Exception {
        when(questionBankService.create(any(QuestionRequest.class), anyString())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());

        verify(questionBankService, times(1)).create(any(QuestionRequest.class), anyString());
    }

    @Test
    @DisplayName("POST /api/questions - PLACEMENT_OFFICER is permitted (201 Created)")
    @WithMockUser(username = "po@college.edu", roles = "PLACEMENT_OFFICER")
    void createQuestion_placementOfficer_permitted() throws Exception {
        when(questionBankService.create(any(QuestionRequest.class), anyString())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());

        verify(questionBankService, times(1)).create(any(QuestionRequest.class), anyString());
    }

    @Test
    @DisplayName("POST /api/questions - ADMIN is permitted (201 Created)")
    @WithMockUser(username = "admin@college.edu", roles = "ADMIN")
    void createQuestion_admin_permitted() throws Exception {
        when(questionBankService.create(any(QuestionRequest.class), anyString())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());

        verify(questionBankService, times(1)).create(any(QuestionRequest.class), anyString());
    }

    @Test
    @DisplayName("POST /api/questions - STUDENT is forbidden (403 Forbidden)")
    @WithMockUser(username = "student@college.edu", roles = "STUDENT")
    void createQuestion_student_forbidden() throws Exception {
        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verify(questionBankService, never()).create(any(), any());
    }

    @Test
    @DisplayName("POST /api/questions - HOD is forbidden (403 Forbidden)")
    @WithMockUser(username = "hod@college.edu", roles = "HOD")
    void createQuestion_hod_forbidden() throws Exception {
        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verify(questionBankService, never()).create(any(), any());
    }

    @Test
    @DisplayName("POST /api/questions - COMPANY is forbidden (403 Forbidden)")
    @WithMockUser(username = "company@college.edu", roles = "COMPANY")
    void createQuestion_company_forbidden() throws Exception {
        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verify(questionBankService, never()).create(any(), any());
    }

    @Test
    @DisplayName("POST /api/questions - Unauthenticated request is rejected (403 Forbidden)")
    void createQuestion_unauthenticated_rejected() throws Exception {
        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verify(questionBankService, never()).create(any(), any());
    }

    // =========================================================================
    // 2. PUT /api/questions/{id}
    // =========================================================================

    @Test
    @DisplayName("PUT /api/questions/{id} - FACULTY is permitted (200 OK)")
    @WithMockUser(username = "faculty@college.edu", roles = "FACULTY")
    void updateQuestion_faculty_permitted() throws Exception {
        when(questionBankService.update(eq(1L), any(QuestionRequest.class), anyString())).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/questions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).update(eq(1L), any(QuestionRequest.class), anyString());
    }

    @Test
    @DisplayName("PUT /api/questions/{id} - OFFICER is permitted (200 OK)")
    @WithMockUser(username = "officer@college.edu", roles = "OFFICER")
    void updateQuestion_officer_permitted() throws Exception {
        when(questionBankService.update(eq(1L), any(QuestionRequest.class), anyString())).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/questions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).update(eq(1L), any(QuestionRequest.class), anyString());
    }

    @Test
    @DisplayName("PUT /api/questions/{id} - PLACEMENT_OFFICER is permitted (200 OK)")
    @WithMockUser(username = "po@college.edu", roles = "PLACEMENT_OFFICER")
    void updateQuestion_placementOfficer_permitted() throws Exception {
        when(questionBankService.update(eq(1L), any(QuestionRequest.class), anyString())).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/questions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).update(eq(1L), any(QuestionRequest.class), anyString());
    }

    @Test
    @DisplayName("PUT /api/questions/{id} - ADMIN is permitted (200 OK)")
    @WithMockUser(username = "admin@college.edu", roles = "ADMIN")
    void updateQuestion_admin_permitted() throws Exception {
        when(questionBankService.update(eq(1L), any(QuestionRequest.class), anyString())).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/questions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).update(eq(1L), any(QuestionRequest.class), anyString());
    }

    @Test
    @DisplayName("PUT /api/questions/{id} - STUDENT is forbidden (403 Forbidden)")
    @WithMockUser(username = "student@college.edu", roles = "STUDENT")
    void updateQuestion_student_forbidden() throws Exception {
        mockMvc.perform(put("/api/questions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verify(questionBankService, never()).update(any(), any(), any());
    }

    @Test
    @DisplayName("PUT /api/questions/{id} - HOD is forbidden (403 Forbidden)")
    @WithMockUser(username = "hod@college.edu", roles = "HOD")
    void updateQuestion_hod_forbidden() throws Exception {
        mockMvc.perform(put("/api/questions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verify(questionBankService, never()).update(any(), any(), any());
    }

    @Test
    @DisplayName("PUT /api/questions/{id} - COMPANY is forbidden (403 Forbidden)")
    @WithMockUser(username = "company@college.edu", roles = "COMPANY")
    void updateQuestion_company_forbidden() throws Exception {
        mockMvc.perform(put("/api/questions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verify(questionBankService, never()).update(any(), any(), any());
    }

    @Test
    @DisplayName("PUT /api/questions/{id} - Unauthenticated request is rejected (403 Forbidden)")
    void updateQuestion_unauthenticated_rejected() throws Exception {
        mockMvc.perform(put("/api/questions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verify(questionBankService, never()).update(any(), any(), any());
    }

    // =========================================================================
    // 3. DELETE /api/questions/{id}
    // =========================================================================

    @Test
    @DisplayName("DELETE /api/questions/{id} - OFFICER is permitted (204 No Content)")
    @WithMockUser(roles = "OFFICER")
    void deleteQuestion_officer_permitted() throws Exception {
        doNothing().when(questionBankService).delete(1L);

        mockMvc.perform(delete("/api/questions/1"))
                .andExpect(status().isNoContent());

        verify(questionBankService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/questions/{id} - PLACEMENT_OFFICER is permitted (204 No Content)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void deleteQuestion_placementOfficer_permitted() throws Exception {
        doNothing().when(questionBankService).delete(1L);

        mockMvc.perform(delete("/api/questions/1"))
                .andExpect(status().isNoContent());

        verify(questionBankService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/questions/{id} - ADMIN is permitted (204 No Content)")
    @WithMockUser(roles = "ADMIN")
    void deleteQuestion_admin_permitted() throws Exception {
        doNothing().when(questionBankService).delete(1L);

        mockMvc.perform(delete("/api/questions/1"))
                .andExpect(status().isNoContent());

        verify(questionBankService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/questions/{id} - STUDENT is forbidden (403 Forbidden)")
    @WithMockUser(roles = "STUDENT")
    void deleteQuestion_student_forbidden() throws Exception {
        mockMvc.perform(delete("/api/questions/1"))
                .andExpect(status().isForbidden());

        verify(questionBankService, never()).delete(any());
    }

    @Test
    @DisplayName("DELETE /api/questions/{id} - FACULTY is forbidden (403 Forbidden)")
    @WithMockUser(roles = "FACULTY")
    void deleteQuestion_faculty_forbidden() throws Exception {
        mockMvc.perform(delete("/api/questions/1"))
                .andExpect(status().isForbidden());

        verify(questionBankService, never()).delete(any());
    }

    @Test
    @DisplayName("DELETE /api/questions/{id} - HOD is forbidden (403 Forbidden)")
    @WithMockUser(roles = "HOD")
    void deleteQuestion_hod_forbidden() throws Exception {
        mockMvc.perform(delete("/api/questions/1"))
                .andExpect(status().isForbidden());

        verify(questionBankService, never()).delete(any());
    }

    @Test
    @DisplayName("DELETE /api/questions/{id} - COMPANY is forbidden (403 Forbidden)")
    @WithMockUser(roles = "COMPANY")
    void deleteQuestion_company_forbidden() throws Exception {
        mockMvc.perform(delete("/api/questions/1"))
                .andExpect(status().isForbidden());

        verify(questionBankService, never()).delete(any());
    }

    @Test
    @DisplayName("DELETE /api/questions/{id} - Unauthenticated request is rejected (403 Forbidden)")
    void deleteQuestion_unauthenticated_rejected() throws Exception {
        mockMvc.perform(delete("/api/questions/1"))
                .andExpect(status().isForbidden());

        verify(questionBankService, never()).delete(any());
    }

    // =========================================================================
    // 4. GET /api/questions/{id}
    // =========================================================================

    @Test
    @DisplayName("GET /api/questions/{id} - STUDENT is permitted (200 OK)")
    @WithMockUser(roles = "STUDENT")
    void getQuestionById_student_permitted() throws Exception {
        when(questionBankService.getById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/questions/1"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).getById(1L);
    }

    @Test
    @DisplayName("GET /api/questions/{id} - FACULTY is permitted (200 OK)")
    @WithMockUser(roles = "FACULTY")
    void getQuestionById_faculty_permitted() throws Exception {
        when(questionBankService.getById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/questions/1"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).getById(1L);
    }

    @Test
    @DisplayName("GET /api/questions/{id} - HOD is permitted (200 OK)")
    @WithMockUser(roles = "HOD")
    void getQuestionById_hod_permitted() throws Exception {
        when(questionBankService.getById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/questions/1"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).getById(1L);
    }

    @Test
    @DisplayName("GET /api/questions/{id} - OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "OFFICER")
    void getQuestionById_officer_permitted() throws Exception {
        when(questionBankService.getById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/questions/1"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).getById(1L);
    }

    @Test
    @DisplayName("GET /api/questions/{id} - PLACEMENT_OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void getQuestionById_placementOfficer_permitted() throws Exception {
        when(questionBankService.getById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/questions/1"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).getById(1L);
    }

    @Test
    @DisplayName("GET /api/questions/{id} - COMPANY is permitted (200 OK)")
    @WithMockUser(roles = "COMPANY")
    void getQuestionById_company_permitted() throws Exception {
        when(questionBankService.getById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/questions/1"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).getById(1L);
    }

    @Test
    @DisplayName("GET /api/questions/{id} - ADMIN is permitted (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void getQuestionById_admin_permitted() throws Exception {
        when(questionBankService.getById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/questions/1"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).getById(1L);
    }

    @Test
    @DisplayName("GET /api/questions/{id} - Unauthenticated request is rejected (403 Forbidden)")
    void getQuestionById_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/questions/1"))
                .andExpect(status().isForbidden());

        verify(questionBankService, never()).getById(any());
    }

    // =========================================================================
    // 5. GET /api/questions (filter/paginated list)
    // =========================================================================

    @Test
    @DisplayName("GET /api/questions - STUDENT is permitted (200 OK)")
    @WithMockUser(roles = "STUDENT")
    void listQuestions_student_permitted() throws Exception {
        when(questionBankService.filter(any(QuestionFilterRequest.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse)));

        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).filter(any(QuestionFilterRequest.class), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/questions - FACULTY is permitted (200 OK)")
    @WithMockUser(roles = "FACULTY")
    void listQuestions_faculty_permitted() throws Exception {
        when(questionBankService.filter(any(QuestionFilterRequest.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse)));

        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).filter(any(QuestionFilterRequest.class), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/questions - HOD is permitted (200 OK)")
    @WithMockUser(roles = "HOD")
    void listQuestions_hod_permitted() throws Exception {
        when(questionBankService.filter(any(QuestionFilterRequest.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse)));

        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).filter(any(QuestionFilterRequest.class), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/questions - OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "OFFICER")
    void listQuestions_officer_permitted() throws Exception {
        when(questionBankService.filter(any(QuestionFilterRequest.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse)));

        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).filter(any(QuestionFilterRequest.class), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/questions - PLACEMENT_OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void listQuestions_placementOfficer_permitted() throws Exception {
        when(questionBankService.filter(any(QuestionFilterRequest.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse)));

        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).filter(any(QuestionFilterRequest.class), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/questions - COMPANY is permitted (200 OK)")
    @WithMockUser(roles = "COMPANY")
    void listQuestions_company_permitted() throws Exception {
        when(questionBankService.filter(any(QuestionFilterRequest.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse)));

        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).filter(any(QuestionFilterRequest.class), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/questions - ADMIN is permitted (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void listQuestions_admin_permitted() throws Exception {
        when(questionBankService.filter(any(QuestionFilterRequest.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse)));

        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).filter(any(QuestionFilterRequest.class), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/questions - Unauthenticated request is rejected (403 Forbidden)")
    void listQuestions_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isForbidden());

        verify(questionBankService, never()).filter(any(), any());
    }

    // =========================================================================
    // 6. GET /api/questions/meta/companies
    // =========================================================================

    @Test
    @DisplayName("GET /api/questions/meta/companies - STUDENT is permitted (200 OK)")
    @WithMockUser(roles = "STUDENT")
    void getCompanies_student_permitted() throws Exception {
        when(questionBankService.getDistinctCompanies()).thenReturn(List.of("Google", "Microsoft"));

        mockMvc.perform(get("/api/questions/meta/companies"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).getDistinctCompanies();
    }

    @Test
    @DisplayName("GET /api/questions/meta/companies - FACULTY is permitted (200 OK)")
    @WithMockUser(roles = "FACULTY")
    void getCompanies_faculty_permitted() throws Exception {
        when(questionBankService.getDistinctCompanies()).thenReturn(List.of("Google", "Microsoft"));

        mockMvc.perform(get("/api/questions/meta/companies"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).getDistinctCompanies();
    }

    @Test
    @DisplayName("GET /api/questions/meta/companies - HOD is permitted (200 OK)")
    @WithMockUser(roles = "HOD")
    void getCompanies_hod_permitted() throws Exception {
        when(questionBankService.getDistinctCompanies()).thenReturn(List.of("Google", "Microsoft"));

        mockMvc.perform(get("/api/questions/meta/companies"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).getDistinctCompanies();
    }

    @Test
    @DisplayName("GET /api/questions/meta/companies - OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "OFFICER")
    void getCompanies_officer_permitted() throws Exception {
        when(questionBankService.getDistinctCompanies()).thenReturn(List.of("Google", "Microsoft"));

        mockMvc.perform(get("/api/questions/meta/companies"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).getDistinctCompanies();
    }

    @Test
    @DisplayName("GET /api/questions/meta/companies - PLACEMENT_OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void getCompanies_placementOfficer_permitted() throws Exception {
        when(questionBankService.getDistinctCompanies()).thenReturn(List.of("Google", "Microsoft"));

        mockMvc.perform(get("/api/questions/meta/companies"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).getDistinctCompanies();
    }

    @Test
    @DisplayName("GET /api/questions/meta/companies - COMPANY is permitted (200 OK)")
    @WithMockUser(roles = "COMPANY")
    void getCompanies_company_permitted() throws Exception {
        when(questionBankService.getDistinctCompanies()).thenReturn(List.of("Google", "Microsoft"));

        mockMvc.perform(get("/api/questions/meta/companies"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).getDistinctCompanies();
    }

    @Test
    @DisplayName("GET /api/questions/meta/companies - ADMIN is permitted (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void getCompanies_admin_permitted() throws Exception {
        when(questionBankService.getDistinctCompanies()).thenReturn(List.of("Google", "Microsoft"));

        mockMvc.perform(get("/api/questions/meta/companies"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).getDistinctCompanies();
    }

    @Test
    @DisplayName("GET /api/questions/meta/companies - Unauthenticated request is rejected (403 Forbidden)")
    void getCompanies_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/questions/meta/companies"))
                .andExpect(status().isForbidden());

        verify(questionBankService, never()).getDistinctCompanies();
    }

    // =========================================================================
    // 7. GET /api/questions/meta/topics
    // =========================================================================

    @Test
    @DisplayName("GET /api/questions/meta/topics - STUDENT is permitted (200 OK)")
    @WithMockUser(roles = "STUDENT")
    void getTopics_student_permitted() throws Exception {
        when(questionBankService.getDistinctTopics()).thenReturn(List.of("Java", "Algorithms"));

        mockMvc.perform(get("/api/questions/meta/topics"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).getDistinctTopics();
    }

    @Test
    @DisplayName("GET /api/questions/meta/topics - FACULTY is permitted (200 OK)")
    @WithMockUser(roles = "FACULTY")
    void getTopics_faculty_permitted() throws Exception {
        when(questionBankService.getDistinctTopics()).thenReturn(List.of("Java", "Algorithms"));

        mockMvc.perform(get("/api/questions/meta/topics"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).getDistinctTopics();
    }

    @Test
    @DisplayName("GET /api/questions/meta/topics - HOD is permitted (200 OK)")
    @WithMockUser(roles = "HOD")
    void getTopics_hod_permitted() throws Exception {
        when(questionBankService.getDistinctTopics()).thenReturn(List.of("Java", "Algorithms"));

        mockMvc.perform(get("/api/questions/meta/topics"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).getDistinctTopics();
    }

    @Test
    @DisplayName("GET /api/questions/meta/topics - OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "OFFICER")
    void getTopics_officer_permitted() throws Exception {
        when(questionBankService.getDistinctTopics()).thenReturn(List.of("Java", "Algorithms"));

        mockMvc.perform(get("/api/questions/meta/topics"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).getDistinctTopics();
    }

    @Test
    @DisplayName("GET /api/questions/meta/topics - PLACEMENT_OFFICER is permitted (200 OK)")
    @WithMockUser(roles = "PLACEMENT_OFFICER")
    void getTopics_placementOfficer_permitted() throws Exception {
        when(questionBankService.getDistinctTopics()).thenReturn(List.of("Java", "Algorithms"));

        mockMvc.perform(get("/api/questions/meta/topics"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).getDistinctTopics();
    }

    @Test
    @DisplayName("GET /api/questions/meta/topics - COMPANY is permitted (200 OK)")
    @WithMockUser(roles = "COMPANY")
    void getTopics_company_permitted() throws Exception {
        when(questionBankService.getDistinctTopics()).thenReturn(List.of("Java", "Algorithms"));

        mockMvc.perform(get("/api/questions/meta/topics"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).getDistinctTopics();
    }

    @Test
    @DisplayName("GET /api/questions/meta/topics - ADMIN is permitted (200 OK)")
    @WithMockUser(roles = "ADMIN")
    void getTopics_admin_permitted() throws Exception {
        when(questionBankService.getDistinctTopics()).thenReturn(List.of("Java", "Algorithms"));

        mockMvc.perform(get("/api/questions/meta/topics"))
                .andExpect(status().isOk());

        verify(questionBankService, times(1)).getDistinctTopics();
    }

    @Test
    @DisplayName("GET /api/questions/meta/topics - Unauthenticated request is rejected (403 Forbidden)")
    void getTopics_unauthenticated_rejected() throws Exception {
        mockMvc.perform(get("/api/questions/meta/topics"))
                .andExpect(status().isForbidden());

        verify(questionBankService, never()).getDistinctTopics();
    }
}

