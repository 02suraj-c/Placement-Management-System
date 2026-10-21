package com.placement.admin;

import com.placement.auth.dto.UserResponse;
import com.placement.dto.ApiResponse;
import com.placement.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @RequestParam(required = false) UserRole role
    ) {
        List<UserResponse> users = (role != null) 
                ? adminService.getUsersByRole(role) 
                : adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserById(id), "User retrieved successfully"));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long id,
            @RequestParam UserRole role
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateUserRole(id, role), "User role updated successfully"));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminService.toggleUserStatus(id, enabled), "User status updated successfully"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", "Operation completed"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemStats() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getSystemStats(), "System stats fetched successfully"));
    }
}
