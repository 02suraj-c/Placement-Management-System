package com.placement.hackathon.dto;

import com.placement.hackathon.HackathonMode;
import com.placement.hackathon.HackathonStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HackathonResponse {

    private Long id;
    private String title;
    private String organizer;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate registrationDeadline;
    private HackathonMode mode;
    private String location;
    private String eligibilityCriteria;
    private String registrationUrl;
    private HackathonStatus status;
    private boolean active;
    private boolean registrationOpen;
    private long registeredCount;
    private String postedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
