package com.placement.hackathon;

import com.placement.exception.ResourceNotFoundException;
import com.placement.hackathon.dto.HackathonRegistrationResponse;
import com.placement.hackathon.dto.HackathonRequest;
import com.placement.hackathon.dto.HackathonResponse;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
import com.placement.user.User;
import com.placement.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HackathonService Unit Tests")
class HackathonServiceTest {

    @Mock
    private HackathonRepository hackathonRepository;

    @Mock
    private HackathonRegistrationRepository hackathonRegistrationRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private HackathonService hackathonService;

    private Hackathon sampleHackathon;
    private HackathonRequest sampleRequest;
    private Student sampleStudent;
    private User sampleUser;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    @DisplayName("createHackathon: saves and returns mapped response with valid dates")
    void createHackathon_validRequest_success() {
        when(hackathonRepository.save(any(Hackathon.class))).thenAnswer(invocation -> {
            Hackathon toSave = invocation.getArgument(0);
            toSave.setId(10L);
            return toSave;
        });
        when(hackathonRegistrationRepository.countByHackathonId(any())).thenReturn(0L);

        HackathonResponse response = hackathonService.createHackathon(sampleRequest, "tpo@college.edu");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getTitle()).isEqualTo("Smart India Codefest 2026");
        assertThat(response.getOrganizer()).isEqualTo("Ministry of Education & AICTE");
        assertThat(response.isRegistrationOpen()).isTrue();
        assertThat(response.getPostedBy()).isEqualTo("tpo@college.edu");

