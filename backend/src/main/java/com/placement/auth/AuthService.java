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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            // SECURITY: Do not reveal which email is registered — prevents user enumeration
            throw new IllegalArgumentException("Registration failed. Please check your details and try again.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                // SECURITY: Ignore role from request — all self-registrations default to STUDENT.
                // Role elevation must go through the /api/admin/users/{id}/role endpoint.
                .role(UserRole.ROLE_STUDENT)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        // Auto-initialize student profile with user's name and graduation year
        Integer gradYear = request.getGraduationYear() != null ? request.getGraduationYear() : 2026;
        String defaultRoll = "CS" + gradYear + "-" + String.format("%03d", savedUser.getId());
        Student student = Student.builder()
                .user(savedUser)
                .rollNumber(defaultRoll)
                .enrollmentNumber("EN" + (gradYear - 4) + "-" + String.format("%04d", savedUser.getId()))
                .department("Computer Science & Engineering")
                .course("B.Tech")
                .currentSemester(6)
                .cgpa(8.0)
                .graduationYear(gradYear)
                .activeBacklogs(0)
                .skills("Java, Spring Boot, React.js, MySQL")
                .build();
        studentRepository.save(student);

        String token = jwtService.generateToken(savedUser);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        String cleanEmail = request.getEmail().trim().toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(cleanEmail, request.getPassword())
        );

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", cleanEmail));

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user found in session");
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userService.mapToResponse(user);
    }
}
