package com.placement.resume;

import com.placement.exception.ResourceNotFoundException;
import com.placement.resume.dto.ResumeResponse;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResumeService Unit Tests")
class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @TempDir
    Path tempUploadDir;

    private ResumeService resumeService;
    private User sampleUser;
    private Student sampleStudent;
    private StudentResume sampleResume;

    @BeforeEach
    void setUp() {
        resumeService = new ResumeService(resumeRepository, studentRepository, userRepository);
        ReflectionTestUtils.setField(resumeService, "uploadDir", tempUploadDir.toString());
        resumeService.init();

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

        sampleResume = StudentResume.builder()
                .id(10L)
                .student(sampleStudent)
                .originalFileName("Aarav_Resume.pdf")
                .storedFileName("sample-uuid-1234.pdf")
                .contentType("application/pdf")
                .fileSize(1024L)
                .storagePath(tempUploadDir.resolve("sample-uuid-1234.pdf").toString())
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should successfully upload a valid PDF resume")
    void uploadResume_validPdf_success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Aarav_Resume.pdf",
                "application/pdf",
                "%PDF-1.4 Mock PDF Content".getBytes()
        );

        when(studentRepository.findByUserEmail("aarav@college.edu")).thenReturn(Optional.of(sampleStudent));
        when(resumeRepository.findByStudentIdAndActiveTrue(100L)).thenReturn(Optional.empty());
        when(resumeRepository.save(any(StudentResume.class))).thenReturn(sampleResume);

        ResumeResponse response = resumeService.uploadResume(file, "aarav@college.edu");

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Aarav_Resume.pdf", response.getOriginalFileName());
        assertEquals("CS2026-042", response.getRollNumber());
        assertTrue(response.isActive());
        verify(resumeRepository, times(1)).save(any(StudentResume.class));
    }

    @Test
    @DisplayName("Should deactivate previous active resume when uploading a new one")
    void uploadResume_replaceExisting_success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Aarav_Resume_v2.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "Mock DOCX Content".getBytes()
        );

        StudentResume existingResume = StudentResume.builder()
                .id(9L)
                .student(sampleStudent)
                .originalFileName("Old_Resume.pdf")
                .active(true)
                .build();

        when(studentRepository.findByUserEmail("aarav@college.edu")).thenReturn(Optional.of(sampleStudent));
        when(resumeRepository.findByStudentIdAndActiveTrue(100L)).thenReturn(Optional.of(existingResume));
        when(resumeRepository.save(any(StudentResume.class))).thenReturn(sampleResume);

        ResumeResponse response = resumeService.uploadResume(file, "aarav@college.edu");

        assertNotNull(response);
        assertFalse(existingResume.isActive());
        verify(resumeRepository, times(2)).save(any(StudentResume.class));
    }

    @Test
    @DisplayName("Should reject empty resume file")
    void uploadResume_emptyFile_throwsException() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        when(studentRepository.findByUserEmail("aarav@college.edu")).thenReturn(Optional.of(sampleStudent));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> resumeService.uploadResume(emptyFile, "aarav@college.edu"));

        assertTrue(ex.getMessage().contains("cannot be empty"));
        verify(resumeRepository, never()).save(any(StudentResume.class));
    }

    @Test
    @DisplayName("Should reject unsupported file extensions (e.g. .exe or .png)")
    void uploadResume_invalidExtension_throwsException() {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file", "script.sh", "application/x-sh", "echo hello".getBytes()
        );

        when(studentRepository.findByUserEmail("aarav@college.edu")).thenReturn(Optional.of(sampleStudent));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> resumeService.uploadResume(invalidFile, "aarav@college.edu"));

        assertTrue(ex.getMessage().contains("Unsupported file extension"));
        verify(resumeRepository, never()).save(any(StudentResume.class));
    }

    @Test
    @DisplayName("Should reject files exceeding 5MB limit")
    void uploadResume_oversizedFile_throwsException() {
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile oversizedFile = new MockMultipartFile(
                "file", "large.pdf", "application/pdf", largeContent
        );

        when(studentRepository.findByUserEmail("aarav@college.edu")).thenReturn(Optional.of(sampleStudent));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> resumeService.uploadResume(oversizedFile, "aarav@college.edu"));

        assertTrue(ex.getMessage().contains("exceeds maximum limit of 5MB"));
        verify(resumeRepository, never()).save(any(StudentResume.class));
    }

    @Test
    @DisplayName("Should retrieve active resume for authenticated student")
    void getCurrentStudentResume_success() {
        when(resumeRepository.findByStudentUserEmailAndActiveTrue("aarav@college.edu"))
                .thenReturn(Optional.of(sampleResume));

        ResumeResponse response = resumeService.getCurrentStudentResume("aarav@college.edu");

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Aarav_Resume.pdf", response.getOriginalFileName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when no active resume exists")
    void getCurrentStudentResume_notFound_throwsException() {
        when(resumeRepository.findByStudentUserEmailAndActiveTrue("aarav@college.edu"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> resumeService.getCurrentStudentResume("aarav@college.edu"));
    }

    @Test
    @DisplayName("Should allow owner to view resume metadata by student ID")
    void getResumeByStudentId_asOwner_success() {
        when(studentRepository.findById(100L)).thenReturn(Optional.of(sampleStudent));
        when(userRepository.findByEmail("aarav@college.edu")).thenReturn(Optional.of(sampleUser));
        when(resumeRepository.findByStudentIdAndActiveTrue(100L)).thenReturn(Optional.of(sampleResume));

        ResumeResponse response = resumeService.getResumeByStudentId(100L, "aarav@college.edu");

        assertNotNull(response);
        assertEquals(10L, response.getId());
    }

    @Test
    @DisplayName("Should prevent student from viewing another student's resume by student ID")
    void getResumeByStudentId_unauthorizedStudent_throwsAccessDenied() {
        User otherUser = User.builder()
                .id(2L)
                .name("Other Student")
                .email("other@college.edu")
                .role(UserRole.ROLE_STUDENT)
                .build();

        when(studentRepository.findById(100L)).thenReturn(Optional.of(sampleStudent));
        when(userRepository.findByEmail("other@college.edu")).thenReturn(Optional.of(otherUser));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> resumeService.getResumeByStudentId(100L, "other@college.edu"));

        assertTrue(ex.getMessage().contains("not authorized"));
    }

    @Test
    @DisplayName("Should allow ADMIN to view any student's resume metadata")
    void getResumeByStudentId_asAdmin_success() {
        User adminUser = User.builder()
                .id(99L)
                .name("Admin User")
                .email("admin@college.edu")
                .role(UserRole.ROLE_ADMIN)
                .build();

        when(studentRepository.findById(100L)).thenReturn(Optional.of(sampleStudent));
        when(userRepository.findByEmail("admin@college.edu")).thenReturn(Optional.of(adminUser));
        when(resumeRepository.findByStudentIdAndActiveTrue(100L)).thenReturn(Optional.of(sampleResume));

        ResumeResponse response = resumeService.getResumeByStudentId(100L, "admin@college.edu");

        assertNotNull(response);
        assertEquals(10L, response.getId());
    }

    @Test
    @DisplayName("Should prevent student from downloading another student's resume file")
    void getResumeFile_unauthorizedStudent_throwsAccessDenied() {
        User otherUser = User.builder()
                .id(2L)
                .email("other@college.edu")
                .role(UserRole.ROLE_STUDENT)
                .build();

        when(resumeRepository.findById(10L)).thenReturn(Optional.of(sampleResume));
        when(userRepository.findByEmail("other@college.edu")).thenReturn(Optional.of(otherUser));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> resumeService.getResumeFile(10L, "other@college.edu"));

        assertTrue(ex.getMessage().contains("not authorized to download another student's resume"));
    }
}
