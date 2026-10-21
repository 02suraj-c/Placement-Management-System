package com.placement.hackathon;

import com.placement.exception.ResourceNotFoundException;
import com.placement.hackathon.dto.HackathonRegistrationResponse;
import com.placement.hackathon.dto.HackathonRequest;
import com.placement.hackathon.dto.HackathonResponse;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HackathonService {

    private final HackathonRepository hackathonRepository;
    private final HackathonRegistrationRepository hackathonRegistrationRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public HackathonResponse createHackathon(HackathonRequest request, String userEmail) {
        if (request == null) {
            throw new IllegalArgumentException("Hackathon request cannot be null");
        }

        validateEventDates(request.getStartDate(), request.getEndDate(), request.getRegistrationDeadline(), true);

        log.info("Creating new hackathon: '{}' by organizer: '{}' (user: {})",
                request.getTitle(), request.getOrganizer(), userEmail);

        Hackathon hackathon = Hackathon.builder()
                .title(request.getTitle().trim())
                .organizer(request.getOrganizer().trim())
                .description(request.getDescription().trim())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .registrationDeadline(request.getRegistrationDeadline())
                .mode(request.getMode())
                .location(request.getLocation() != null ? request.getLocation().trim() : null)
                .eligibilityCriteria(request.getEligibilityCriteria() != null ? request.getEligibilityCriteria().trim() : null)
                .registrationUrl(request.getRegistrationUrl() != null ? request.getRegistrationUrl().trim() : null)
                .status(request.getStatus() != null ? request.getStatus() : HackathonStatus.UPCOMING)
                .active(request.getActive() != null ? request.getActive() : true)
                .postedBy(userEmail)
                .build();

        Hackathon saved = hackathonRepository.save(hackathon);
        log.info("Successfully created hackathon with ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public HackathonResponse getHackathonById(Long id) {
        Hackathon hackathon = hackathonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon", "id", id));
        return mapToResponse(hackathon);
    }

    @Transactional(readOnly = true)
    public List<HackathonResponse> getAllActiveHackathons(String keyword, HackathonMode mode, HackathonStatus status) {
        List<Hackathon> list;

        if (keyword != null && !keyword.isBlank()) {
            list = hackathonRepository.searchActiveHackathons(keyword.trim());
        } else if (mode != null) {
            list = hackathonRepository.findByActiveTrueAndModeOrderByStartDateAsc(mode);
        } else if (status != null) {
            list = hackathonRepository.findByActiveTrueAndStatusOrderByStartDateAsc(status);
        } else {
            list = hackathonRepository.findByActiveTrueOrderByStartDateAsc();
        }

        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HackathonResponse> getAllHackathons() {
        return hackathonRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public HackathonResponse updateHackathon(Long id, HackathonRequest request, String userEmail) {
        if (request == null) {
            throw new IllegalArgumentException("Hackathon request cannot be null");
        }

        Hackathon hackathon = hackathonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon", "id", id));

        validateEventDates(request.getStartDate(), request.getEndDate(), request.getRegistrationDeadline(), false);

        log.info("Updating hackathon ID: {} by user: {}", id, userEmail);

        hackathon.setTitle(request.getTitle().trim());
        hackathon.setOrganizer(request.getOrganizer().trim());
        hackathon.setDescription(request.getDescription().trim());
        hackathon.setStartDate(request.getStartDate());
        hackathon.setEndDate(request.getEndDate());
        hackathon.setRegistrationDeadline(request.getRegistrationDeadline());
        hackathon.setMode(request.getMode());
        hackathon.setLocation(request.getLocation() != null ? request.getLocation().trim() : null);
        hackathon.setEligibilityCriteria(request.getEligibilityCriteria() != null ? request.getEligibilityCriteria().trim() : null);
        hackathon.setRegistrationUrl(request.getRegistrationUrl() != null ? request.getRegistrationUrl().trim() : null);
        if (request.getStatus() != null) {
            hackathon.setStatus(request.getStatus());
        }
        if (request.getActive() != null) {
            hackathon.setActive(request.getActive());
        }

        Hackathon updated = hackathonRepository.save(hackathon);
        log.info("Successfully updated hackathon ID: {}", updated.getId());
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteHackathon(Long id, String userEmail) {
        Hackathon hackathon = hackathonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon", "id", id));

        log.info("Deleting hackathon ID: {} by user: {}", id, userEmail);
        hackathonRepository.delete(hackathon);
        log.info("Successfully deleted hackathon ID: {}", id);
    }

    @Transactional
    public HackathonRegistrationResponse registerStudent(Long hackathonId, String userEmail) {
        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon", "id", hackathonId));

        Student student = studentRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + userEmail));

        if (!hackathon.isActive()) {
            throw new IllegalStateException("Hackathon listing is inactive");
        }

        if (hackathon.getStatus() == HackathonStatus.COMPLETED || hackathon.getStatus() == HackathonStatus.CANCELLED) {
            throw new IllegalStateException("Registration is closed for this hackathon");
        }

        if (LocalDate.now().isAfter(hackathon.getRegistrationDeadline())) {
            throw new IllegalStateException("Registration deadline has passed on " + hackathon.getRegistrationDeadline());
        }

        if (hackathonRegistrationRepository.existsByHackathonIdAndStudentId(hackathon.getId(), student.getId())) {
            throw new IllegalStateException("Student is already registered for this hackathon");
        }

        log.info("Registering student ID: {} for hackathon ID: {}", student.getId(), hackathon.getId());

        HackathonRegistration registration = HackathonRegistration.builder()
                .hackathon(hackathon)
                .student(student)
                .build();

        HackathonRegistration saved = hackathonRegistrationRepository.save(registration);
        return mapRegistrationToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<HackathonRegistrationResponse> getStudentRegistrations(String userEmail) {
        Student student = studentRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + userEmail));

        return hackathonRegistrationRepository.findByStudentIdOrderByRegisteredAtDesc(student.getId()).stream()
                .map(this::mapRegistrationToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HackathonRegistrationResponse> getHackathonRegistrations(Long hackathonId) {
        if (!hackathonRepository.existsById(hackathonId)) {
            throw new ResourceNotFoundException("Hackathon", "id", hackathonId);
        }

        return hackathonRegistrationRepository.findByHackathonId(hackathonId).stream()
                .map(this::mapRegistrationToResponse)
                .toList();
    }

    private void validateEventDates(LocalDate startDate, LocalDate endDate, LocalDate registrationDeadline, boolean isNew) {
        if (registrationDeadline == null || startDate == null || endDate == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }

        if (isNew && registrationDeadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Registration deadline cannot be in the past");
        }

        if (registrationDeadline.isAfter(startDate)) {
            throw new IllegalArgumentException("Registration deadline cannot be after hackathon start date");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Hackathon start date cannot be after end date");
        }
    }

    public HackathonResponse mapToResponse(Hackathon hackathon) {
        boolean registrationOpen = hackathon.isActive() &&
                hackathon.getStatus() != HackathonStatus.COMPLETED &&
                hackathon.getStatus() != HackathonStatus.CANCELLED &&
                (hackathon.getRegistrationDeadline() != null && !LocalDate.now().isAfter(hackathon.getRegistrationDeadline()));

        long regCount = hackathonRegistrationRepository.countByHackathonId(hackathon.getId());

        return HackathonResponse.builder()
                .id(hackathon.getId())
                .title(hackathon.getTitle())
                .organizer(hackathon.getOrganizer())
                .description(hackathon.getDescription())
                .startDate(hackathon.getStartDate())
                .endDate(hackathon.getEndDate())
                .registrationDeadline(hackathon.getRegistrationDeadline())
                .mode(hackathon.getMode())
                .location(hackathon.getLocation())
                .eligibilityCriteria(hackathon.getEligibilityCriteria())
                .registrationUrl(hackathon.getRegistrationUrl())
                .status(hackathon.getStatus())
                .active(hackathon.isActive())
                .registrationOpen(registrationOpen)
                .registeredCount(regCount)
                .postedBy(hackathon.getPostedBy())
                .createdAt(hackathon.getCreatedAt())
                .updatedAt(hackathon.getUpdatedAt())
                .build();
    }

    public HackathonRegistrationResponse mapRegistrationToResponse(HackathonRegistration reg) {
        return HackathonRegistrationResponse.builder()
                .id(reg.getId())
                .hackathonId(reg.getHackathon().getId())
                .hackathonTitle(reg.getHackathon().getTitle())
                .organizer(reg.getHackathon().getOrganizer())
                .studentId(reg.getStudent().getId())
                .studentName(reg.getStudent().getUser() != null ? reg.getStudent().getUser().getName() : null)
                .rollNumber(reg.getStudent().getRollNumber())
                .registeredAt(reg.getRegisteredAt())
                .build();
    }
}
