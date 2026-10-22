package com.placement.placement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.placement.company.Company;
import com.placement.exception.GlobalExceptionHandler;
import com.placement.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PlacementControllerTest {

    private MockMvc mockMvc;
    private StubPlacementService placementService;
    private PlacementController placementController;
    private ObjectMapper objectMapper;

    private PlacementDrive sampleDrive;
    private Company sampleCompany;

    static class StubPlacementService extends PlacementService {
        PlacementDrive driveToReturn;
        List<PlacementDrive> listToReturn = new ArrayList<>();
        boolean throwNotFound = false;
        boolean deleteCalled = false;

        public StubPlacementService() {
            super(null, null);
        }

        @Override
        public PlacementDrive createPlacementDrive(PlacementDrive placementDrive) {
            return driveToReturn;
        }

        @Override
        public List<PlacementDrive> getAllPlacementDrives() {
            return listToReturn;
        }

        @Override
        public PlacementDrive getPlacementDriveById(Long id) {
            if (throwNotFound) {
                throw new ResourceNotFoundException("Placement drive not found with id: " + id);
            }
            return driveToReturn;
        }

        @Override
        public PlacementDrive updatePlacementDrive(Long id, PlacementDrive updatedDrive) {
            return driveToReturn;
        }

        @Override
        public void deletePlacementDrive(Long id) {
            deleteCalled = true;
        }

        @Override
        public List<PlacementDrive> getDrivesByCompany(Long companyId) {
            return listToReturn;
        }

        @Override
        public List<PlacementDrive> getDrivesByStatus(DriveStatus status) {
            return listToReturn;
        }

        @Override
        public List<PlacementDrive> searchDrivesByJobTitle(String jobTitle) {
            return listToReturn;
        }
    }

    @BeforeEach
    void setUp() {
        placementService = new StubPlacementService();
        placementController = new PlacementController(placementService);
        mockMvc = MockMvcBuilders.standaloneSetup(placementController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

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

        placementService.driveToReturn = sampleDrive;
        placementService.listToReturn = List.of(sampleDrive);
    }

    @Test
    @DisplayName("POST /api/placement-drives creates drive and returns 201 Created")
    void createPlacementDrive_Returns201() throws Exception {
        mockMvc.perform(post("/api/placement-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDrive)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.jobTitle").value("Software Engineer"))
                .andExpect(jsonPath("$.status").value("UPCOMING"));
    }

    @Test
    @DisplayName("GET /api/placement-drives returns list and 200 OK")
    void getAllPlacementDrives_Returns200() throws Exception {
        mockMvc.perform(get("/api/placement-drives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].jobTitle").value("Software Engineer"));
    }

    @Test
    @DisplayName("GET /api/placement-drives/{id} returns drive and 200 OK")
    void getPlacementDriveById_Returns200() throws Exception {
        mockMvc.perform(get("/api/placement-drives/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.jobTitle").value("Software Engineer"));
    }

    @Test
    @DisplayName("GET /api/placement-drives/{id} returns 404 when not found via GlobalExceptionHandler")
    void getPlacementDriveById_Returns404() throws Exception {
        placementService.throwNotFound = true;

        mockMvc.perform(get("/api/placement-drives/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Placement drive not found with id: 99"));
    }

    @Test
    @DisplayName("PUT /api/placement-drives/{id} updates drive and returns 200 OK")
    void updatePlacementDrive_Returns200() throws Exception {
        mockMvc.perform(put("/api/placement-drives/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDrive)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("DELETE /api/placement-drives/{id} returns 204 No Content")
    void deletePlacementDrive_Returns204() throws Exception {
        mockMvc.perform(delete("/api/placement-drives/10"))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertTrue(placementService.deleteCalled);
    }

    @Test
    @DisplayName("GET /api/placement-drives/company/{companyId} returns drives for company")
    void getDrivesByCompany_Returns200() throws Exception {
        mockMvc.perform(get("/api/placement-drives/company/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].company.name").value("Acme Corp"));
    }

    @Test
    @DisplayName("GET /api/placement-drives/status/{status} returns matching drives")
    void getDrivesByStatus_Returns200() throws Exception {
        mockMvc.perform(get("/api/placement-drives/status/UPCOMING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("UPCOMING"));
    }

    @Test
    @DisplayName("GET /api/placement-drives/search returns matched drives")
    void searchDrivesByJobTitle_Returns200() throws Exception {
        mockMvc.perform(get("/api/placement-drives/search").param("jobTitle", "Engineer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].jobTitle").value("Software Engineer"));
    }

    @Test
    @DisplayName("POST /api/placement-drives with valid eligibility criteria succeeds")
    void createPlacementDrive_WithEligibilityCriteria_Returns201() throws Exception {
        sampleDrive.setMinimumCgpa(7.5);
        sampleDrive.setMaximumAllowedBacklogs(1);
        sampleDrive.setEligibleBranches("CSE,IT");

        mockMvc.perform(post("/api/placement-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDrive)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/placement-drives with minimumCgpa > 10.0 returns 400 Bad Request")
    void createPlacementDrive_InvalidCgpa_Returns400() throws Exception {
        sampleDrive.setMinimumCgpa(11.0);

        mockMvc.perform(post("/api/placement-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDrive)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/placement-drives with negative backlogs returns 400 Bad Request")
    void createPlacementDrive_NegativeBacklogs_Returns400() throws Exception {
        sampleDrive.setMaximumAllowedBacklogs(-1);

        mockMvc.perform(post("/api/placement-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDrive)))
                .andExpect(status().isBadRequest());
    }
}
