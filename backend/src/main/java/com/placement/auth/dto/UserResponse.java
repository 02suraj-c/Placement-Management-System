package com.placement.auth.dto;

import com.placement.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private boolean enabled;
    private LocalDateTime createdAt;

    // Student profile session fields
    private Integer year;
    private Integer semester;
    private String rollNumber;
    private String department;
}
