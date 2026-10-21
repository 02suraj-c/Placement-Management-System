package com.placement.attendance;

import com.placement.attendance.dto.AttendanceRecordResponse;
import com.placement.attendance.dto.AttendanceSummaryResponse;
import com.placement.attendance.dto.CRTAttendanceRequest;
import com.placement.attendance.dto.ClassAttendanceRequest;
import com.placement.exception.ResourceNotFoundException;
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
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceService Unit Tests")
class AttendanceServiceTest {

    @Mock
    private ClassAttendanceRepository classAttendanceRepository;

    @Mock
    private CRTAttendanceRepository crtAttendanceRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    private AttendanceService attendanceService;
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
                .subject("Data Structures")
                .attendanceDate(LocalDate.now())
                .present(true)
                .remarks("Active participation")
                .recordedBy("faculty@college.edu")
                .build();

        sampleCrtAttendance = CRTAttendance.builder()
                .id(20L)
                .student(sampleStudent)
                .sessionName("Quantitative Aptitude Module 1")
                .sessionDate(LocalDate.now())
                .present(true)
                .trainerName("Mr. Sharma")
                .recordedBy("tpo@college.edu")
                .build();
    }

    @Test
    @DisplayName("Should successfully record class attendance by staff")
    void recordClassAttendance_success() {
        ClassAttendanceRequest request = ClassAttendanceRequest.builder()
                .studentId(100L)
                .subject("Data Structures")
                .attendanceDate(LocalDate.now())
                .present(true)
                .remarks("Active participation")
                .build();

        when(studentRepository.findById(100L)).thenReturn(Optional.of(sampleStudent));
        when(classAttendanceRepository.existsByStudentIdAndSubjectAndAttendanceDate(100L, "Data Structures", LocalDate.now()))
                .thenReturn(false);
        when(classAttendanceRepository.save(any(ClassAttendance.class))).thenReturn(sampleClassAttendance);

        AttendanceRecordResponse response = attendanceService.recordClassAttendance(request, "faculty@college.edu");

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("CLASS", response.getType());
        assertEquals("Data Structures", response.getTitle());
        assertTrue(response.isPresent());
        assertEquals("PRESENT", response.getStatus());
        assertEquals("faculty@college.edu", response.getRecordedBy());
        verify(classAttendanceRepository).save(any(ClassAttendance.class));
    }

    @Test
    @DisplayName("Should reject duplicate class attendance on the same date and subject")
    void recordClassAttendance_duplicate_throwsException() {
        ClassAttendanceRequest request = ClassAttendanceRequest.builder()
                .studentId(100L)
                .subject("Data Structures")
                .attendanceDate(LocalDate.now())
                .present(true)
                .build();

        when(studentRepository.findById(100L)).thenReturn(Optional.of(sampleStudent));
        when(classAttendanceRepository.existsByStudentIdAndSubjectAndAttendanceDate(100L, "Data Structures", LocalDate.now()))
                .thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> attendanceService.recordClassAttendance(request, "faculty@college.edu"));

        assertTrue(ex.getMessage().contains("already recorded"));
        verify(classAttendanceRepository, never()).save(any(ClassAttendance.class));
    }

    @Test
    @DisplayName("Should successfully record CRT attendance by staff")
    void recordCRTAttendance_success() {
        CRTAttendanceRequest request = CRTAttendanceRequest.builder()
                .studentId(100L)
                .sessionName("Quantitative Aptitude Module 1")
                .sessionDate(LocalDate.now())
                .present(true)
                .trainerName("Mr. Sharma")
                .build();

        when(studentRepository.findById(100L)).thenReturn(Optional.of(sampleStudent));
        when(crtAttendanceRepository.existsByStudentIdAndSessionNameAndSessionDate(100L, "Quantitative Aptitude Module 1", LocalDate.now()))
                .thenReturn(false);
        when(crtAttendanceRepository.save(any(CRTAttendance.class))).thenReturn(sampleCrtAttendance);

        AttendanceRecordResponse response = attendanceService.recordCRTAttendance(request, "tpo@college.edu");

        assertNotNull(response);
        assertEquals(20L, response.getId());
        assertEquals("CRT", response.getType());
        assertEquals("Quantitative Aptitude Module 1", response.getTitle());
        assertTrue(response.isPresent());
        assertEquals("Mr. Sharma", response.getTrainerName());
        verify(crtAttendanceRepository).save(any(CRTAttendance.class));
    }

    @Test
    @DisplayName("Should reject duplicate CRT attendance on the same date and session")
    void recordCRTAttendance_duplicate_throwsException() {
        CRTAttendanceRequest request = CRTAttendanceRequest.builder()
                .studentId(100L)
                .sessionName("Quantitative Aptitude Module 1")
                .sessionDate(LocalDate.now())
                .present(true)
                .build();

        when(studentRepository.findById(100L)).thenReturn(Optional.of(sampleStudent));
        when(crtAttendanceRepository.existsByStudentIdAndSessionNameAndSessionDate(100L, "Quantitative Aptitude Module 1", LocalDate.now()))
                .thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> attendanceService.recordCRTAttendance(request, "tpo@college.edu"));

        assertTrue(ex.getMessage().contains("already recorded"));
        verify(crtAttendanceRepository, never()).save(any(CRTAttendance.class));
    }

    @Test
    @DisplayName("Should allow student to view their own class attendance history")
    void getStudentClassAttendance_asOwner_success() {
        when(studentRepository.findById(100L)).thenReturn(Optional.of(sampleStudent));
        when(userRepository.findByEmail("aarav@college.edu")).thenReturn(Optional.of(sampleUser));
        when(classAttendanceRepository.findByStudentIdOrderByAttendanceDateDesc(100L))
                .thenReturn(List.of(sampleClassAttendance));

        List<AttendanceRecordResponse> records = attendanceService.getStudentClassAttendance(100L, "aarav@college.edu");

        assertNotNull(records);
        assertEquals(1, records.size());
        assertEquals("Data Structures", records.get(0).getTitle());
    }

    @Test
    @DisplayName("Should prevent student from viewing another student's attendance history")
    void getStudentClassAttendance_unauthorized_throwsAccessDenied() {
        User otherUser = User.builder()
                .id(2L)
                .name("Other Student")
                .email("other@college.edu")
                .role(UserRole.ROLE_STUDENT)
                .build();

        when(studentRepository.findById(100L)).thenReturn(Optional.of(sampleStudent));
        when(userRepository.findByEmail("other@college.edu")).thenReturn(Optional.of(otherUser));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> attendanceService.getStudentClassAttendance(100L, "other@college.edu"));

        assertTrue(ex.getMessage().contains("not authorized to view another student's attendance"));
    }

    @Test
    @DisplayName("Should allow FACULTY to view any student's attendance history")
    void getStudentClassAttendance_asFaculty_success() {
        User facultyUser = User.builder()
                .id(99L)
                .name("Prof. Gupta")
                .email("prof@college.edu")
                .role(UserRole.ROLE_FACULTY)
                .build();

        when(studentRepository.findById(100L)).thenReturn(Optional.of(sampleStudent));
        when(userRepository.findByEmail("prof@college.edu")).thenReturn(Optional.of(facultyUser));
        when(classAttendanceRepository.findByStudentIdOrderByAttendanceDateDesc(100L))
                .thenReturn(List.of(sampleClassAttendance));

        List<AttendanceRecordResponse> records = attendanceService.getStudentClassAttendance(100L, "prof@college.edu");

        assertNotNull(records);
        assertEquals(1, records.size());
    }

    @Test
    @DisplayName("Should calculate attendance percentage accurately")
    void getStudentAttendanceSummary_calculation_success() {
        when(studentRepository.findById(100L)).thenReturn(Optional.of(sampleStudent));
        when(userRepository.findByEmail("aarav@college.edu")).thenReturn(Optional.of(sampleUser));

        when(classAttendanceRepository.countByStudentId(100L)).thenReturn(10L);
        when(classAttendanceRepository.countByStudentIdAndPresentTrue(100L)).thenReturn(8L);

        when(crtAttendanceRepository.countByStudentId(100L)).thenReturn(5L);
        when(crtAttendanceRepository.countByStudentIdAndPresentTrue(100L)).thenReturn(4L);

        when(classAttendanceRepository.findDistinctSubjectsByStudentId(100L)).thenReturn(List.of("Data Structures"));
        when(classAttendanceRepository.countByStudentIdAndSubject(100L, "Data Structures")).thenReturn(10L);
        when(classAttendanceRepository.countByStudentIdAndSubjectAndPresentTrue(100L, "Data Structures")).thenReturn(8L);

        AttendanceSummaryResponse summary = attendanceService.getStudentAttendanceSummary(100L, "aarav@college.edu");

        assertNotNull(summary);
        assertEquals(10L, summary.getTotalClassSessions());
        assertEquals(8L, summary.getAttendedClassSessions());
        assertEquals(80.0, summary.getClassAttendancePercentage());

        assertEquals(5L, summary.getTotalCrtSessions());
        assertEquals(4L, summary.getAttendedCrtSessions());
        assertEquals(80.0, summary.getCrtAttendancePercentage());

        assertEquals(15L, summary.getTotalSessions());
        assertEquals(12L, summary.getTotalAttended());
        assertEquals(80.0, summary.getOverallAttendancePercentage());
        assertTrue(summary.isEligibleForPlacements()); // >= 75%
        assertEquals(1, summary.getSubjectBreakdowns().size());
        assertEquals("Data Structures", summary.getSubjectBreakdowns().get(0).getSubject());
        assertEquals(80.0, summary.getSubjectBreakdowns().get(0).getPercentage());
    }

    @Test
    @DisplayName("Should handle 0 total attendance records safely without division by zero")
    void getStudentAttendanceSummary_emptyRecords_safeDefaults() {
        when(studentRepository.findById(100L)).thenReturn(Optional.of(sampleStudent));
        when(userRepository.findByEmail("aarav@college.edu")).thenReturn(Optional.of(sampleUser));

        when(classAttendanceRepository.countByStudentId(100L)).thenReturn(0L);
        when(classAttendanceRepository.countByStudentIdAndPresentTrue(100L)).thenReturn(0L);
        when(crtAttendanceRepository.countByStudentId(100L)).thenReturn(0L);
        when(crtAttendanceRepository.countByStudentIdAndPresentTrue(100L)).thenReturn(0L);
        when(classAttendanceRepository.findDistinctSubjectsByStudentId(100L)).thenReturn(List.of());

        AttendanceSummaryResponse summary = attendanceService.getStudentAttendanceSummary(100L, "aarav@college.edu");

        assertNotNull(summary);
        assertEquals(0L, summary.getTotalSessions());
        assertEquals(0.0, summary.getClassAttendancePercentage());
        assertEquals(0.0, summary.getCrtAttendancePercentage());
        assertEquals(0.0, summary.getOverallAttendancePercentage());
        assertFalse(summary.isEligibleForPlacements());
    }
}
