package com.placement.interview;

import com.placement.application.ApplicationRepository;
import com.placement.application.ApplicationStatus;
import com.placement.application.PlacementApplication;
import com.placement.exception.ResourceNotFoundException;
import com.placement.interview.dto.InterviewResultRequest;
import com.placement.interview.dto.InterviewScheduleRequest;
import com.placement.notification.NotificationService;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InterviewService Tests")
class InterviewServiceTest {

    @Mock
    private InterviewRoundRepository interviewRoundRepository;

    @Mock
    private RoundResultRepository roundResultRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private InterviewService interviewService;

    private PlacementApplication sampleApplication;
    private InterviewScheduleRequest sampleScheduleRequest;
    private InterviewRound sampleRound;
    private User staffUser;
    private User studentUser;
    private Student sampleStudent;

    @BeforeEach
    void setUp() {
        staffUser = User.builder()
                .id(1L)
                .email("tpo@college.edu")
                .role(UserRole.ROLE_PLACEMENT_OFFICER)
                .build();

        studentUser = User.builder()
                .id(2L)
                .email("student@college.edu")
                .role(UserRole.ROLE_STUDENT)
                .build();

        sampleStudent = Student.builder()
                .id(10L)
                .user(studentUser)
                .build();

        sampleApplication = new PlacementApplication();
        sampleApplication.setId(100L);
        sampleApplication.setStudentId(10L);
        sampleApplication.setDriveId(50L);
        sampleApplication.setStatus(ApplicationStatus.SHORTLISTED);

        sampleScheduleRequest = new InterviewScheduleRequest();
        sampleScheduleRequest.setApplicationId(100L);
        sampleScheduleRequest.setRoundName("Technical Round 1");
        sampleScheduleRequest.setInterviewDateTime(LocalDateTime.now().plusDays(2));
        sampleScheduleRequest.setMode("ONLINE");
        sampleScheduleRequest.setLocation("Google Meet");

        sampleRound = new InterviewRound();
        sampleRound.setId(500L);
        sampleRound.setApplicationId(100L);
        sampleRound.setRoundName("Technical Round 1");
        sampleRound.setInterviewDateTime(LocalDateTime.now().plusDays(2));
        sampleRound.setMode("ONLINE");
        sampleRound.setStatus("SCHEDULED");
    }

    @Test
    @DisplayName("Staff can schedule an interview for an active application")
    void scheduleInterview_byStaff_success() {
        when(userRepository.findByEmail("tpo@college.edu")).thenReturn(Optional.of(staffUser));
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(sampleApplication));
        when(interviewRoundRepository.save(any(InterviewRound.class))).thenReturn(sampleRound);

        InterviewRound scheduled = interviewService.scheduleInterview(sampleScheduleRequest, "tpo@college.edu");

        assertNotNull(scheduled);
        assertEquals(ApplicationStatus.INTERVIEW_SCHEDULED, sampleApplication.getStatus());
        verify(applicationRepository).save(sampleApplication);
        verify(notificationService).createNotification(eq(10L), anyString());
    }

    @Test
    @DisplayName("Students cannot schedule an interview")
    void scheduleInterview_byStudent_throwsAccessDenied() {
        when(userRepository.findByEmail("student@college.edu")).thenReturn(Optional.of(studentUser));

        assertThrows(AccessDeniedException.class, () ->
                interviewService.scheduleInterview(sampleScheduleRequest, "student@college.edu")
        );
        verify(interviewRoundRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cannot schedule interview for a rejected application")
    void scheduleInterview_rejectedApplication_throwsIllegalState() {
        sampleApplication.setStatus(ApplicationStatus.REJECTED);
        when(userRepository.findByEmail("tpo@college.edu")).thenReturn(Optional.of(staffUser));
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(sampleApplication));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                interviewService.scheduleInterview(sampleScheduleRequest, "tpo@college.edu")
        );
        assertTrue(ex.getMessage().contains("rejected application"));
    }

    @Test
    @DisplayName("Cannot schedule interview in the past")
    void scheduleInterview_pastTime_throwsIllegalArgument() {
        sampleScheduleRequest.setInterviewDateTime(LocalDateTime.now().minusDays(1));
        when(userRepository.findByEmail("tpo@college.edu")).thenReturn(Optional.of(staffUser));
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(sampleApplication));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                interviewService.scheduleInterview(sampleScheduleRequest, "tpo@college.edu")
        );
        assertTrue(ex.getMessage().contains("past"));
    }

    @Test
    @DisplayName("Student can view interviews for their own application")
    void getInterviewsByApplication_ownApplication_success() {
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(sampleApplication));
        when(userRepository.findByEmail("student@college.edu")).thenReturn(Optional.of(studentUser));
        when(studentRepository.findByUserEmail("student@college.edu")).thenReturn(Optional.of(sampleStudent));
        when(interviewRoundRepository.findByApplicationId(100L)).thenReturn(List.of(sampleRound));

        List<InterviewRound> list = interviewService.getInterviewsByApplication(100L, "student@college.edu");

        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("Student cannot view interviews for another student's application")
    void getInterviewsByApplication_otherStudent_throwsAccessDenied() {
        Student otherStudent = Student.builder().id(999L).build();
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(sampleApplication));
        when(userRepository.findByEmail("student@college.edu")).thenReturn(Optional.of(studentUser));
        when(studentRepository.findByUserEmail("student@college.edu")).thenReturn(Optional.of(otherStudent));

        assertThrows(AccessDeniedException.class, () ->
                interviewService.getInterviewsByApplication(100L, "student@college.edu")
        );
    }

    @Test
    @DisplayName("Staff can record interview result and update application status")
    void recordResult_passed_success() {
        when(userRepository.findByEmail("tpo@college.edu")).thenReturn(Optional.of(staffUser));
        when(interviewRoundRepository.findById(500L)).thenReturn(Optional.of(sampleRound));
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(sampleApplication));
        when(roundResultRepository.save(any(RoundResult.class))).thenAnswer(i -> i.getArgument(0));

        InterviewResultRequest resultRequest = new InterviewResultRequest();
        resultRequest.setResult("PASSED");
        resultRequest.setScore(90.0);
        resultRequest.setRemarks("Excellent communication and technical skills");

        RoundResult result = interviewService.recordResult(500L, resultRequest, "tpo@college.edu");

        assertNotNull(result);
        assertEquals("PASSED", result.getResult());
        assertEquals("PASSED", sampleRound.getStatus());
        assertEquals(ApplicationStatus.SHORTLISTED, sampleApplication.getStatus());
        verify(notificationService).createNotification(eq(10L), anyString());
    }

    @Test
    @DisplayName("Student cannot record interview results")
    void recordResult_byStudent_throwsAccessDenied() {
        when(userRepository.findByEmail("student@college.edu")).thenReturn(Optional.of(studentUser));

        InterviewResultRequest resultRequest = new InterviewResultRequest();
        resultRequest.setResult("PASSED");

        assertThrows(AccessDeniedException.class, () ->
                interviewService.recordResult(500L, resultRequest, "student@college.edu")
        );
    }
}
