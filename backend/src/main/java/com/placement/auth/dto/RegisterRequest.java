package com.placement.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    // SECURITY: Minimum 8 chars; must contain letter + digit or symbol
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*[\\d\\W]).{8,}$",
        message = "Password must contain at least one letter and one digit or special character"
    )
    private String password;

    // SECURITY: Role is intentionally removed from this DTO.
    // All self-registrations default to ROLE_STUDENT on the server side.
    // Admins can promote users via /api/admin/users/{id}/role.

    private Integer graduationYear;
}
