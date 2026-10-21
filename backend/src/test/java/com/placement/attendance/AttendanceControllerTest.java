package com.placement.attendance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.placement.attendance.dto.CRTAttendanceRequest;
import com.placement.attendance.dto.ClassAttendanceRequest;
import com.placement.exception.GlobalExceptionHandler;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceController Tests")
class AttendanceControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private ClassAttendanceRepository classAttendanceRepository;

    @Mock
    private CRTAttendanceRepository crtAttendanceRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    private AttendanceService attendanceService;
    private AttendanceController attendanceController;

    private User sampleUser;
    private Student sampleStudent;
    private ClassAttendance sampleClassAttendance;
    private CRTAttendance sampleCrtAttendance;

    @BeforeEach
    void setUp() {
        attendanceService = new AttendanceService(
                classAttendanceRepository,
                crtAttendanceRepository,
                studentRepository,
                userRepository
        );
        attendanceController = new AttendanceController(attendanceService);

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
                .department("Computer Science")
                .course("B.Tech")
                .build();

        sampleClassAttendance = ClassAttendance.builder()
                .id(10L)
                .student(sampleStudent)
                .subject("Operating Systems")
                .attendanceDate(LocalDate.now())
                .present(true)
                .recordedBy("faculty@college.edu")
                .build();

        sampleCrtAttendance = CRTAttendance.builder()
                .id(20L)
                .student(sampleStudent)
                .sessionName("Mock Technical Interview")
                .sessionDate(LocalDate.now())
                .present(true)
                .recordedBy("tpo@college.edu")
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

        mockMvc = MockMvcBuilders.standaloneSetup(attendanceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authPrincipalResolver)
                .build();
    }

    @Test
    @DisplayName("POST /api/attendance/class - Should record class attendance and return 201 Created")
    void recordClassAttendance_success() throws Exception {
        ClassAttendanceRequest request = ClassAttendanceRequest.builder()
                .studentId(100L)
                .subject("Operating Systems")
                .attendanceDate(LocalDate.now())
                .present(true)
                .build();

        when(studentRepository.findById(100L)).thenReturn(Optional.of(sampleStudent));
        when(classAttendanceRepository.existsByStudentIdAndSubjectAndAttendanceDate(100L, "Operating Systems", LocalDate.now()))
                .thenReturn(false);
        when(classAttendanceRepository.save(any(ClassAttendance.class))).thenReturn(sampleClassAttendance);

        mockMvc.perform(post("/api/attendance/class")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.title").value("Operating Systems"))
                .andExpect(jsonPath("$.data.status").value("PRESENT"));
    }

    @Test
    @DisplayName("POST /api/attendance/class - Missing subject should return 400 Bad Request")
    void recordClassAttendance_missingSubject_returnsBadRequest() throws Exception {
        ClassAttendanceRequest request = ClassAttendanceRequest.builder()
                .studentId(100L)
                .attendanceDate(LocalDate.now())
                .present(true)
                .build();

        mockMvc.perform(post("/api/attendance/class")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.subject").exists());
    }

    @Test
    @DisplayName("POST /api/attendance/crt - Should record CRT attendance and return 201 Created")
    void recordCRTAttendance_success() throws Exception {
        CRTAttendanceRequest request = CRTAttendanceRequest.builder()
                .studentId(100L)
                .sessionName("Mock Technical Interview")
                .sessionDate(LocalDate.now())
                .present(true)
                .build();

        when(studentRepository.findById(100L)).thenReturn(Optional.of(sampleStudent));
        when(crtAttendanceRepository.existsByStudentIdAndSessionNameAndSessionDate(100L, "Mock Technical Interview", LocalDate.now()))
                .thenReturn(false);
        when(crtAttendanceRepository.save(any(CRTAttendance.class))).thenReturn(sampleCrtAttendance);

        mockMvc.perform(post("/api/attendance/crt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(20))
                .andExpect(jsonPath("$.data.title").value("Mock Technical Interview"));
    }

    @Test
    @DisplayName("GET /api/attendance/me/summary - Should return current student attendance summary")
    void getMyAttendanceSummary_success() throws Exception {
        when(studentRepository.findByUserEmail("aarav@college.edu")).thenReturn(Optional.of(sampleStudent));
        when(classAttendanceRepository.countByStudentId(100L)).thenReturn(20L);
        when(classAttendanceRepository.countByStudentIdAndPresentTrue(100L)).thenReturn(18L);
        when(crtAttendanceRepository.countByStudentId(100L)).thenReturn(10L);
        when(crtAttendanceRepository.countByStudentIdAndPresentTrue(100L)).thenReturn(9L);
        when(classAttendanceRepository.findDistinctSubjectsByStudentId(100L)).thenReturn(List.of());

        mockMvc.perform(get("/api/attendance/me/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalSessions").value(30))
                .andExpect(jsonPath("$.data.totalAttended").value(27))
                .andExpect(jsonPath("$.data.overallAttendancePercentage").value(90.0))
                .andExpect(jsonPath("$.data.eligibleForPlacements").value(true));
    }

    @Test
    @DisplayName("GET /api/attendance/student/{id}/summary - Unauthorized student should receive 403 Forbidden")
    void getStudentAttendanceSummary_unauthorized_returns403() throws Exception {
        User otherUser = User.builder()
                .id(2L)
                .email("aarav@college.edu") // current caller
                .role(UserRole.ROLE_STUDENT)
                .build();

        Student otherStudent = Student.builder()
                .id(200L)
                .user(User.builder().id(999L).build()) // different owner
                .build();

        when(studentRepository.findById(200L)).thenReturn(Optional.of(otherStudent));
        when(userRepository.findByEmail("aarav@college.edu")).thenReturn(Optional.of(otherUser));

        mockMvc.perform(get("/api/attendance/student/200/summary"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You do not have permission to access this resource"));
    }
}
