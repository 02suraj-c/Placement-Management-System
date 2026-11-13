package com.placement.application;

import com.placement.application.dto.ApplicationRequest;
import com.placement.application.dto.ApplicationResponse;
import com.placement.application.dto.ApplicationStatusUpdateRequest;
import com.placement.eligibility.EligibilityService;
import com.placement.eligibility.dto.EligibilityCheckResponse;
import com.placement.exception.DuplicateResourceException;
import com.placement.exception.ResourceNotFoundException;
import com.placement.notification.NotificationService;
import com.placement.placement.DriveStatus;
import com.placement.placement.PlacementDrive;
import com.placement.placement.PlacementRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationService Tests")
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private PlacementRepository placementRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private EligibilityService eligibilityService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationTimelineRepository applicationTimelineRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private Student sampleStudent;
    private PlacementDrive sampleDrive;
    private User sampleUser;
    private ApplicationRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("student@college.edu")
                .role(UserRole.ROLE_STUDENT)
                .build();

        sampleStudent = Student.builder()
                .id(10L)
                .user(sampleUser)
                .rollNumber("CS2026-100")
                .cgpa(8.5)
                .department("CSE")
                .activeBacklogs(0)
                .build();

        sampleDrive = new PlacementDrive();
        sampleDrive.setId(100L);
        sampleDrive.setJobTitle("Software Engineer");
        sampleDrive.setStatus(DriveStatus.UPCOMING);
        sampleDrive.setApplicationStartDate(LocalDate.now().minusDays(2));
        sampleDrive.setApplicationEndDate(LocalDate.now().plusDays(5));

        sampleRequest = new ApplicationRequest();
        sampleRequest.setStudentId(10L);
        sampleRequest.setDriveId(100L);
    }

    @Test
    @DisplayName("Should successfully apply when student is eligible and drive is open")
    void createApplication_success() {
        when(studentRepository.findById(10L)).thenReturn(Optional.of(sampleStudent));
        when(placementRepository.findById(100L)).thenReturn(Optional.of(sampleDrive));
        when(applicationRepository.existsByStudentIdAndDriveId(10L, 100L)).thenReturn(false);

        EligibilityCheckResponse eligibleResponse = EligibilityCheckResponse.builder()
                .eligible(true)
                .reasons(List.of())
                .build();
        when(eligibilityService.evaluateEligibility(sampleStudent, sampleDrive)).thenReturn(eligibleResponse);

        PlacementApplication saved = new PlacementApplication();
        saved.setId(1L);
        saved.setStudentId(10L);
        saved.setDriveId(100L);
        saved.setStatus(ApplicationStatus.APPLIED);
        when(applicationRepository.save(any(PlacementApplication.class))).thenReturn(saved);

        ApplicationResponse response = applicationService.createApplication(sampleRequest);

        assertNotNull(response);
        assertEquals(ApplicationStatus.APPLIED, response.getStatus());
        verify(notificationService).createNotification(eq(10L), anyString());
    }

    @Test
    @DisplayName("Should reject duplicate application with DuplicateResourceException")
    void createApplication_duplicate_throwsException() {
        when(studentRepository.findById(10L)).thenReturn(Optional.of(sampleStudent));
        when(placementRepository.findById(100L)).thenReturn(Optional.of(sampleDrive));
        when(applicationRepository.existsByStudentIdAndDriveId(10L, 100L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                applicationService.createApplication(sampleRequest)
        );
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject application if drive deadline has passed")
    void createApplication_deadlinePassed_throwsIllegalState() {
        sampleDrive.setApplicationEndDate(LocalDate.now().minusDays(1));

        when(studentRepository.findById(10L)).thenReturn(Optional.of(sampleStudent));
        when(placementRepository.findById(100L)).thenReturn(Optional.of(sampleDrive));
        when(applicationRepository.existsByStudentIdAndDriveId(10L, 100L)).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                applicationService.createApplication(sampleRequest)
        );
        assertTrue(ex.getMessage().contains("deadline has passed"));
    }

    @Test
    @DisplayName("Should reject application if drive is cancelled")
    void createApplication_driveCancelled_throwsIllegalState() {
        sampleDrive.setStatus(DriveStatus.CANCELLED);

        when(studentRepository.findById(10L)).thenReturn(Optional.of(sampleStudent));
        when(placementRepository.findById(100L)).thenReturn(Optional.of(sampleDrive));
        when(applicationRepository.existsByStudentIdAndDriveId(10L, 100L)).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                applicationService.createApplication(sampleRequest)
        );
        assertTrue(ex.getMessage().contains("Placement drive is cancelled"));
    }

    @Test
    @DisplayName("Should reject application when student is ineligible")
    void createApplication_ineligible_throwsIllegalState() {
        when(studentRepository.findById(10L)).thenReturn(Optional.of(sampleStudent));
        when(placementRepository.findById(100L)).thenReturn(Optional.of(sampleDrive));
        when(applicationRepository.existsByStudentIdAndDriveId(10L, 100L)).thenReturn(false);

        EligibilityCheckResponse ineligible = EligibilityCheckResponse.builder()
                .eligible(false)
                .reasons(List.of("Student CGPA is below minimum required CGPA."))
                .build();
        when(eligibilityService.evaluateEligibility(sampleStudent, sampleDrive)).thenReturn(ineligible);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                applicationService.createApplication(sampleRequest)
        );
        assertTrue(ex.getMessage().contains("Student is not eligible"));
    }

    @Test
    @DisplayName("Should enforce valid status transitions")
    void updateStatus_validTransitions() {
        PlacementApplication app = new PlacementApplication();
        app.setId(1L);
        app.setStudentId(10L);
        app.setDriveId(100L);
        app.setStatus(ApplicationStatus.APPLIED);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(PlacementApplication.class))).thenAnswer(i -> i.getArgument(0));

        // APPLIED -> SHORTLISTED
        ApplicationStatusUpdateRequest update1 = new ApplicationStatusUpdateRequest();
        update1.setStatus(ApplicationStatus.SHORTLISTED);
        ApplicationResponse resp1 = applicationService.updateStatus(1L, update1);
        assertEquals(ApplicationStatus.SHORTLISTED, resp1.getStatus());

        // SHORTLISTED -> INTERVIEW_SCHEDULED
        ApplicationStatusUpdateRequest update2 = new ApplicationStatusUpdateRequest();
        update2.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        ApplicationResponse resp2 = applicationService.updateStatus(1L, update2);
        assertEquals(ApplicationStatus.INTERVIEW_SCHEDULED, resp2.getStatus());

        // INTERVIEW_SCHEDULED -> SELECTED
        ApplicationStatusUpdateRequest update3 = new ApplicationStatusUpdateRequest();
        update3.setStatus(ApplicationStatus.SELECTED);
        ApplicationResponse resp3 = applicationService.updateStatus(1L, update3);
        assertEquals(ApplicationStatus.SELECTED, resp3.getStatus());
    }

    @Test
    @DisplayName("Should reject invalid status transition from terminal state")
    void updateStatus_fromSelectedOrRejected_throwsIllegalState() {
        PlacementApplication app = new PlacementApplication();
        app.setId(1L);
        app.setStatus(ApplicationStatus.REJECTED);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));

        ApplicationStatusUpdateRequest update = new ApplicationStatusUpdateRequest();
        update.setStatus(ApplicationStatus.SHORTLISTED);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                applicationService.updateStatus(1L, update)
        );
        assertTrue(ex.getMessage().contains("Cannot change status"));
    }

    @Test
    @DisplayName("Should prevent student from viewing other student's applications")
    void getApplicationsByStudent_unauthorizedStudent_throwsAccessDenied() {
        User otherUser = User.builder()
                .id(2L)
                .email("other@college.edu")
                .role(UserRole.ROLE_STUDENT)
                .build();
        Student otherStudent = Student.builder().id(99L).build();

        when(studentRepository.existsById(10L)).thenReturn(true);
        when(userRepository.findByEmail("other@college.edu")).thenReturn(Optional.of(otherUser));
        when(studentRepository.findByUserEmail("other@college.edu")).thenReturn(Optional.of(otherStudent));

        assertThrows(AccessDeniedException.class, () ->
                applicationService.getApplicationsByStudent(10L, "other@college.edu")
        );
    }
}

