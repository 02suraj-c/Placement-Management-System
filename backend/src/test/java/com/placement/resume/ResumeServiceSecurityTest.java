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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResumeService Security and Ownership Tests")
class ResumeServiceSecurityTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @TempDir
    Path tempUploadDir;

    private ResumeService resumeService;

    private User ownerUser;
    private Student ownerStudent;
    private StudentResume sampleResume;
    private Path sampleResumeFile;

    private User otherStudentUser;
    private Student otherStudent;

    private User adminUser;
    private User facultyUser;
    private User hodUser;
    private User officerUser;
    private User placementOfficerUser;

    @BeforeEach
    void setUp() throws IOException {
        resumeService = new ResumeService(resumeRepository, studentRepository, userRepository);
        ReflectionTestUtils.setField(resumeService, "uploadDir", tempUploadDir.toString());
        resumeService.init();

        ownerUser = User.builder()
                .id(1L)
                .name("Aarav Sharma")
                .email("student@college.edu")
                .role(UserRole.ROLE_STUDENT)
                .build();

        ownerStudent = Student.builder()
                .id(100L)
                .user(ownerUser)
                .rollNumber("CS2026-042")
                .department("Computer Science")
                .course("B.Tech")
                .build();

        sampleResumeFile = tempUploadDir.resolve("aarav-resume.pdf");
        Files.writeString(sampleResumeFile, "%PDF-1.4 Mock PDF Content");

        sampleResume = StudentResume.builder()
                .id(10L)
                .student(ownerStudent)
                .originalFileName("Aarav_Resume.pdf")
                .storedFileName("aarav-resume.pdf")
                .contentType("application/pdf")
                .fileSize(1024L)
                .storagePath(sampleResumeFile.toString())
                .active(true)
                .build();

        otherStudentUser = User.builder()
                .id(2L)
                .name("Other Student")
                .email("other@college.edu")
                .role(UserRole.ROLE_STUDENT)
                .build();

        otherStudent = Student.builder()
                .id(200L)
                .user(otherStudentUser)
                .rollNumber("CS2026-099")
                .department("Computer Science")
                .course("B.Tech")
                .build();

        adminUser = User.builder()
                .id(99L)
                .name("Admin User")
                .email("admin@college.edu")
                .role(UserRole.ROLE_ADMIN)
                .build();

        facultyUser = User.builder()
                .id(98L)
                .name("Faculty User")
                .email("faculty@college.edu")
                .role(UserRole.ROLE_FACULTY)
                .build();

        hodUser = User.builder()
                .id(97L)
                .name("HOD User")
                .email("hod@college.edu")
                .role(UserRole.ROLE_HOD)
                .build();

        officerUser = User.builder()
                .id(96L)
                .name("Officer User")
                .email("officer@college.edu")
                .role(UserRole.ROLE_OFFICER)
                .build();

        placementOfficerUser = User.builder()
                .id(95L)
                .name("Placement Officer")
                .email("po@college.edu")
                .role(UserRole.ROLE_PLACEMENT_OFFICER)
                .build();
    }

    // ==========================================
    // getResumeByStudentId() ownership tests
    // ==========================================

    @Test
    @DisplayName("getResumeByStudentId - Student accessing own resume metadata is permitted")
    void getResumeByStudentId_asOwner_success() {
        when(studentRepository.findById(100L)).thenReturn(Optional.of(ownerStudent));
        when(userRepository.findByEmail("student@college.edu")).thenReturn(Optional.of(ownerUser));
        when(resumeRepository.findByStudentIdAndActiveTrue(100L)).thenReturn(Optional.of(sampleResume));

        ResumeResponse response = resumeService.getResumeByStudentId(100L, "student@college.edu");

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Aarav_Resume.pdf", response.getOriginalFileName());
        assertEquals("CS2026-042", response.getRollNumber());
        verify(resumeRepository, times(1)).findByStudentIdAndActiveTrue(100L);
    }

    @Test
    @DisplayName("getResumeByStudentId - Student accessing another student's resume metadata throws AccessDeniedException")
    void getResumeByStudentId_asAnotherStudent_throwsAccessDenied() {
        when(studentRepository.findById(100L)).thenReturn(Optional.of(ownerStudent));
        when(userRepository.findByEmail("other@college.edu")).thenReturn(Optional.of(otherStudentUser));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> resumeService.getResumeByStudentId(100L, "other@college.edu"));

        assertTrue(ex.getMessage().contains("You are not authorized to access another student's resume"));
        verify(resumeRepository, never()).findByStudentIdAndActiveTrue(any());
    }

    @Test
    @DisplayName("getResumeByStudentId - ADMIN can access another student's resume metadata")
    void getResumeByStudentId_asAdmin_success() {
        when(studentRepository.findById(100L)).thenReturn(Optional.of(ownerStudent));
        when(userRepository.findByEmail("admin@college.edu")).thenReturn(Optional.of(adminUser));
        when(resumeRepository.findByStudentIdAndActiveTrue(100L)).thenReturn(Optional.of(sampleResume));

        ResumeResponse response = resumeService.getResumeByStudentId(100L, "admin@college.edu");

        assertNotNull(response);
        assertEquals(10L, response.getId());
    }

    @Test
    @DisplayName("getResumeByStudentId - FACULTY can access another student's resume metadata")
    void getResumeByStudentId_asFaculty_success() {
        when(studentRepository.findById(100L)).thenReturn(Optional.of(ownerStudent));
        when(userRepository.findByEmail("faculty@college.edu")).thenReturn(Optional.of(facultyUser));
        when(resumeRepository.findByStudentIdAndActiveTrue(100L)).thenReturn(Optional.of(sampleResume));

        ResumeResponse response = resumeService.getResumeByStudentId(100L, "faculty@college.edu");

        assertNotNull(response);
        assertEquals(10L, response.getId());
    }

    @Test
    @DisplayName("getResumeByStudentId - HOD can access another student's resume metadata")
    void getResumeByStudentId_asHod_success() {
        when(studentRepository.findById(100L)).thenReturn(Optional.of(ownerStudent));
        when(userRepository.findByEmail("hod@college.edu")).thenReturn(Optional.of(hodUser));
        when(resumeRepository.findByStudentIdAndActiveTrue(100L)).thenReturn(Optional.of(sampleResume));

        ResumeResponse response = resumeService.getResumeByStudentId(100L, "hod@college.edu");

        assertNotNull(response);
        assertEquals(10L, response.getId());
    }

    @Test
    @DisplayName("getResumeByStudentId - OFFICER can access another student's resume metadata")
    void getResumeByStudentId_asOfficer_success() {
        when(studentRepository.findById(100L)).thenReturn(Optional.of(ownerStudent));
        when(userRepository.findByEmail("officer@college.edu")).thenReturn(Optional.of(officerUser));
        when(resumeRepository.findByStudentIdAndActiveTrue(100L)).thenReturn(Optional.of(sampleResume));

        ResumeResponse response = resumeService.getResumeByStudentId(100L, "officer@college.edu");

        assertNotNull(response);
        assertEquals(10L, response.getId());
    }

    @Test
    @DisplayName("getResumeByStudentId - PLACEMENT_OFFICER can access another student's resume metadata")
    void getResumeByStudentId_asPlacementOfficer_success() {
        when(studentRepository.findById(100L)).thenReturn(Optional.of(ownerStudent));
        when(userRepository.findByEmail("po@college.edu")).thenReturn(Optional.of(placementOfficerUser));
        when(resumeRepository.findByStudentIdAndActiveTrue(100L)).thenReturn(Optional.of(sampleResume));

        ResumeResponse response = resumeService.getResumeByStudentId(100L, "po@college.edu");

        assertNotNull(response);
        assertEquals(10L, response.getId());
    }

    @Test
    @DisplayName("getResumeByStudentId - Nonexistent student throws ResourceNotFoundException")
    void getResumeByStudentId_studentNotFound_throwsException() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> resumeService.getResumeByStudentId(999L, "student@college.edu"));
    }

    @Test
    @DisplayName("getResumeByStudentId - Nonexistent caller user throws ResourceNotFoundException")
    void getResumeByStudentId_userNotFound_throwsException() {
        when(studentRepository.findById(100L)).thenReturn(Optional.of(ownerStudent));
        when(userRepository.findByEmail("unknown@college.edu")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> resumeService.getResumeByStudentId(100L, "unknown@college.edu"));
    }

    @Test
    @DisplayName("getResumeByStudentId - Student with no active resume throws ResourceNotFoundException")
    void getResumeByStudentId_activeResumeNotFound_throwsException() {
        when(studentRepository.findById(100L)).thenReturn(Optional.of(ownerStudent));
        when(userRepository.findByEmail("student@college.edu")).thenReturn(Optional.of(ownerUser));
        when(resumeRepository.findByStudentIdAndActiveTrue(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> resumeService.getResumeByStudentId(100L, "student@college.edu"));
    }

    // ==========================================
    // getResumeFile() ownership tests
    // ==========================================

    @Test
    @DisplayName("getResumeFile - Student downloading own resume file is permitted")
    void getResumeFile_asOwner_success() {
        when(resumeRepository.findById(10L)).thenReturn(Optional.of(sampleResume));
        when(userRepository.findByEmail("student@college.edu")).thenReturn(Optional.of(ownerUser));

        ResumeService.ResumeDownloadInfo downloadInfo = resumeService.getResumeFile(10L, "student@college.edu");

        assertNotNull(downloadInfo);
        assertEquals("Aarav_Resume.pdf", downloadInfo.originalFilename());
        assertEquals("application/pdf", downloadInfo.contentType());
        assertNotNull(downloadInfo.resource());
        assertTrue(downloadInfo.resource().exists());
    }

    @Test
    @DisplayName("getResumeFile - Student downloading another student's resume file throws AccessDeniedException")
    void getResumeFile_asAnotherStudent_throwsAccessDenied() {
        when(resumeRepository.findById(10L)).thenReturn(Optional.of(sampleResume));
        when(userRepository.findByEmail("other@college.edu")).thenReturn(Optional.of(otherStudentUser));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> resumeService.getResumeFile(10L, "other@college.edu"));

        assertTrue(ex.getMessage().contains("You are not authorized to download another student's resume"));
    }

    @Test
    @DisplayName("getResumeFile - ADMIN can download another student's resume file")
    void getResumeFile_asAdmin_success() {
        when(resumeRepository.findById(10L)).thenReturn(Optional.of(sampleResume));
        when(userRepository.findByEmail("admin@college.edu")).thenReturn(Optional.of(adminUser));

        ResumeService.ResumeDownloadInfo downloadInfo = resumeService.getResumeFile(10L, "admin@college.edu");

        assertNotNull(downloadInfo);
        assertEquals("Aarav_Resume.pdf", downloadInfo.originalFilename());
        assertTrue(downloadInfo.resource().exists());
    }

    @Test
    @DisplayName("getResumeFile - FACULTY can download another student's resume file")
    void getResumeFile_asFaculty_success() {
        when(resumeRepository.findById(10L)).thenReturn(Optional.of(sampleResume));
        when(userRepository.findByEmail("faculty@college.edu")).thenReturn(Optional.of(facultyUser));

        ResumeService.ResumeDownloadInfo downloadInfo = resumeService.getResumeFile(10L, "faculty@college.edu");

        assertNotNull(downloadInfo);
        assertEquals("Aarav_Resume.pdf", downloadInfo.originalFilename());
        assertTrue(downloadInfo.resource().exists());
    }

    @Test
    @DisplayName("getResumeFile - HOD can download another student's resume file")
    void getResumeFile_asHod_success() {
        when(resumeRepository.findById(10L)).thenReturn(Optional.of(sampleResume));
        when(userRepository.findByEmail("hod@college.edu")).thenReturn(Optional.of(hodUser));

        ResumeService.ResumeDownloadInfo downloadInfo = resumeService.getResumeFile(10L, "hod@college.edu");

        assertNotNull(downloadInfo);
        assertEquals("Aarav_Resume.pdf", downloadInfo.originalFilename());
        assertTrue(downloadInfo.resource().exists());
    }

    @Test
    @DisplayName("getResumeFile - OFFICER can download another student's resume file")
    void getResumeFile_asOfficer_success() {
        when(resumeRepository.findById(10L)).thenReturn(Optional.of(sampleResume));
        when(userRepository.findByEmail("officer@college.edu")).thenReturn(Optional.of(officerUser));

        ResumeService.ResumeDownloadInfo downloadInfo = resumeService.getResumeFile(10L, "officer@college.edu");

        assertNotNull(downloadInfo);
        assertEquals("Aarav_Resume.pdf", downloadInfo.originalFilename());
        assertTrue(downloadInfo.resource().exists());
    }

    @Test
    @DisplayName("getResumeFile - PLACEMENT_OFFICER can download another student's resume file")
    void getResumeFile_asPlacementOfficer_success() {
        when(resumeRepository.findById(10L)).thenReturn(Optional.of(sampleResume));
        when(userRepository.findByEmail("po@college.edu")).thenReturn(Optional.of(placementOfficerUser));

        ResumeService.ResumeDownloadInfo downloadInfo = resumeService.getResumeFile(10L, "po@college.edu");

        assertNotNull(downloadInfo);
        assertEquals("Aarav_Resume.pdf", downloadInfo.originalFilename());
        assertTrue(downloadInfo.resource().exists());
    }

    @Test
    @DisplayName("getResumeFile - Nonexistent resume throws ResourceNotFoundException")
    void getResumeFile_resumeNotFound_throwsException() {
        when(resumeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> resumeService.getResumeFile(999L, "student@college.edu"));
    }

    @Test
    @DisplayName("getResumeFile - Nonexistent caller user throws ResourceNotFoundException")
    void getResumeFile_userNotFound_throwsException() {
        when(resumeRepository.findById(10L)).thenReturn(Optional.of(sampleResume));
        when(userRepository.findByEmail("unknown@college.edu")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> resumeService.getResumeFile(10L, "unknown@college.edu"));
    }

    @Test
    @DisplayName("getResumeFile - Resume file missing on disk throws ResourceNotFoundException")
    void getResumeFile_fileMissingOnDisk_throwsException() {
        StudentResume missingFileResume = StudentResume.builder()
                .id(11L)
                .student(ownerStudent)
                .originalFileName("Missing.pdf")
                .storagePath(tempUploadDir.resolve("missing-uuid.pdf").toString())
                .build();

        when(resumeRepository.findById(11L)).thenReturn(Optional.of(missingFileResume));
        when(userRepository.findByEmail("student@college.edu")).thenReturn(Optional.of(ownerUser));

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> resumeService.getResumeFile(11L, "student@college.edu"));

        assertTrue(ex.getMessage().contains("not found on disk"));
    }
}

