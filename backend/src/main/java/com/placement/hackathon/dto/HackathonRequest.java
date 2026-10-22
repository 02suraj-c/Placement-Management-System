package com.placement.hackathon.dto;

import com.placement.hackathon.HackathonMode;
import com.placement.hackathon.HackathonStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HackathonRequest {

    @NotBlank(message = "Hackathon title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Organizer name is required")
    @Size(max = 150, message = "Organizer must not exceed 150 characters")
    private String organizer;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Registration deadline is required")
    private LocalDate registrationDeadline;

    @NotNull(message = "Hackathon mode is required (ONLINE, OFFLINE, HYBRID)")
    private HackathonMode mode;

    @Size(max = 150, message = "Location must not exceed 150 characters")
    private String location;

    @Size(max = 500, message = "Eligibility criteria must not exceed 500 characters")
    private String eligibilityCriteria;

    @Size(max = 255, message = "Registration URL must not exceed 255 characters")
    private String registrationUrl;

    @Builder.Default
    private HackathonStatus status = HackathonStatus.UPCOMING;

    @Builder.Default
    private Boolean active = true;
}
