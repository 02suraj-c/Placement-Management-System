package com.placement.internship;

import com.placement.exception.ResourceNotFoundException;
import com.placement.internship.dto.InternshipRequest;
import com.placement.internship.dto.InternshipResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternshipService {

    private final InternshipRepository internshipRepository;

    @Transactional
    public InternshipResponse createInternship(InternshipRequest request, String userEmail) {
        if (request == null) {
            throw new IllegalArgumentException("Internship request cannot be null");
        }

        if (request.getApplicationDeadline() != null && request.getApplicationDeadline().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Application deadline cannot be in the past");
        }

        log.info("Creating new internship listing: '{}' at '{}' by user: {}",
                request.getTitle(), request.getCompany(), userEmail);

        Internship internship = Internship.builder()
                .title(request.getTitle().trim())
                .company(request.getCompany().trim())
                .description(request.getDescription().trim())
                .requiredSkills(request.getRequiredSkills().trim())
                .location(request.getLocation().trim())
                .stipend(request.getStipend() != null ? request.getStipend().trim() : null)
                .duration(request.getDuration() != null ? request.getDuration().trim() : null)
                .eligibilityCriteria(request.getEligibilityCriteria() != null ? request.getEligibilityCriteria().trim() : null)
                .applicationDeadline(request.getApplicationDeadline())
                .applicationUrl(request.getApplicationUrl() != null ? request.getApplicationUrl().trim() : null)
                .active(request.getActive() != null ? request.getActive() : true)
                .postedBy(userEmail)
                .build();

        Internship saved = internshipRepository.save(internship);
        log.info("Successfully created internship with ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public InternshipResponse getInternshipById(Long id) {
        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Internship", "id", id));
        return mapToResponse(internship);
    }

    @Transactional(readOnly = true)
    public List<InternshipResponse> getAllActiveInternships(String keyword, String location) {
        List<Internship> list;

        if (keyword != null && !keyword.isBlank()) {
            list = internshipRepository.searchActiveInternships(keyword.trim());
        } else if (location != null && !location.isBlank()) {
            list = internshipRepository.findActiveByLocation(location.trim());
        } else {
            list = internshipRepository.findByActiveTrueOrderByCreatedAtDesc();
        }

        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InternshipResponse> getAllInternships() {
        return internshipRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public InternshipResponse updateInternship(Long id, InternshipRequest request, String userEmail) {
        if (request == null) {
            throw new IllegalArgumentException("Internship request cannot be null");
        }

        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Internship", "id", id));

        if (request.getApplicationDeadline() != null && request.getApplicationDeadline().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Application deadline cannot be in the past");
        }

        log.info("Updating internship ID: {} by user: {}", id, userEmail);

        internship.setTitle(request.getTitle().trim());
        internship.setCompany(request.getCompany().trim());
        internship.setDescription(request.getDescription().trim());
        internship.setRequiredSkills(request.getRequiredSkills().trim());
        internship.setLocation(request.getLocation().trim());
        internship.setStipend(request.getStipend() != null ? request.getStipend().trim() : null);
        internship.setDuration(request.getDuration() != null ? request.getDuration().trim() : null);
        internship.setEligibilityCriteria(request.getEligibilityCriteria() != null ? request.getEligibilityCriteria().trim() : null);
        internship.setApplicationDeadline(request.getApplicationDeadline());
        internship.setApplicationUrl(request.getApplicationUrl() != null ? request.getApplicationUrl().trim() : null);
        if (request.getActive() != null) {
            internship.setActive(request.getActive());
        }

        Internship updated = internshipRepository.save(internship);
        log.info("Successfully updated internship ID: {}", updated.getId());
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteInternship(Long id, String userEmail) {
        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Internship", "id", id));

        log.info("Deleting internship ID: {} by user: {}", id, userEmail);
        internshipRepository.delete(internship);
        log.info("Successfully deleted internship ID: {}", id);
    }

    public InternshipResponse mapToResponse(Internship internship) {
        boolean isExpired = internship.getApplicationDeadline() != null &&
                internship.getApplicationDeadline().isBefore(LocalDate.now());

        return InternshipResponse.builder()
                .id(internship.getId())
                .title(internship.getTitle())
                .company(internship.getCompany())
                .description(internship.getDescription())
                .requiredSkills(internship.getRequiredSkills())
                .location(internship.getLocation())
                .stipend(internship.getStipend())
                .duration(internship.getDuration())
                .eligibilityCriteria(internship.getEligibilityCriteria())
                .applicationDeadline(internship.getApplicationDeadline())
                .applicationUrl(internship.getApplicationUrl())
                .active(internship.isActive())
                .expired(isExpired)
                .postedBy(internship.getPostedBy())
                .createdAt(internship.getCreatedAt())
                .updatedAt(internship.getUpdatedAt())
                .build();
    }
}
