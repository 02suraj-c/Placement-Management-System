package com.placement.student;

import com.placement.exception.ResourceNotFoundException;
import com.placement.student.dto.StudentProfileRequest;
import com.placement.student.dto.StudentProfileResponse;
import com.placement.student.dto.StudentUpdateRequest;
import com.placement.user.User;
import com.placement.user.UserRepository;
import com.placement.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService Unit Tests")
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StudentService studentService;

    private User sampleUser;
    private Student sampleStudent;
    private StudentProfileRequest profileRequest;

    @BeforeEach
    void setUp() {
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
                .skills("Java, Spring Boot, React")
                .address("New Delhi")
                .build();

        profileRequest = StudentProfileRequest.builder()
                .rollNumber("CS2026-042")
                .enrollmentNumber("EN2022001")
                .department("Computer Science")
                .course("B.Tech")
                .currentSemester(6)
                .cgpa(8.85)
                .graduationYear(2026)
                .phoneNumber("9876543210")
                .activeBacklogs(0)
                .skills("Java, Spring Boot, React")
                .address("New Delhi")
                .build();
    }

    @Test
    @DisplayName("Should successfully create student profile")
    void createProfile_success() {
        when(userRepository.findByEmail("aarav@college.edu")).thenReturn(Optional.of(sampleUser));
        when(studentRepository.existsByUserId(1L)).thenReturn(false);
        when(studentRepository.existsByRollNumber("CS2026-042")).thenReturn(false);
        when(studentRepository.existsByEnrollmentNumber("EN2022001")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(sampleStudent);

        StudentProfileResponse response = studentService.createProfile(profileRequest, "aarav@college.edu");

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("CS2026-042", response.getRollNumber());
        assertEquals("Aarav Sharma", response.getName());
        assertEquals("aarav@college.edu", response.getEmail());
        assertEquals(8.85, response.getCgpa());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    @DisplayName("Should reject profile creation if user already has a student profile")
    void createProfile_duplicateUser_throwsException() {
        when(userRepository.findByEmail("aarav@college.edu")).thenReturn(Optional.of(sampleUser));
        when(studentRepository.existsByUserId(1L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> studentService.createProfile(profileRequest, "aarav@college.edu"));

        assertTrue(ex.getMessage().contains("already exists"));
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    @DisplayName("Should reject profile creation if roll number is duplicate")
    void createProfile_duplicateRollNumber_throwsException() {
        when(userRepository.findByEmail("aarav@college.edu")).thenReturn(Optional.of(sampleUser));
        when(studentRepository.existsByUserId(1L)).thenReturn(false);
        when(studentRepository.existsByRollNumber("CS2026-042")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> studentService.createProfile(profileRequest, "aarav@college.edu"));

        assertTrue(ex.getMessage().contains("Roll number already registered"));
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    @DisplayName("Should reject profile creation with invalid CGPA (> 10.0)")
    void createProfile_invalidCgpa_throwsException() {
        profileRequest.setCgpa(11.5);
        when(userRepository.findByEmail("aarav@college.edu")).thenReturn(Optional.of(sampleUser));
        when(studentRepository.existsByUserId(1L)).thenReturn(false);
        when(studentRepository.existsByRollNumber("CS2026-042")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> studentService.createProfile(profileRequest, "aarav@college.edu"));

        assertTrue(ex.getMessage().contains("CGPA must be between 0.0 and 10.0"));
    }

    @Test
    @DisplayName("Should retrieve current authenticated student profile")
    void getCurrentStudentProfile_success() {
        when(studentRepository.findByUserEmail("aarav@college.edu")).thenReturn(Optional.of(sampleStudent));

        StudentProfileResponse response = studentService.getCurrentStudentProfile("aarav@college.edu");

        assertNotNull(response);
        assertEquals("CS2026-042", response.getRollNumber());
        assertEquals("Aarav Sharma", response.getName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for nonexistent student profile")
    void getCurrentStudentProfile_notFound_throwsException() {
        when(studentRepository.findByUserEmail("unknown@college.edu")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> studentService.getCurrentStudentProfile("unknown@college.edu"));
    }

    @Test
    @DisplayName("Should allow student to view their own profile by ID")
    void getProfileById_asOwner_success() {
        when(studentRepository.findById(100L)).thenReturn(Optional.of(sampleStudent));
        when(userRepository.findByEmail("aarav@college.edu")).thenReturn(Optional.of(sampleUser));

        StudentProfileResponse response = studentService.getProfileById(100L, "aarav@college.edu");

        assertNotNull(response);
        assertEquals(100L, response.getId());
    }

    @Test
    @DisplayName("Should prevent student from viewing another student's profile by ID")
    void getProfileById_asUnauthorizedStudent_throwsAccessDenied() {
        User otherStudentUser = User.builder()
                .id(2L)
                .name("Other Student")
                .email("other@college.edu")
                .role(UserRole.ROLE_STUDENT)
                .build();

        when(studentRepository.findById(100L)).thenReturn(Optional.of(sampleStudent));
        when(userRepository.findByEmail("other@college.edu")).thenReturn(Optional.of(otherStudentUser));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> studentService.getProfileById(100L, "other@college.edu"));

        assertTrue(ex.getMessage().contains("not authorized to view another student's profile"));
    }

    @Test
    @DisplayName("Should allow ADMIN or staff to view any student's profile by ID")
    void getProfileById_asAdmin_success() {
        User adminUser = User.builder()
                .id(99L)
                .name("Admin User")
                .email("admin@college.edu")
                .role(UserRole.ROLE_ADMIN)
                .build();

        when(studentRepository.findById(100L)).thenReturn(Optional.of(sampleStudent));
        when(userRepository.findByEmail("admin@college.edu")).thenReturn(Optional.of(adminUser));

        StudentProfileResponse response = studentService.getProfileById(100L, "admin@college.edu");

        assertNotNull(response);
        assertEquals(100L, response.getId());
    }

    @Test
    @DisplayName("Should update student profile fields successfully")
    void updateCurrentStudentProfile_success() {
        when(studentRepository.findByUserEmail("aarav@college.edu")).thenReturn(Optional.of(sampleStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(sampleStudent);

        StudentUpdateRequest updateRequest = StudentUpdateRequest.builder()
                .cgpa(9.2)
                .currentSemester(7)
                .skills("Java, Spring Boot, React, AWS")
                .build();

        StudentProfileResponse response = studentService.updateCurrentStudentProfile(updateRequest, "aarav@college.edu");

        assertNotNull(response);
        verify(studentRepository).save(sampleStudent);
        assertEquals(9.2, sampleStudent.getCgpa());
        assertEquals(7, sampleStudent.getCurrentSemester());
        assertEquals("Java, Spring Boot, React, AWS", sampleStudent.getSkills());
    }

    @Test
    @DisplayName("Should reject profile update if CGPA is negative")
    void updateCurrentStudentProfile_invalidCgpa_throwsException() {
        when(studentRepository.findByUserEmail("aarav@college.edu")).thenReturn(Optional.of(sampleStudent));

        StudentUpdateRequest updateRequest = StudentUpdateRequest.builder()
                .cgpa(-1.0)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> studentService.updateCurrentStudentProfile(updateRequest, "aarav@college.edu"));
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    @DisplayName("Should retrieve all student profiles for admin/staff")
    void getAllStudents_success() {
        when(studentRepository.findAll()).thenReturn(List.of(sampleStudent));

        List<StudentProfileResponse> result = studentService.getAllStudents();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CS2026-042", result.get(0).getRollNumber());
    }
}
