package com.placement.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.placement.exception.GlobalExceptionHandler;
import com.placement.internship.dto.InternshipRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("InternshipController Tests")
class InternshipControllerTest {

    @Mock
    private InternshipRepository internshipRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Internship sampleInternship;
    private InternshipRequest sampleRequest;

    @BeforeEach
    void setUp() {
        InternshipService internshipService = new InternshipService(internshipRepository);
        InternshipController controller = new InternshipController(internshipService);

        UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(
                "tpo@college.edu",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_PLACEMENT_OFFICER"))
        );

        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                return mockUserDetails;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(authPrincipalResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        sampleInternship = Internship.builder()
                .id(1L)
                .title("Software Engineer Intern")
                .company("Google")
                .description("Work on scalable cloud systems and APIs")
                .requiredSkills("Java, Spring Boot, MySQL")
                .location("Bangalore / Remote")
                .stipend("₹45,000 / month")
                .duration("6 Months")
                .eligibilityCriteria("B.Tech CSE/IT, CGPA >= 8.0")
                .applicationDeadline(LocalDate.now().plusDays(30))
                .applicationUrl("https://careers.google.com/jobs/123")
                .active(true)
                .postedBy("tpo@college.edu")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleRequest = InternshipRequest.builder()
                .title("Software Engineer Intern")
                .company("Google")
                .description("Work on scalable cloud systems and APIs")
                .requiredSkills("Java, Spring Boot, MySQL")
                .location("Bangalore / Remote")
                .stipend("₹45,000 / month")
                .duration("6 Months")
                .eligibilityCriteria("B.Tech CSE/IT, CGPA >= 8.0")
                .applicationDeadline(LocalDate.now().plusDays(30))
                .applicationUrl("https://careers.google.com/jobs/123")
                .active(true)
                .build();
    }

    @Test
    @DisplayName("POST /api/internships: returns 201 Created when valid body is submitted")
    void createInternship_validRequest_returns201() throws Exception {
        when(internshipRepository.save(any(Internship.class))).thenAnswer(invocation -> {
            Internship toSave = invocation.getArgument(0);
            toSave.setId(10L);
            return toSave;
        });

        mockMvc.perform(post("/api/internships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(10)))
                .andExpect(jsonPath("$.data.title", is("Software Engineer Intern")))
                .andExpect(jsonPath("$.data.company", is("Google")));
    }

    @Test
    @DisplayName("POST /api/internships: returns 400 Bad Request when required fields are missing")
    void createInternship_invalidRequest_returns400() throws Exception {
        InternshipRequest invalidRequest = InternshipRequest.builder()
                .title("") // Blank title
                .company("Google")
                .build();

        mockMvc.perform(post("/api/internships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Validation failed")));
    }

    @Test
    @DisplayName("GET /api/internships: returns 200 OK with list of active internships")
    void getActiveInternships_returns200() throws Exception {
        when(internshipRepository.findByActiveTrueOrderByCreatedAtDesc())
                .thenReturn(List.of(sampleInternship));

        mockMvc.perform(get("/api/internships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].company", is("Google")));
    }

    @Test
    @DisplayName("GET /api/internships/all: returns 200 OK with all internships for staff")
    void getAllInternships_returns200() throws Exception {
        when(internshipRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(sampleInternship));

        mockMvc.perform(get("/api/internships/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/internships/{id}: returns 200 OK when found")
    void getInternshipById_found_returns200() throws Exception {
        when(internshipRepository.findById(1L)).thenReturn(Optional.of(sampleInternship));

        mockMvc.perform(get("/api/internships/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.company", is("Google")));
    }

    @Test
    @DisplayName("GET /api/internships/{id}: returns 404 Not Found when ID does not exist")
    void getInternshipById_notFound_returns404() throws Exception {
        when(internshipRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/internships/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("PUT /api/internships/{id}: returns 200 OK on successful update")
    void updateInternship_validRequest_returns200() throws Exception {
        when(internshipRepository.findById(1L)).thenReturn(Optional.of(sampleInternship));
        when(internshipRepository.save(any(Internship.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/internships/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.title", is("Software Engineer Intern")));
    }

    @Test
    @DisplayName("DELETE /api/internships/{id}: returns 200 OK on successful deletion")
    void deleteInternship_validId_returns200() throws Exception {
        when(internshipRepository.findById(1L)).thenReturn(Optional.of(sampleInternship));

        mockMvc.perform(delete("/api/internships/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Internship listing deleted successfully")));

        verify(internshipRepository, times(1)).delete(sampleInternship);
    }
}
