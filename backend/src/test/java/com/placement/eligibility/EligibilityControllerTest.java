package com.placement.eligibility;

import com.placement.eligibility.dto.EligibilityCheckResponse;
import com.placement.exception.GlobalExceptionHandler;
import com.placement.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EligibilityController Tests")
class EligibilityControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EligibilityService eligibilityService;

    @InjectMocks
    private EligibilityController eligibilityController;

    private EligibilityCheckResponse sampleEligibleResponse;
    private EligibilityCheckResponse sampleIneligibleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eligibilityController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleEligibleResponse = EligibilityCheckResponse.builder()
                .studentId(1L)
                .driveId(10L)
                .eligible(true)
                .reasons(List.of())
                .studentCgpa(8.5)
                .minimumCgpa(7.0)
                .studentActiveBacklogs(0)
                .maximumAllowedBacklogs(0)
                .studentDepartment("CSE")
                .eligibleBranches("CSE, ECE, IT")
                .driveStatus("UPCOMING")
                .build();

        sampleIneligibleResponse = EligibilityCheckResponse.builder()
                .studentId(2L)
                .driveId(10L)
                .eligible(false)
                .reasons(List.of("Student CGPA (6.2) is below minimum required CGPA (7.0)."))
                .studentCgpa(6.2)
                .minimumCgpa(7.0)
                .studentActiveBacklogs(0)
                .maximumAllowedBacklogs(0)
                .studentDepartment("CSE")
                .eligibleBranches("CSE, ECE, IT")
                .driveStatus("UPCOMING")
                .build();
    }

    @Test
    @DisplayName("1. GET /api/eligibility/check - Valid request returns HTTP 200 with eligible result")
    void checkEligibility_validRequest_returns200() throws Exception {
        when(eligibilityService.checkEligibility(1L, 10L)).thenReturn(sampleEligibleResponse);

        mockMvc.perform(get("/api/eligibility/check")
                        .param("studentId", "1")
                        .param("driveId", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value(1))
                .andExpect(jsonPath("$.data.driveId").value(10))
                .andExpect(jsonPath("$.data.eligible").value(true))
                .andExpect(jsonPath("$.data.studentCgpa").value(8.5))
                .andExpect(jsonPath("$.data.minimumCgpa").value(7.0))
                .andExpect(jsonPath("$.data.studentDepartment").value("CSE"))
                .andExpect(jsonPath("$.data.eligibleBranches").value("CSE, ECE, IT"))
                .andExpect(jsonPath("$.data.driveStatus").value("UPCOMING"))
                .andExpect(jsonPath("$.data.reasons").isArray())
                .andExpect(jsonPath("$.data.reasons").isEmpty())
                .andExpect(jsonPath("$.message").value("Eligibility evaluated successfully"));

        verify(eligibilityService, times(1)).checkEligibility(1L, 10L);
    }

    @Test
    @DisplayName("GET /api/eligibility/check - Ineligible student returns HTTP 200 with reasons list")
    void checkEligibility_ineligibleStudent_returns200WithReasons() throws Exception {
        when(eligibilityService.checkEligibility(2L, 10L)).thenReturn(sampleIneligibleResponse);

        mockMvc.perform(get("/api/eligibility/check")
                        .param("studentId", "2")
                        .param("driveId", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value(2))
                .andExpect(jsonPath("$.data.driveId").value(10))
                .andExpect(jsonPath("$.data.eligible").value(false))
                .andExpect(jsonPath("$.data.reasons[0]").value("Student CGPA (6.2) is below minimum required CGPA (7.0)."));
    }

    @Test
    @DisplayName("2. GET /api/eligibility/check - Missing studentId returns HTTP 400 Bad Request")
    void checkEligibility_missingStudentId_returns400() throws Exception {
        mockMvc.perform(get("/api/eligibility/check")
                        .param("driveId", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(eligibilityService, never()).checkEligibility(any(), any());
    }

    @Test
    @DisplayName("3. GET /api/eligibility/check - Missing driveId returns HTTP 400 Bad Request")
    void checkEligibility_missingDriveId_returns400() throws Exception {
        mockMvc.perform(get("/api/eligibility/check")
                        .param("studentId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(eligibilityService, never()).checkEligibility(any(), any());
    }

    @Test
    @DisplayName("GET /api/eligibility/check - Missing both parameters returns HTTP 400 Bad Request")
    void checkEligibility_missingBothParams_returns400() throws Exception {
        mockMvc.perform(get("/api/eligibility/check")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(eligibilityService, never()).checkEligibility(any(), any());
    }

    @Test
    @DisplayName("4. GET /api/eligibility/check - Student not found returns HTTP 404 Not Found")
    void checkEligibility_studentNotFound_returns404() throws Exception {
        when(eligibilityService.checkEligibility(999L, 10L))
                .thenThrow(new ResourceNotFoundException("Student", "id", 999L));

        mockMvc.perform(get("/api/eligibility/check")
                        .param("studentId", "999")
                        .param("driveId", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Student not found with id : '999'"));
    }

    @Test
    @DisplayName("5. GET /api/eligibility/check - Drive not found returns HTTP 404 Not Found")
    void checkEligibility_driveNotFound_returns404() throws Exception {
        when(eligibilityService.checkEligibility(1L, 999L))
                .thenThrow(new ResourceNotFoundException("PlacementDrive", "id", 999L));

        mockMvc.perform(get("/api/eligibility/check")
                        .param("studentId", "1")
                        .param("driveId", "999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("PlacementDrive not found with id : '999'"));
    }
}
