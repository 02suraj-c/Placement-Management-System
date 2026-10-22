package com.placement.resume;

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
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResumeController Tests")
class ResumeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @TempDir
    Path tempUploadDir;

    private ResumeService resumeService;
    private ResumeController resumeController;

    private User sampleUser;
    private Student sampleStudent;
    private StudentResume sampleResume;
    private File storedFile;

    @BeforeEach
    void setUp() throws Exception {
        resumeService = new ResumeService(resumeRepository, studentRepository, userRepository);
        ReflectionTestUtils.setField(resumeService, "uploadDir", tempUploadDir.toString());
        resumeService.init();

        resumeController = new ResumeController(resumeService);

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

        storedFile = tempUploadDir.resolve("sample-uuid-1234.pdf").toFile();
        try (FileOutputStream fos = new FileOutputStream(storedFile)) {
            fos.write("%PDF-1.4 Mock PDF Content".getBytes());
        }

        sampleResume = StudentResume.builder()
                .id(10L)
                .student(sampleStudent)
                .originalFileName("Aarav_Resume.pdf")
                .storedFileName("sample-uuid-1234.pdf")
                .contentType("application/pdf")
                .fileSize(1024L)
                .storagePath(storedFile.getAbsolutePath())
                .active(true)
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

        mockMvc = MockMvcBuilders.standaloneSetup(resumeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authPrincipalResolver)
                .build();
    }

    @Test
    @DisplayName("POST /api/resumes/upload - Should upload valid resume and return 201 Created")
    void uploadResume_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Aarav_Resume.pdf",
                "application/pdf",
                "%PDF-1.4 Mock PDF Content".getBytes()
        );

        when(studentRepository.findByUserEmail("aarav@college.edu")).thenReturn(Optional.of(sampleStudent));
        when(resumeRepository.findByStudentIdAndActiveTrue(100L)).thenReturn(Optional.empty());
        when(resumeRepository.save(any(StudentResume.class))).thenReturn(sampleResume);

        mockMvc.perform(multipart("/api/resumes/upload").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.originalFileName").value("Aarav_Resume.pdf"))
                .andExpect(jsonPath("$.message").value("Resume uploaded successfully"));
    }

    @Test
    @DisplayName("POST /api/resumes/upload - Should reject unsupported extension with 400 Bad Request")
    void uploadResume_invalidExtension_returnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "malicious.sh",
                "application/x-sh",
                "rm -rf /".getBytes()
        );

        when(studentRepository.findByUserEmail("aarav@college.edu")).thenReturn(Optional.of(sampleStudent));

        mockMvc.perform(multipart("/api/resumes/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Unsupported file extension")));
    }

    @Test
    @DisplayName("GET /api/resumes/me - Should return current student's active resume metadata")
    void getCurrentResume_success() throws Exception {
        when(resumeRepository.findByStudentUserEmailAndActiveTrue("aarav@college.edu"))
                .thenReturn(Optional.of(sampleResume));

        mockMvc.perform(get("/api/resumes/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.originalFileName").value("Aarav_Resume.pdf"));
    }

    @Test
    @DisplayName("GET /api/resumes/me/download - Should download student's active resume binary")
    void downloadCurrentResume_success() throws Exception {
        when(resumeRepository.findByStudentUserEmailAndActiveTrue("aarav@college.edu"))
                .thenReturn(Optional.of(sampleResume));
        when(resumeRepository.findById(10L)).thenReturn(Optional.of(sampleResume));
        when(userRepository.findByEmail("aarav@college.edu")).thenReturn(Optional.of(sampleUser));

        mockMvc.perform(get("/api/resumes/me/download"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Aarav_Resume.pdf\""))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    @DisplayName("GET /api/resumes/student/{id} - Should return resume metadata for authorized user")
    void getResumeByStudentId_success() throws Exception {
        when(studentRepository.findById(100L)).thenReturn(Optional.of(sampleStudent));
        when(userRepository.findByEmail("aarav@college.edu")).thenReturn(Optional.of(sampleUser));
        when(resumeRepository.findByStudentIdAndActiveTrue(100L)).thenReturn(Optional.of(sampleResume));

        mockMvc.perform(get("/api/resumes/student/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    @DisplayName("GET /api/resumes/download/{id} - Unauthorized student should receive 403 Forbidden")
    void downloadResumeById_unauthorized_returns403() throws Exception {
        User otherUser = User.builder()
                .id(2L)
                .email("aarav@college.edu") // current caller
                .role(UserRole.ROLE_STUDENT)
                .build();

        StudentResume otherResume = StudentResume.builder()
                .id(99L)
                .student(Student.builder().id(200L).user(User.builder().id(999L).build()).build()) // owned by 999L
                .build();

        when(resumeRepository.findById(99L)).thenReturn(Optional.of(otherResume));
        when(userRepository.findByEmail("aarav@college.edu")).thenReturn(Optional.of(otherUser));

        mockMvc.perform(get("/api/resumes/download/99"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You do not have permission to access this resource"));
    }
}
