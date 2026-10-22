package com.placement.student;

import com.placement.exception.ResourceNotFoundException;
import com.placement.student.dto.StudentProfileRequest;
import com.placement.student.dto.StudentProfileResponse;
import com.placement.student.dto.StudentUpdateRequest;
import com.placement.user.User;
import com.placement.user.UserRepository;
import com.placement.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    @Transactional
    public StudentProfileResponse createProfile(StudentProfileRequest request, String userEmail) {
        log.info("Creating student profile for user: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        if (studentRepository.existsByUserId(user.getId())) {
            throw new IllegalArgumentException("Student profile already exists for user: " + userEmail);
        }

        if (studentRepository.existsByRollNumber(request.getRollNumber())) {
            throw new IllegalArgumentException("Roll number already registered: " + request.getRollNumber());
        }

        if (request.getEnrollmentNumber() != null && !request.getEnrollmentNumber().trim().isEmpty()) {
            if (studentRepository.existsByEnrollmentNumber(request.getEnrollmentNumber().trim())) {
                throw new IllegalArgumentException("Enrollment number already registered: " + request.getEnrollmentNumber());
            }
        }

        validateAcademicData(request.getCgpa(), request.getCurrentSemester(), request.getGraduationYear());

        Student student = Student.builder()
                .user(user)
                .rollNumber(request.getRollNumber().trim())
                .enrollmentNumber(request.getEnrollmentNumber() != null ? request.getEnrollmentNumber().trim() : null)
                .department(request.getDepartment().trim())
                .course(request.getCourse().trim())
                .currentSemester(request.getCurrentSemester())
                .cgpa(request.getCgpa())
                .graduationYear(request.getGraduationYear())
                .phoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null)
                .activeBacklogs(request.getActiveBacklogs() != null ? request.getActiveBacklogs() : 0)
                .skills(request.getSkills())
                .address(request.getAddress())
                .build();

        Student savedStudent = studentRepository.save(student);
        log.info("Successfully created student profile with ID: {} for user: {}", savedStudent.getId(), userEmail);

        return mapToResponse(savedStudent);
    }

    @Transactional(readOnly = true)
    public StudentProfileResponse getCurrentStudentProfile(String userEmail) {
        Student student = studentRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + userEmail));
        return mapToResponse(student);
    }

    @Transactional(readOnly = true)
    public StudentProfileResponse getProfileById(Long id, String currentUserEmail) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentUserEmail));

        // If the authenticated user is a student, ensure they can only view their own profile
        if (currentUser.getRole() == UserRole.ROLE_STUDENT) {
            if (!student.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You are not authorized to view another student's profile");
            }
        }

        return mapToResponse(student);
    }

    @Transactional
    public StudentProfileResponse updateCurrentStudentProfile(StudentUpdateRequest request, String userEmail) {
        log.info("Updating student profile for user: {}", userEmail);

        Student student = studentRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + userEmail));

        if (request.getCgpa() != null) {
            validateCgpa(request.getCgpa());
            student.setCgpa(request.getCgpa());
        }

        if (request.getCurrentSemester() != null) {
            validateSemester(request.getCurrentSemester());
            student.setCurrentSemester(request.getCurrentSemester());
        }

        if (request.getGraduationYear() != null) {
            validateGraduationYear(request.getGraduationYear());
            student.setGraduationYear(request.getGraduationYear());
        }

        if (request.getDepartment() != null && !request.getDepartment().trim().isEmpty()) {
            student.setDepartment(request.getDepartment().trim());
        }

        if (request.getCourse() != null && !request.getCourse().trim().isEmpty()) {
            student.setCourse(request.getCourse().trim());
        }

        if (request.getPhoneNumber() != null) {
            student.setPhoneNumber(request.getPhoneNumber().trim());
        }

        if (request.getActiveBacklogs() != null) {
            if (request.getActiveBacklogs() < 0) {
                throw new IllegalArgumentException("Active backlogs cannot be negative");
            }
            student.setActiveBacklogs(request.getActiveBacklogs());
        }

        if (request.getSkills() != null) {
            student.setSkills(request.getSkills());
        }

        if (request.getAddress() != null) {
            student.setAddress(request.getAddress());
        }

        Student updatedStudent = studentRepository.save(student);
        log.info("Successfully updated student profile with ID: {}", updatedStudent.getId());

        return mapToResponse(updatedStudent);
    }

    @Transactional(readOnly = true)
    public List<StudentProfileResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void validateAcademicData(Double cgpa, Integer semester, Integer gradYear) {
        validateCgpa(cgpa);
        validateSemester(semester);
        validateGraduationYear(gradYear);
    }

    private void validateCgpa(Double cgpa) {
        if (cgpa == null || cgpa < 0.0 || cgpa > 10.0) {
            throw new IllegalArgumentException("CGPA must be between 0.0 and 10.0");
        }
    }

    private void validateSemester(Integer semester) {
        if (semester == null || semester < 1 || semester > 8) {
            throw new IllegalArgumentException("Current semester must be between 1 and 8");
        }
    }

    private void validateGraduationYear(Integer gradYear) {
        if (gradYear == null || gradYear < 2000 || gradYear > 2100) {
            throw new IllegalArgumentException("Graduation year must be valid (2000-2100)");
        }
    }

    public StudentProfileResponse mapToResponse(Student student) {
        return StudentProfileResponse.builder()
                .id(student.getId())
                .userId(student.getUser() != null ? student.getUser().getId() : null)
                .name(student.getUser() != null ? student.getUser().getName() : null)
                .email(student.getUser() != null ? student.getUser().getEmail() : null)
                .role(student.getUser() != null ? student.getUser().getRole() : null)
                .rollNumber(student.getRollNumber())
                .enrollmentNumber(student.getEnrollmentNumber())
                .department(student.getDepartment())
                .course(student.getCourse())
                .currentSemester(student.getCurrentSemester())
                .cgpa(student.getCgpa())
                .graduationYear(student.getGraduationYear())
                .phoneNumber(student.getPhoneNumber())
                .activeBacklogs(student.getActiveBacklogs())
                .skills(student.getSkills())
                .address(student.getAddress())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }
}