        verify(hackathonRepository, times(1)).save(any(Hackathon.class));
    }

    @Test
    @DisplayName("createHackathon: throws IllegalArgumentException when request is null")
    void createHackathon_nullRequest_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> hackathonService.createHackathon(null, "tpo@college.edu"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Hackathon request cannot be null");
    }

    @Test
    @DisplayName("createHackathon: throws IllegalArgumentException when registration deadline is in the past")
    void createHackathon_regDeadlineInPast_throwsIllegalArgumentException() {
        sampleRequest.setRegistrationDeadline(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> hackathonService.createHackathon(sampleRequest, "tpo@college.edu"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Registration deadline cannot be in the past");

        verify(hackathonRepository, never()).save(any());
    }

    @Test
    @DisplayName("createHackathon: throws IllegalArgumentException when registration deadline is after start date")
    void createHackathon_regDeadlineAfterStartDate_throwsIllegalArgumentException() {
        sampleRequest.setRegistrationDeadline(sampleRequest.getStartDate().plusDays(1));

        assertThatThrownBy(() -> hackathonService.createHackathon(sampleRequest, "tpo@college.edu"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Registration deadline cannot be after hackathon start date");

        verify(hackathonRepository, never()).save(any());
    }

    @Test
    @DisplayName("createHackathon: throws IllegalArgumentException when start date is after end date")
    void createHackathon_startDateAfterEndDate_throwsIllegalArgumentException() {
        sampleRequest.setStartDate(sampleRequest.getEndDate().plusDays(1));

        assertThatThrownBy(() -> hackathonService.createHackathon(sampleRequest, "tpo@college.edu"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Hackathon start date cannot be after end date");

        verify(hackathonRepository, never()).save(any());
    }

    @Test
    @DisplayName("getHackathonById: returns mapped response when found")
    void getHackathonById_found_returnsResponse() {
        when(hackathonRepository.findById(1L)).thenReturn(Optional.of(sampleHackathon));
        when(hackathonRegistrationRepository.countByHackathonId(1L)).thenReturn(5L);

        HackathonResponse response = hackathonService.getHackathonById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRegisteredCount()).isEqualTo(5L);
        assertThat(response.isRegistrationOpen()).isTrue();
    }

    @Test
    @DisplayName("getHackathonById: throws ResourceNotFoundException when non-existent")
    void getHackathonById_notFound_throwsResourceNotFoundException() {
        when(hackathonRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hackathonService.getHackathonById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Hackathon not found with id : '999'");
    }

    @Test
    @DisplayName("getAllActiveHackathons: returns active list when no filter provided")
    void getAllActiveHackathons_noFilter_returnsActiveList() {
        when(hackathonRepository.findByActiveTrueOrderByStartDateAsc()).thenReturn(List.of(sampleHackathon));
        when(hackathonRegistrationRepository.countByHackathonId(1L)).thenReturn(0L);

        List<HackathonResponse> result = hackathonService.getAllActiveHackathons(null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Smart India Codefest 2026");
        verify(hackathonRepository, times(1)).findByActiveTrueOrderByStartDateAsc();
    }

    @Test
    @DisplayName("getAllActiveHackathons: filters by keyword when provided")
    void getAllActiveHackathons_withKeyword_callsSearch() {
        when(hackathonRepository.searchActiveHackathons("Codefest")).thenReturn(List.of(sampleHackathon));

        List<HackathonResponse> result = hackathonService.getAllActiveHackathons("Codefest", null, null);

        assertThat(result).hasSize(1);
        verify(hackathonRepository, times(1)).searchActiveHackathons("Codefest");
        verify(hackathonRepository, never()).findByActiveTrueOrderByStartDateAsc();
    }

    @Test
    @DisplayName("getAllActiveHackathons: filters by mode when mode is provided")
    void getAllActiveHackathons_withMode_callsModeSearch() {
        when(hackathonRepository.findByActiveTrueAndModeOrderByStartDateAsc(HackathonMode.HYBRID))
                .thenReturn(List.of(sampleHackathon));

        List<HackathonResponse> result = hackathonService.getAllActiveHackathons(null, HackathonMode.HYBRID, null);

        assertThat(result).hasSize(1);
        verify(hackathonRepository, times(1)).findByActiveTrueAndModeOrderByStartDateAsc(HackathonMode.HYBRID);
    }

    @Test
    @DisplayName("getAllHackathons: returns all hackathons for staff management")
    void getAllHackathons_returnsAll() {
        Hackathon inactive = Hackathon.builder()
                .id(2L)
                .title("Past Hackathon")
                .organizer("Old Org")
                .description("Past event")
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now().minusDays(28))
                .registrationDeadline(LocalDate.now().minusDays(35))
                .mode(HackathonMode.ONLINE)
                .status(HackathonStatus.COMPLETED)
                .active(false)
                .build();

        when(hackathonRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(sampleHackathon, inactive));

        List<HackathonResponse> result = hackathonService.getAllHackathons();

        assertThat(result).hasSize(2);
        assertThat(result.get(1).isActive()).isFalse();
        assertThat(result.get(1).isRegistrationOpen()).isFalse();
    }

    @Test
    @DisplayName("updateHackathon: successfully updates event details and returns response")
    void updateHackathon_validRequest_success() {
        when(hackathonRepository.findById(1L)).thenReturn(Optional.of(sampleHackathon));
        when(hackathonRepository.save(any(Hackathon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sampleRequest.setTitle("Updated Codefest 2026");
        sampleRequest.setMode(HackathonMode.ONLINE);

        HackathonResponse response = hackathonService.updateHackathon(1L, sampleRequest, "tpo@college.edu");

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Updated Codefest 2026");
        assertThat(response.getMode()).isEqualTo(HackathonMode.ONLINE);
        verify(hackathonRepository, times(1)).save(any(Hackathon.class));
    }

    @Test
    @DisplayName("deleteHackathon: deletes existing hackathon successfully")
    void deleteHackathon_found_deletes() {
        when(hackathonRepository.findById(1L)).thenReturn(Optional.of(sampleHackathon));

        hackathonService.deleteHackathon(1L, "tpo@college.edu");

        verify(hackathonRepository, times(1)).delete(sampleHackathon);
    }

    @Test
    @DisplayName("registerStudent: registers student successfully before deadline")
    void registerStudent_validStudentAndOpenHackathon_success() {
        when(hackathonRepository.findById(1L)).thenReturn(Optional.of(sampleHackathon));
        when(studentRepository.findByUserEmail("aarav@college.edu")).thenReturn(Optional.of(sampleStudent));
        when(hackathonRegistrationRepository.existsByHackathonIdAndStudentId(1L, 100L)).thenReturn(false);

        HackathonRegistration registration = HackathonRegistration.builder()
                .id(50L)
                .hackathon(sampleHackathon)
                .student(sampleStudent)
                .registeredAt(LocalDateTime.now())
                .build();

        when(hackathonRegistrationRepository.save(any(HackathonRegistration.class))).thenReturn(registration);

        HackathonRegistrationResponse response = hackathonService.registerStudent(1L, "aarav@college.edu");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(50L);
        assertThat(response.getHackathonTitle()).isEqualTo("Smart India Codefest 2026");
        assertThat(response.getStudentName()).isEqualTo("Aarav Sharma");
        assertThat(response.getRollNumber()).isEqualTo("CS2026-042");

        verify(hackathonRegistrationRepository, times(1)).save(any(HackathonRegistration.class));
    }

    @Test
    @DisplayName("registerStudent: throws IllegalStateException when registration deadline has passed")
    void registerStudent_deadlinePassed_throwsIllegalStateException() {
        sampleHackathon.setRegistrationDeadline(LocalDate.now().minusDays(1));

        when(hackathonRepository.findById(1L)).thenReturn(Optional.of(sampleHackathon));
        when(studentRepository.findByUserEmail("aarav@college.edu")).thenReturn(Optional.of(sampleStudent));

        assertThatThrownBy(() -> hackathonService.registerStudent(1L, "aarav@college.edu"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Registration deadline has passed");

        verify(hackathonRegistrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerStudent: throws IllegalStateException when hackathon is completed")
    void registerStudent_hackathonCompleted_throwsIllegalStateException() {
        sampleHackathon.setStatus(HackathonStatus.COMPLETED);

        when(hackathonRepository.findById(1L)).thenReturn(Optional.of(sampleHackathon));
        when(studentRepository.findByUserEmail("aarav@college.edu")).thenReturn(Optional.of(sampleStudent));

        assertThatThrownBy(() -> hackathonService.registerStudent(1L, "aarav@college.edu"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Registration is closed for this hackathon");

        verify(hackathonRegistrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerStudent: throws IllegalStateException when student is already registered")
    void registerStudent_alreadyRegistered_throwsIllegalStateException() {
        when(hackathonRepository.findById(1L)).thenReturn(Optional.of(sampleHackathon));
        when(studentRepository.findByUserEmail("aarav@college.edu")).thenReturn(Optional.of(sampleStudent));
        when(hackathonRegistrationRepository.existsByHackathonIdAndStudentId(1L, 100L)).thenReturn(true);

        assertThatThrownBy(() -> hackathonService.registerStudent(1L, "aarav@college.edu"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Student is already registered for this hackathon");

        verify(hackathonRegistrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("getStudentRegistrations: returns registered hackathons for authenticated student")
    void getStudentRegistrations_returnsRegistrations() {
        when(studentRepository.findByUserEmail("aarav@college.edu")).thenReturn(Optional.of(sampleStudent));

        HackathonRegistration reg = HackathonRegistration.builder()
                .id(50L)
                .hackathon(sampleHackathon)
                .student(sampleStudent)
                .registeredAt(LocalDateTime.now())
                .build();

        when(hackathonRegistrationRepository.findByStudentIdOrderByRegisteredAtDesc(100L))
                .thenReturn(List.of(reg));

        List<HackathonRegistrationResponse> result = hackathonService.getStudentRegistrations("aarav@college.edu");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHackathonTitle()).isEqualTo("Smart India Codefest 2026");
    }
}
