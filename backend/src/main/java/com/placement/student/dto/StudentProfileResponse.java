package com.placement.student.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentProfileResponse {

    private Long id;
    private Long userId;
    private String name;
    private String email;
    private UserRole role;
    private String rollNumber;
    private String enrollmentNumber;
    private String department;
    private String course;
    private Integer currentSemester;
    private Double cgpa;
    private Integer graduationYear;
    private String phoneNumber;
    private Integer activeBacklogs;
    private String skills;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
