package com.placement.hackathon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.placement.exception.GlobalExceptionHandler;
import com.placement.hackathon.dto.HackathonRequest;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
import com.placement.user.User;
import com.placement.user.UserRole;
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
@DisplayName("HackathonController Tests")
class HackathonControllerTest {

    @Mock
    private HackathonRepository hackathonRepository;

    @Mock
    private HackathonRegistrationRepository hackathonRegistrationRepository;

    @Mock
    private StudentRepository studentRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Hackathon sampleHackathon;
    private HackathonRequest sampleRequest;
    private Student sampleStudent;

    @BeforeEach
    void setUp() {
        HackathonService hackathonService = new HackathonService(
                hackathonRepository,
                hackathonRegistrationRepository,
                studentRepository
        );
        HackathonController controller = new HackathonController(hackathonService);

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

        LocalDate today = LocalDate.now();

        sampleHackathon = Hackathon.builder()
                .id(1L)
                .title("Smart India Codefest 2026")
                .organizer("Ministry of Education & AICTE")
                .description("National level hackathon for AI and IoT innovation")
                .startDate(today.plusDays(20))
                .endDate(today.plusDays(22))
                .registrationDeadline(today.plusDays(10))
                .mode(HackathonMode.HYBRID)
                .location("Campus Auditorium & Online")
                .eligibilityCriteria("Open to all engineering students")
                .registrationUrl("https://codefest.aicte.gov.in")
                .status(HackathonStatus.UPCOMING)
                .active(true)
                .postedBy("tpo@college.edu")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleRequest = HackathonRequest.builder()
                .title("Smart India Codefest 2026")
                .organizer("Ministry of Education & AICTE")
                .description("National level hackathon for AI and IoT innovation")
                .startDate(today.plusDays(20))
                .endDate(today.plusDays(22))
                .registrationDeadline(today.plusDays(10))
                .mode(HackathonMode.HYBRID)
                .location("Campus Auditorium & Online")
                .eligibilityCriteria("Open to all engineering students")
                .registrationUrl("https://codefest.aicte.gov.in")
                .status(HackathonStatus.UPCOMING)
                .active(true)
                .build();

        User sampleUser = User.builder()
                .id(1L)
                .name("Aarav Sharma")
                .email("tpo@college.edu")
                .role(UserRole.ROLE_STUDENT)
                .build();

        sampleStudent = Student.builder()
                .id(100L)
                .user(sampleUser)
                .rollNumber("CS2026-042")
                .department("Computer Science")
                .course("B.Tech")
                .build();
    }

    @Test
    @DisplayName("POST /api/hackathons: returns 201 Created on valid request")
    void createHackathon_validRequest_returns201() throws Exception {
        when(hackathonRepository.save(any(Hackathon.class))).thenAnswer(invocation -> {
            Hackathon toSave = invocation.getArgument(0);
            toSave.setId(10L);
            return toSave;
        });

        mockMvc.perform(post("/api/hackathons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(10)))
                .andExpect(jsonPath("$.data.title", is("Smart India Codefest 2026")))
                .andExpect(jsonPath("$.data.mode", is("HYBRID")));
    }

    @Test
    @DisplayName("POST /api/hackathons: returns 400 Bad Request when mandatory field is missing")
    void createHackathon_invalidRequest_returns400() throws Exception {
        HackathonRequest invalid = HackathonRequest.builder()
                .title("") // Blank title
                .organizer("AICTE")
                .build();

        mockMvc.perform(post("/api/hackathons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Validation failed")));
    }

    @Test
    @DisplayName("GET /api/hackathons: returns 200 OK with active hackathons")
    void getActiveHackathons_returns200() throws Exception {
        when(hackathonRepository.findByActiveTrueOrderByStartDateAsc())
                .thenReturn(List.of(sampleHackathon));

        mockMvc.perform(get("/api/hackathons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title", is("Smart India Codefest 2026")));
    }

    @Test
    @DisplayName("GET /api/hackathons/all: returns 200 OK with all listings for staff")
    void getAllHackathons_returns200() throws Exception {
        when(hackathonRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(sampleHackathon));

        mockMvc.perform(get("/api/hackathons/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/hackathons/{id}: returns 200 OK when found")
    void getHackathonById_found_returns200() throws Exception {
        when(hackathonRepository.findById(1L)).thenReturn(Optional.of(sampleHackathon));

        mockMvc.perform(get("/api/hackathons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.organizer", is("Ministry of Education & AICTE")));
    }

    @Test
    @DisplayName("GET /api/hackathons/{id}: returns 404 Not Found when ID does not exist")
    void getHackathonById_notFound_returns404() throws Exception {
        when(hackathonRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/hackathons/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("PUT /api/hackathons/{id}: returns 200 OK on successful update")
    void updateHackathon_validRequest_returns200() throws Exception {
        when(hackathonRepository.findById(1L)).thenReturn(Optional.of(sampleHackathon));
        when(hackathonRepository.save(any(Hackathon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/hackathons/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.title", is("Smart India Codefest 2026")));
    }

    @Test
    @DisplayName("DELETE /api/hackathons/{id}: returns 200 OK on successful deletion")
    void deleteHackathon_validId_returns200() throws Exception {
        when(hackathonRepository.findById(1L)).thenReturn(Optional.of(sampleHackathon));

        mockMvc.perform(delete("/api/hackathons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Hackathon deleted successfully")));

        verify(hackathonRepository, times(1)).delete(sampleHackathon);
    }

    @Test
    @DisplayName("POST /api/hackathons/{id}/register: returns 201 Created on student registration")
    void registerStudent_openHackathon_returns201() throws Exception {
        when(hackathonRepository.findById(1L)).thenReturn(Optional.of(sampleHackathon));
        when(studentRepository.findByUserEmail("tpo@college.edu")).thenReturn(Optional.of(sampleStudent));
        when(hackathonRegistrationRepository.existsByHackathonIdAndStudentId(1L, 100L)).thenReturn(false);

        HackathonRegistration registration = HackathonRegistration.builder()
                .id(25L)
                .hackathon(sampleHackathon)
                .student(sampleStudent)
                .registeredAt(LocalDateTime.now())
                .build();

        when(hackathonRegistrationRepository.save(any(HackathonRegistration.class))).thenReturn(registration);

        mockMvc.perform(post("/api/hackathons/1/register"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(25)))
                .andExpect(jsonPath("$.data.hackathonTitle", is("Smart India Codefest 2026")));
    }

    @Test
    @DisplayName("GET /api/hackathons/me/registrations: returns 200 OK with student registrations")
    void getMyRegistrations_returns200() throws Exception {
        when(studentRepository.findByUserEmail("tpo@college.edu")).thenReturn(Optional.of(sampleStudent));

        HackathonRegistration registration = HackathonRegistration.builder()
                .id(25L)
                .hackathon(sampleHackathon)
                .student(sampleStudent)
                .registeredAt(LocalDateTime.now())
                .build();

        when(hackathonRegistrationRepository.findByStudentIdOrderByRegisteredAtDesc(100L))
                .thenReturn(List.of(registration));

        mockMvc.perform(get("/api/hackathons/me/registrations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].hackathonTitle", is("Smart India Codefest 2026")));
    }
}
