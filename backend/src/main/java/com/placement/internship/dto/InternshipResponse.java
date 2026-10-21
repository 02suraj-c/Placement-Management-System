package com.placement.internship.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipResponse {

    private Long id;
    private String title;
    private String company;
    private String description;
    private String requiredSkills;
    private String location;
    private String stipend;
    private String duration;
    private String eligibilityCriteria;
    private LocalDate applicationDeadline;
    private String applicationUrl;
    private boolean active;
    private boolean expired;
    private String postedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
