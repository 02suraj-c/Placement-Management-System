package com.placement.admin;

import com.placement.auth.dto.UserResponse;
import com.placement.user.UserRepository;
import com.placement.user.UserRole;
import com.placement.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserService userService;
    private final UserRepository userRepository;

    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    public List<UserResponse> getUsersByRole(UserRole role) {
        return userService.getUsersByRole(role);
    }

    public UserResponse getUserById(Long id) {
        return userService.getUserById(id);
    }

    public UserResponse updateUserRole(Long id, UserRole role) {
        return userService.updateUserRole(id, role);
    }

    public UserResponse toggleUserStatus(Long id, boolean enabled) {
        return userService.toggleUserStatus(id, enabled);
    }

    public void deleteUser(Long id) {
        userService.deleteUser(id);
    }

    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("students", userRepository.countByRole(UserRole.ROLE_STUDENT));
        stats.put("faculty", userRepository.countByRole(UserRole.ROLE_FACULTY));
        stats.put("hod", userRepository.countByRole(UserRole.ROLE_HOD));
        stats.put("officers", userRepository.countByRole(UserRole.ROLE_OFFICER) + userRepository.countByRole(UserRole.ROLE_PLACEMENT_OFFICER));
        stats.put("companies", userRepository.countByRole(UserRole.ROLE_COMPANY));
        stats.put("admins", userRepository.countByRole(UserRole.ROLE_ADMIN));
        return stats;
    }
}
