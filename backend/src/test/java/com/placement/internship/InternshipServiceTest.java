package com.placement.internship;

import com.placement.exception.ResourceNotFoundException;
import com.placement.internship.dto.InternshipRequest;
import com.placement.internship.dto.InternshipResponse;
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
class InternshipServiceTest {

    @Mock
    private InternshipRepository internshipRepository;

    @InjectMocks
    private InternshipService internshipService;

    private Internship sampleInternship;
    private InternshipRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleInternship = Internship.builder()
                .id(1L)
                .title("Software Engineer Intern")
                .company("Google")
                .description("Work on scalable cloud systems and APIs")
                .requiredSkills("Java, Spring Boot, MySQL")
                .location("Bangalore / Remote")
                .stipend("₹45,000 / month")
                .duration("6 Months")
                .eligibilityCriteria("B.Tech CSE/IT, CGPA >= 8.0")
                .applicationDeadline(LocalDate.now().plusDays(30))
                .applicationUrl("https://careers.google.com/jobs/123")
                .active(true)
                .postedBy("tpo@college.edu")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleRequest = InternshipRequest.builder()
                .title("Software Engineer Intern")
                .company("Google")
                .description("Work on scalable cloud systems and APIs")
                .requiredSkills("Java, Spring Boot, MySQL")
                .location("Bangalore / Remote")
                .stipend("₹45,000 / month")
                .duration("6 Months")
                .eligibilityCriteria("B.Tech CSE/IT, CGPA >= 8.0")
                .applicationDeadline(LocalDate.now().plusDays(30))
                .applicationUrl("https://careers.google.com/jobs/123")
                .active(true)
                .build();
    }

    @Test
    @DisplayName("createInternship: saves and returns mapped InternshipResponse with valid data")
    void createInternship_validRequest_success() {
        when(internshipRepository.save(any(Internship.class))).thenAnswer(invocation -> {
            Internship toSave = invocation.getArgument(0);
            toSave.setId(10L);
            return toSave;
        });

        InternshipResponse response = internshipService.createInternship(sampleRequest, "tpo@college.edu");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getTitle()).isEqualTo("Software Engineer Intern");
        assertThat(response.getCompany()).isEqualTo("Google");
        assertThat(response.getPostedBy()).isEqualTo("tpo@college.edu");
        assertThat(response.isExpired()).isFalse();
        assertThat(response.isActive()).isTrue();

        verify(internshipRepository, times(1)).save(any(Internship.class));
    }

    @Test
    @DisplayName("createInternship: throws IllegalArgumentException when request is null")
    void createInternship_nullRequest_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> internshipService.createInternship(null, "tpo@college.edu"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Internship request cannot be null");

        verify(internshipRepository, never()).save(any());
    }

    @Test
    @DisplayName("createInternship: throws IllegalArgumentException when deadline is in the past")
    void createInternship_pastDeadline_throwsIllegalArgumentException() {
        sampleRequest.setApplicationDeadline(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> internshipService.createInternship(sampleRequest, "tpo@college.edu"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Application deadline cannot be in the past");

        verify(internshipRepository, never()).save(any());
    }

    @Test
    @DisplayName("getInternshipById: returns mapped response when found")
    void getInternshipById_found_returnsResponse() {
        when(internshipRepository.findById(1L)).thenReturn(Optional.of(sampleInternship));

        InternshipResponse response = internshipService.getInternshipById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCompany()).isEqualTo("Google");
        assertThat(response.isExpired()).isFalse();
    }

    @Test
    @DisplayName("getInternshipById: throws ResourceNotFoundException when ID does not exist")
    void getInternshipById_notFound_throwsResourceNotFoundException() {
        when(internshipRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internshipService.getInternshipById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Internship not found with id : '999'");
    }

    @Test
    @DisplayName("getAllActiveInternships: returns active listings when no filters applied")
    void getAllActiveInternships_noFilter_returnsActiveOnly() {
        when(internshipRepository.findByActiveTrueOrderByCreatedAtDesc())
                .thenReturn(List.of(sampleInternship));

        List<InternshipResponse> result = internshipService.getAllActiveInternships(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Software Engineer Intern");
        verify(internshipRepository, times(1)).findByActiveTrueOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("getAllActiveInternships: searches by keyword when keyword is provided")
    void getAllActiveInternships_withKeyword_callsSearch() {
        when(internshipRepository.searchActiveInternships("Java"))
                .thenReturn(List.of(sampleInternship));

        List<InternshipResponse> result = internshipService.getAllActiveInternships("Java", null);

        assertThat(result).hasSize(1);
        verify(internshipRepository, times(1)).searchActiveInternships("Java");
        verify(internshipRepository, never()).findByActiveTrueOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("getAllActiveInternships: searches by location when location is provided")
    void getAllActiveInternships_withLocation_callsLocationSearch() {
        when(internshipRepository.findActiveByLocation("Bangalore"))
                .thenReturn(List.of(sampleInternship));

        List<InternshipResponse> result = internshipService.getAllActiveInternships(null, "Bangalore");

        assertThat(result).hasSize(1);
        verify(internshipRepository, times(1)).findActiveByLocation("Bangalore");
        verify(internshipRepository, never()).findByActiveTrueOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("getAllInternships: returns all listings including inactive")
    void getAllInternships_returnsAllListings() {
        Internship inactive = Internship.builder()
                .id(2L)
                .title("Frontend Intern")
                .company("Amazon")
                .description("React frontend work")
                .requiredSkills("React, CSS")
                .location("Hyderabad")
                .applicationDeadline(LocalDate.now().plusDays(10))
                .active(false)
                .build();

        when(internshipRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(sampleInternship, inactive));

        List<InternshipResponse> result = internshipService.getAllInternships();

        assertThat(result).hasSize(2);
        assertThat(result.get(1).isActive()).isFalse();
    }

    @Test
    @DisplayName("updateInternship: successfully updates fields and returns response")
    void updateInternship_validRequest_success() {
        when(internshipRepository.findById(1L)).thenReturn(Optional.of(sampleInternship));
        when(internshipRepository.save(any(Internship.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InternshipRequest updateRequest = InternshipRequest.builder()
                .title("Senior Cloud Intern")
                .company("Google Cloud")
                .description("Updated description")
                .requiredSkills("Java, GCP, Kubernetes")
                .location("Remote")
                .stipend("₹50,000 / month")
                .duration("6 Months")
                .eligibilityCriteria("CGPA >= 8.5")
                .applicationDeadline(LocalDate.now().plusDays(45))
                .applicationUrl("https://careers.google.com/jobs/456")
                .active(true)
                .build();

        InternshipResponse response = internshipService.updateInternship(1L, updateRequest, "admin@college.edu");

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Senior Cloud Intern");
        assertThat(response.getCompany()).isEqualTo("Google Cloud");
        assertThat(response.getStipend()).isEqualTo("₹50,000 / month");
        verify(internshipRepository, times(1)).save(any(Internship.class));
    }

    @Test
    @DisplayName("updateInternship: throws ResourceNotFoundException if internship not found")
    void updateInternship_notFound_throwsResourceNotFoundException() {
        when(internshipRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internshipService.updateInternship(999L, sampleRequest, "admin@college.edu"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(internshipRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateInternship: throws IllegalArgumentException if updated deadline is in the past")
    void updateInternship_pastDeadline_throwsIllegalArgumentException() {
        when(internshipRepository.findById(1L)).thenReturn(Optional.of(sampleInternship));

        sampleRequest.setApplicationDeadline(LocalDate.now().minusDays(5));

        assertThatThrownBy(() -> internshipService.updateInternship(1L, sampleRequest, "admin@college.edu"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Application deadline cannot be in the past");

        verify(internshipRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteInternship: deletes existing internship successfully")
    void deleteInternship_found_deletes() {
        when(internshipRepository.findById(1L)).thenReturn(Optional.of(sampleInternship));

        internshipService.deleteInternship(1L, "admin@college.edu");

        verify(internshipRepository, times(1)).delete(sampleInternship);
    }

    @Test
    @DisplayName("deleteInternship: throws ResourceNotFoundException when non-existent")
    void deleteInternship_notFound_throwsResourceNotFoundException() {
        when(internshipRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internshipService.deleteInternship(999L, "admin@college.edu"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(internshipRepository, never()).delete(any());
    }

    @Test
    @DisplayName("mapToResponse: correctly marks internship as expired when deadline is before today")
    void mapToResponse_expiredDeadline_setsExpiredTrue() {
        sampleInternship.setApplicationDeadline(LocalDate.now().minusDays(2));

        InternshipResponse response = internshipService.mapToResponse(sampleInternship);

        assertThat(response.isExpired()).isTrue();
    }
}
