package com.placement.student;

import com.placement.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @Column(name = "roll_number", nullable = false, unique = true, length = 50)
    private String rollNumber;

    @Column(name = "enrollment_number", unique = true, length = 50)
    private String enrollmentNumber;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "course", nullable = false, length = 100)
    private String course;

    @Column(name = "current_semester", nullable = false)
    private Integer currentSemester;

    @Column(name = "cgpa", nullable = false)
    private Double cgpa;

    @Column(name = "graduation_year", nullable = false)
    private Integer graduationYear;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Builder.Default
    @Column(name = "active_backlogs", nullable = false)
    private Integer activeBacklogs = 0;

    @Column(name = "skills", length = 1000)
    private String skills;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.activeBacklogs == null) {
            this.activeBacklogs = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
