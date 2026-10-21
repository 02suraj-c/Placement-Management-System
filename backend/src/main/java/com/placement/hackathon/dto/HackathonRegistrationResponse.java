package com.placement.hackathon.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HackathonRegistrationResponse {

    private Long id;
    private Long hackathonId;
    private String hackathonTitle;
    private String organizer;
    private Long studentId;
    private String studentName;
    private String rollNumber;
    private LocalDateTime registeredAt;
}
