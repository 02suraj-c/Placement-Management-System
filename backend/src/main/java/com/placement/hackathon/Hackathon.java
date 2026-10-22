package com.placement.hackathon;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a Hackathon event.
 */
@Entity
@Table(
        name = "hackathons",
        indexes = {
                @Index(name = "idx_hackathon_status", columnList = "status"),
                @Index(name = "idx_hackathon_active", columnList = "active"),
                @Index(name = "idx_hackathon_reg_deadline", columnList = "registration_deadline"),
                @Index(name = "idx_hackathon_start_date", columnList = "start_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hackathon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "organizer", nullable = false, length = 150)
    private String organizer;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "registration_deadline", nullable = false)
    private LocalDate registrationDeadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    private HackathonMode mode;

    @Column(name = "location", length = 150)
    private String location;

    @Column(name = "eligibility_criteria", length = 500)
    private String eligibilityCriteria;

    @Column(name = "registration_url", length = 255)
    private String registrationUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private HackathonStatus status = HackathonStatus.UPCOMING;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "posted_by", length = 100)
    private String postedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
