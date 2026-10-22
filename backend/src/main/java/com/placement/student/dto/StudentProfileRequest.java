package com.placement.student.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileRequest {

    @NotBlank(message = "College roll number is required")
    @Size(max = 50, message = "Roll number must not exceed 50 characters")
    private String rollNumber;

    @Size(max = 50, message = "Enrollment number must not exceed 50 characters")
    private String enrollmentNumber;

    @NotBlank(message = "Department or branch is required")
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @NotBlank(message = "Course is required")
    @Size(max = 100, message = "Course must not exceed 100 characters")
    private String course;

    @NotNull(message = "Current semester is required")
    @Min(value = 1, message = "Current semester must be at least 1")
    @Max(value = 8, message = "Current semester cannot exceed 8")
    private Integer currentSemester;

    @NotNull(message = "CGPA is required")
    @DecimalMin(value = "0.0", message = "CGPA cannot be less than 0.0")
    @DecimalMax(value = "10.0", message = "CGPA cannot exceed 10.0")
    private Double cgpa;

    @NotNull(message = "Graduation year is required")
    @Min(value = 2000, message = "Graduation year must be valid (>= 2000)")
    @Max(value = 2100, message = "Graduation year must be valid (<= 2100)")
    private Integer graduationYear;

    @Pattern(regexp = "^[0-9+\\- ]{10,15}$", message = "Phone number must be between 10 and 15 digits")
    private String phoneNumber;

    @Min(value = 0, message = "Active backlogs cannot be negative")
    @Builder.Default
    private Integer activeBacklogs = 0;

    @Size(max = 1000, message = "Skills description must not exceed 1000 characters")
    private String skills;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;
}
