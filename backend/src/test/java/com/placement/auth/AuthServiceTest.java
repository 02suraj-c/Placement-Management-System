package com.placement.auth;

import com.placement.auth.dto.AuthResponse;
import com.placement.auth.dto.LoginRequest;
import com.placement.auth.dto.RegisterRequest;
import com.placement.auth.dto.UserResponse;
import com.placement.exception.ResourceNotFoundException;
import com.placement.security.JwtService;
import com.placement.user.User;
import com.placement.user.UserRepository;
import com.placement.user.UserRole;
import com.placement.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Captor
    private ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test.user@example.com")
                .password("$2a$10$encodedPasswordHash")
                .role(UserRole.ROLE_STUDENT)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Successful registration assigns ROLE_STUDENT")
    void register_successfulRegistration_assignsRoleStudent() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Aarav Sharma")
                .email("aarav@college.edu")
                .password("plainPassword123")
                .role(UserRole.ROLE_STUDENT)
                .build();

        when(userRepository.existsByEmail("aarav@college.edu")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword123")).thenReturn("$2a$10$encodedHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("mocked.jwt.token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(UserRole.ROLE_STUDENT, response.getRole());
        assertEquals("aarav@college.edu", response.getEmail());

        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(UserRole.ROLE_STUDENT, savedUser.getRole());
    }

    @Test
    @DisplayName("A registration request containing ROLE_ADMIN still creates a student account")
    void register_requestContainingRoleAdmin_createsStudentAccount() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Malicious Admin Attempt")
                .email("admin.attempt@college.edu")
                .password("adminSecretPass")
                .role(UserRole.ROLE_ADMIN)
                .build();

        when(userRepository.existsByEmail("admin.attempt@college.edu")).thenReturn(false);
        when(passwordEncoder.encode("adminSecretPass")).thenReturn("$2a$10$encodedAdminPass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(11L);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("mocked.admin.token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(UserRole.ROLE_STUDENT, response.getRole());
        assertNotEquals(UserRole.ROLE_ADMIN, response.getRole());

        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(UserRole.ROLE_STUDENT, savedUser.getRole(),
                "Public registration must always assign ROLE_STUDENT regardless of requested role");
        assertNotEquals(UserRole.ROLE_ADMIN, savedUser.getRole());
    }

    @Test
    @DisplayName("A registration request containing ROLE_OFFICER or ROLE_HOD still creates a student account")
    void register_requestContainingPrivilegedRoles_createsStudentAccount() {
        for (UserRole elevatedRole : new UserRole[]{UserRole.ROLE_OFFICER, UserRole.ROLE_HOD, UserRole.ROLE_PLACEMENT_OFFICER, UserRole.ROLE_COMPANY}) {
            RegisterRequest request = RegisterRequest.builder()
                    .name("Privileged Candidate")
                    .email(elevatedRole.name().toLowerCase() + "@college.edu")
                    .password("secretPassword")
                    .role(elevatedRole)
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPass");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(jwtService.generateToken(any(User.class))).thenReturn("mocked.token");

            AuthResponse response = authService.register(request);

            assertEquals(UserRole.ROLE_STUDENT, response.getRole());
            verify(userRepository, atLeastOnce()).save(userCaptor.capture());
            assertEquals(UserRole.ROLE_STUDENT, userCaptor.getValue().getRole());
        }
    }

    @Test
    @DisplayName("Email is trimmed and converted to lowercase")
    void register_emailTrimmedAndConvertedToLowercase() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Whitespace User")
                .email("   Padded.Email.User@College.EDU   ")
                .password("securePassword")
                .role(UserRole.ROLE_STUDENT)
                .build();

        String expectedNormalizedEmail = "padded.email.user@college.edu";

        when(userRepository.existsByEmail(expectedNormalizedEmail)).thenReturn(false);
        when(passwordEncoder.encode("securePassword")).thenReturn("$2a$10$encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(12L);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("mocked.jwt.token");

        AuthResponse response = authService.register(request);

        // Verification: existsByEmail must be called with the normalized email
        verify(userRepository, times(1)).existsByEmail(expectedNormalizedEmail);
        verify(userRepository, never()).existsByEmail("   Padded.Email.User@College.EDU   ");

        // Verification: User saved with normalized email
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(expectedNormalizedEmail, savedUser.getEmail());

        // Verification: Response contains normalized email
        assertEquals(expectedNormalizedEmail, response.getEmail());
    }

    @Test
    @DisplayName("Duplicate normalized email is rejected")
    void register_duplicateNormalizedEmail_isRejected() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Duplicate Candidate")
                .email("   Test.User@Example.COM   ")
                .password("plainPassword123")
                .role(UserRole.ROLE_STUDENT)
                .build();

        String normalizedEmail = "test.user@example.com";
        when(userRepository.existsByEmail(normalizedEmail)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                authService.register(request)
        );

        assertTrue(ex.getMessage().contains("Email already in use: test.user@example.com"));

        // Ensure user is never persisted and JWT is never generated
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Password is encoded before saving")
    void register_passwordIsEncodedBeforeSaving() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Plain Password User")
                .email("plain.password@college.edu")
                .password("RawPlainTextPassword!@#")
                .role(UserRole.ROLE_STUDENT)
                .build();

        when(userRepository.existsByEmail("plain.password@college.edu")).thenReturn(false);
        when(passwordEncoder.encode("RawPlainTextPassword!@#")).thenReturn("$2a$10$encodedSecureHashValue");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(14L);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("mocked.jwt.token");

        authService.register(request);

        verify(passwordEncoder, times(1)).encode("RawPlainTextPassword!@#");
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("$2a$10$encodedSecureHashValue", savedUser.getPassword());
        assertNotEquals("RawPlainTextPassword!@#", savedUser.getPassword());
    }

    @Test
    @DisplayName("JWT token is generated after successful registration")
    void register_jwtTokenGeneratedAfterSuccessfulRegistration() {
        RegisterRequest request = RegisterRequest.builder()
                .name("JWT Test User")
                .email("jwt.user@college.edu")
                .password("password123")
                .role(UserRole.ROLE_STUDENT)
                .build();

        User persistedUser = User.builder()
                .id(20L)
                .name("JWT Test User")
                .email("jwt.user@college.edu")
                .password("$2a$10$hash")
                .role(UserRole.ROLE_STUDENT)
                .enabled(true)
                .build();

        when(userRepository.existsByEmail("jwt.user@college.edu")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hash");
        when(userRepository.save(any(User.class))).thenReturn(persistedUser);
        when(jwtService.generateToken(persistedUser)).thenReturn("header.payload.signature");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("header.payload.signature", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(20L, response.getId());
        assertEquals("JWT Test User", response.getName());
        assertEquals("jwt.user@college.edu", response.getEmail());
        assertEquals(UserRole.ROLE_STUDENT, response.getRole());

        verify(jwtService, times(1)).generateToken(persistedUser);
    }

    @Test
    @DisplayName("Login works with uppercase or whitespace-padded email")
    void login_worksWithUppercaseOrWhitespacePaddedEmail() {
        LoginRequest request = LoginRequest.builder()
                .email("   Test.User@Example.COM   ")
                .password("plainPassword123")
                .build();

        String expectedCleanEmail = "test.user@example.com";

        when(userRepository.findByEmail(expectedCleanEmail)).thenReturn(Optional.of(sampleUser));
        when(jwtService.generateToken(sampleUser)).thenReturn("login.jwt.token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("login.jwt.token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(sampleUser.getId(), response.getId());
        assertEquals(sampleUser.getEmail(), response.getEmail());
        assertEquals(sampleUser.getRole(), response.getRole());

        // Verify authenticationManager was called with normalized email
        verify(authenticationManager, times(1)).authenticate(authCaptor.capture());
        UsernamePasswordAuthenticationToken authToken = authCaptor.getValue();
        assertEquals(expectedCleanEmail, authToken.getPrincipal());
        assertEquals("plainPassword123", authToken.getCredentials());

        // Verify findByEmail was called with normalized email
        verify(userRepository, times(1)).findByEmail(expectedCleanEmail);
    }

    @Test
    @DisplayName("Login fails with ResourceNotFoundException when user does not exist")
    void login_userNotFound_throwsResourceNotFoundException() {
        LoginRequest request = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                authService.login(request)
        );

        assertTrue(ex.getMessage().contains("User not found with email : 'nonexistent@example.com'"));
    }

    @Test
    @DisplayName("getCurrentUser returns authenticated user profile")
    void getCurrentUser_authenticated_returnsUserProfile() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test.user@example.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UserResponse expectedResponse = UserResponse.builder()
                .id(1L)
                .name("Test User")
                .email("test.user@example.com")
                .role(UserRole.ROLE_STUDENT)
                .enabled(true)
                .createdAt(sampleUser.getCreatedAt())
                .build();

        when(userRepository.findByEmail("test.user@example.com")).thenReturn(Optional.of(sampleUser));
        when(userService.mapToResponse(sampleUser)).thenReturn(expectedResponse);

        UserResponse response = authService.getCurrentUser();

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test User", response.getName());
        assertEquals("test.user@example.com", response.getEmail());
        assertEquals(UserRole.ROLE_STUDENT, response.getRole());
        assertTrue(response.isEnabled());

        verify(userRepository, times(1)).findByEmail("test.user@example.com");
        verify(userService, times(1)).mapToResponse(sampleUser);
    }

    @Test
    @DisplayName("getCurrentUser throws ResourceNotFoundException when unauthenticated")
    void getCurrentUser_unauthenticated_throwsResourceNotFoundException() {
        SecurityContextHolder.clearContext();

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                authService.getCurrentUser()
        );

        assertTrue(ex.getMessage().contains("No authenticated user found in session"));
    }
}

