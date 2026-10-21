package com.placement.student;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.exception.GlobalExceptionHandler;
import com.placement.student.dto.StudentProfileRequest;
import com.placement.student.dto.StudentUpdateRequest;
import com.placement.user.User;
import com.placement.user.UserRepository;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentController Tests")
class StudentControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    private StudentService studentService;
    private StudentController studentController;

    private User sampleUser;
    private Student sampleStudent;
    private StudentProfileRequest validProfileRequest;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(studentRepository, userRepository);
        studentController = new StudentController(studentService);

        sampleUser = User.builder()
                .id(1L)
                .name("Aarav Sharma")
                .email("aarav@college.edu")
                .role(UserRole.ROLE_STUDENT)
                .build();

        sampleStudent = Student.builder()
                .id(100L)
                .user(sampleUser)
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

        UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(
                "aarav@college.edu",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
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

        mockMvc = MockMvcBuilders.standaloneSetup(studentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authPrincipalResolver)
                .build();
    }

    @Test
    @DisplayName("POST /api/students - Should create profile and return 201 Created")
    void createProfile_success() throws Exception {
        when(userRepository.findByEmail("aarav@college.edu")).thenReturn(Optional.of(sampleUser));
        when(studentRepository.existsByUserId(1L)).thenReturn(false);
        when(studentRepository.existsByRollNumber("CS2026-042")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(sampleStudent);

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validProfileRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.rollNumber").value("CS2026-042"))
                .andExpect(jsonPath("$.message").value("Student profile created successfully"));
    }

    @Test
    @DisplayName("POST /api/students - Invalid CGPA should return 400 Bad Request")
    void createProfile_invalidCgpa_returnsBadRequest() throws Exception {
        validProfileRequest.setCgpa(12.0); // Invalid CGPA > 10.0

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validProfileRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.cgpa").exists());
    }

    @Test
    @DisplayName("POST /api/students - Missing roll number should return 400 Bad Request")
    void createProfile_missingRollNumber_returnsBadRequest() throws Exception {
        validProfileRequest.setRollNumber("");

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validProfileRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.rollNumber").exists());
    }

    @Test
    @DisplayName("GET /api/students/me - Should return authenticated student profile")
    void getCurrentProfile_success() throws Exception {
        when(studentRepository.findByUserEmail("aarav@college.edu")).thenReturn(Optional.of(sampleStudent));

        mockMvc.perform(get("/api/students/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rollNumber").value("CS2026-042"))
                .andExpect(jsonPath("$.data.name").value("Aarav Sharma"));
    }

    @Test
    @DisplayName("PUT /api/students/me - Should update profile and return 200 OK")
    void updateCurrentProfile_success() throws Exception {
        when(studentRepository.findByUserEmail("aarav@college.edu")).thenReturn(Optional.of(sampleStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(sampleStudent);

        StudentUpdateRequest updateRequest = StudentUpdateRequest.builder()
                .cgpa(9.1)
                .currentSemester(7)
                .build();

        mockMvc.perform(put("/api/students/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/students/{id} - Should return student profile by ID")
    void getProfileById_success() throws Exception {
        when(studentRepository.findById(100L)).thenReturn(Optional.of(sampleStudent));
        when(userRepository.findByEmail("aarav@college.edu")).thenReturn(Optional.of(sampleUser));

        mockMvc.perform(get("/api/students/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100));
    }

    @Test
    @DisplayName("GET /api/students/{id} - Nonexistent student should return 404 Not Found")
    void getProfileById_notFound_returns404() throws Exception {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/students/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Student not found with id : '999'"));
    }

    @Test
    @DisplayName("GET /api/students/{id} - Unauthorized profile access should return 403 Forbidden")
    void getProfileById_unauthorized_returns403() throws Exception {
        User otherUser = User.builder()
                .id(2L)
                .name("Other Student")
                .email("aarav@college.edu") // current caller
                .role(UserRole.ROLE_STUDENT)
                .build();

        Student otherStudent = Student.builder()
                .id(200L)
                .user(User.builder().id(999L).email("owner@college.edu").build()) // different owner
                .build();

        when(studentRepository.findById(200L)).thenReturn(Optional.of(otherStudent));
        when(userRepository.findByEmail("aarav@college.edu")).thenReturn(Optional.of(otherUser));

        mockMvc.perform(get("/api/students/200"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You do not have permission to access this resource"));
    }

    @Test
    @DisplayName("GET /api/students - Should return student directory list")
    void getAllStudents_success() throws Exception {
        when(studentRepository.findAll()).thenReturn(List.of(sampleStudent));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].rollNumber").value("CS2026-042"));
    }
}
