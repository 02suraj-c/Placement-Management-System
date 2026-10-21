package com.placement.internship.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipRequest {

    @NotBlank(message = "Internship title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Company name is required")
    @Size(max = 150, message = "Company name must not exceed 150 characters")
    private String company;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Required skills are required")
    @Size(max = 500, message = "Required skills must not exceed 500 characters")
    private String requiredSkills;

    @NotBlank(message = "Location is required")
    @Size(max = 150, message = "Location must not exceed 150 characters")
    private String location;

    @Size(max = 100, message = "Stipend must not exceed 100 characters")
    private String stipend;

    @Size(max = 100, message = "Duration must not exceed 100 characters")
    private String duration;

    @Size(max = 500, message = "Eligibility criteria must not exceed 500 characters")
    private String eligibilityCriteria;

    @NotNull(message = "Application deadline is required")
    @FutureOrPresent(message = "Application deadline must be today or a future date")
    private LocalDate applicationDeadline;

    @Size(max = 255, message = "Application URL must not exceed 255 characters")
    private String applicationUrl;

    @Builder.Default
    private Boolean active = true;
}
