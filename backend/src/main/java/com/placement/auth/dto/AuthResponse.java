package com.placement.auth.dto;

import com.placement.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private Long id;
    private String name;
    private String email;
    private UserRole role;

    // Student academic session fields
    private Integer year;
    private Integer semester;
    private String rollNumber;
    private String department;
}
