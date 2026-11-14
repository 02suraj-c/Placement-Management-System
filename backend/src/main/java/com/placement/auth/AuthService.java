package com.placement.auth;

import com.placement.auth.dto.AuthResponse;
import com.placement.auth.dto.LoginRequest;
import com.placement.auth.dto.RegisterRequest;
import com.placement.auth.dto.UserResponse;
import com.placement.exception.ResourceNotFoundException;
import com.placement.security.JwtService;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
import com.placement.user.User;
import com.placement.user.UserRepository;
import com.placement.user.UserRole;
import com.placement.user.UserService;
import com.placement.util.YearUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final StudentRepository studentRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already in use: " + normalizedEmail);
        }

        User user = User.builder()
                .name(request.getName())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.ROLE_STUDENT)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        Student createdStudent = null;

        if (savedUser.getRole() == com.placement.user.UserRole.ROLE_STUDENT) {
            String roll = request.getRollNumber() != null && !request.getRollNumber().trim().isEmpty()
                    ? request.getRollNumber().trim()
                    : "ST" + (System.currentTimeMillis() % 100000);
            if (!studentRepository.existsByRollNumber(roll)) {
                int semester = request.getCurrentSemester() != null ? request.getCurrentSemester() : 7;
                Student student = Student.builder()
                        .user(savedUser)
                        .rollNumber(roll)
                        .enrollmentNumber(request.getEnrollmentNumber())
                        .department(request.getDepartment() != null && !request.getDepartment().trim().isEmpty() ? request.getDepartment().trim() : "Computer Science & Engineering")
                        .course(request.getCourse() != null && !request.getCourse().trim().isEmpty() ? request.getCourse().trim() : "B.Tech")
                        .currentSemester(semester)
                        .academicYear(YearUtils.calculateYearFromSemester(semester))
                        .cgpa(request.getCgpa() != null ? request.getCgpa() : 8.5)
                        .graduationYear(request.getGraduationYear() != null ? request.getGraduationYear() : 2026)
                        .phoneNumber(request.getPhoneNumber())
                        .activeBacklogs(request.getActiveBacklogs() != null ? request.getActiveBacklogs() : 0)
                        .skills(request.getSkills() != null ? request.getSkills() : "Java, Data Structures, Web Development")
                        .address(request.getAddress())
                        .build();
                createdStudent = studentRepository.save(student);
            }
        }

        String token = jwtService.generateToken(savedUser);

        AuthResponse.AuthResponseBuilder resp = AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole());

        if (createdStudent != null) {
            resp.year(createdStudent.getAcademicYear())
                .semester(createdStudent.getCurrentSemester())
                .rollNumber(createdStudent.getRollNumber())
                .department(createdStudent.getDepartment());
        }

        return resp.build();
    }

    public AuthResponse login(LoginRequest request) {
        String cleanEmail = normalizeEmail(request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(cleanEmail, request.getPassword())
        );

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", cleanEmail));

        String token = jwtService.generateToken(user);

        AuthResponse.AuthResponseBuilder resp = AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole());

        if (user.getRole() == com.placement.user.UserRole.ROLE_STUDENT) {
            studentRepository.findByUserEmail(cleanEmail).ifPresent(student -> {
                resp.year(student.getAcademicYear())
                    .semester(student.getCurrentSemester())
                    .rollNumber(student.getRollNumber())
                    .department(student.getDepartment());
            });
        }

        return resp.build();
    }

    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user found in session");
        }
        String email = normalizeEmail(authentication.getName());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        UserResponse response = userService.mapToResponse(user);
        if (user.getRole() == com.placement.user.UserRole.ROLE_STUDENT) {
            studentRepository.findByUserEmail(email).ifPresent(student -> {
                response.setYear(student.getAcademicYear());
                response.setSemester(student.getCurrentSemester());
                response.setRollNumber(student.getRollNumber());
                response.setDepartment(student.getDepartment());
            });
        }
        return response;
    }

    private String normalizeEmail(String email) {
        return email != null ? email.trim().toLowerCase(Locale.ROOT) : null;
    }
}
