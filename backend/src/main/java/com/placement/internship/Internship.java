package com.placement.internship;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing an Internship opportunity posted for students.
 */
@Entity
@Table(
        name = "internships",
        indexes = {
                @Index(name = "idx_internship_company", columnList = "company"),
                @Index(name = "idx_internship_active", columnList = "active"),
                @Index(name = "idx_internship_deadline", columnList = "application_deadline")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Internship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "company", nullable = false, length = 150)
    private String company;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "required_skills", nullable = false, length = 500)
    private String requiredSkills;

    @Column(name = "location", nullable = false, length = 150)
    private String location;

    @Column(name = "stipend", length = 100)
    private String stipend;

    @Column(name = "duration", length = 100)
    private String duration;

    @Column(name = "eligibility_criteria", length = 500)
    private String eligibilityCriteria;

    @Column(name = "application_deadline", nullable = false)
    private LocalDate applicationDeadline;

    @Column(name = "application_url", length = 255)
    private String applicationUrl;

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
