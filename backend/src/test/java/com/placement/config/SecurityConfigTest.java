package com.placement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.admin.AdminService;
import com.placement.auth.AuthService;
import com.placement.auth.dto.AuthResponse;
import com.placement.auth.dto.LoginRequest;
import com.placement.auth.dto.RegisterRequest;
import com.placement.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("SecurityConfig Request Matcher and Authorization Tests")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private AdminService adminService;

    @Test
    @DisplayName("Public access: POST /api/auth/register is permitted without authentication")
    void register_publicPost_permitted() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Test User")
                .email("test.register@college.edu")
                .password("password123")
                .role(UserRole.ROLE_STUDENT)
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt.token.register")
                .email("test.register@college.edu")
                .role(UserRole.ROLE_STUDENT)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Public access: POST /api/auth/login is permitted without authentication")
    void login_publicPost_permitted() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("test.login@college.edu")
                .password("password123")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt.token.login")
                .email("test.login@college.edu")
                .role(UserRole.ROLE_STUDENT)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Method restriction: GET /api/auth/register requires authentication (returns 401 or 403)")
    void register_nonPost_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/register"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Protected endpoint: GET /api/auth/me requires authentication (returns 401 or 403 when unauthenticated)")
    void getMe_unauthenticated_forbidden() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Protected endpoint: POST /api/auth/logout requires authentication (returns 401 or 403 when unauthenticated)")
    void logout_unauthenticated_forbidden() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Public access: GET /actuator/health is permitted without authentication")
    void actuatorHealth_get_permitted() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Public access: HEAD /actuator/health is permitted (preserves Docker healthcheck wget --spider)")
    void actuatorHealth_head_permitted() throws Exception {
        mockMvc.perform(head("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Protected actuator endpoints: GET /actuator/beans requires authentication")
    void actuatorOther_unauthenticated_forbidden() throws Exception {
        mockMvc.perform(get("/actuator/beans"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin endpoint: GET /api/admin/users unauthenticated is forbidden (403/401)")
    void adminUsers_unauthenticated_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin endpoint: Non-admin user (ROLE_STUDENT) cannot access /api/admin/users")
    @WithMockUser(roles = "STUDENT")
    void adminUsers_authenticatedAsStudent_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin endpoint: Admin user (ROLE_ADMIN) can access /api/admin/users")
    @WithMockUser(roles = "ADMIN")
    void adminUsers_authenticatedAsAdmin_permitted() throws Exception {
        when(adminService.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk());
    }
}

