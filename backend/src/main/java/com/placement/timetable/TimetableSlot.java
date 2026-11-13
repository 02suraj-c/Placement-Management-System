package com.placement.timetable;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity representing an individual class slot in the academic timetable.
 */
@Entity
@Table(name = "timetable_slots", indexes = {
        @Index(name = "idx_tt_year_sem_div", columnList = "academic_year, semester, division"),
        @Index(name = "idx_tt_day", columnList = "day_of_week"),
        @Index(name = "idx_tt_faculty", columnList = "faculty_id"),
        @Index(name = "idx_tt_room", columnList = "room")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Academic year is required (1-4)")
    @Min(1) @Max(4)
    @Column(name = "academic_year", nullable = false)
    private Integer academicYear;

    @NotNull(message = "Semester is required (1-8)")
    @Min(1) @Max(8)
    @Column(name = "semester", nullable = false)
    private Integer semester;

    @NotBlank(message = "Division / Section is required")
    @Size(max = 20)
    @Column(name = "division", nullable = false, length = 20)
    private String division;

    @NotBlank(message = "Day is required (e.g. MONDAY)")
    @Column(name = "day_of_week", nullable = false, length = 20)
    private String day;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @NotBlank(message = "Subject is required")
    @Size(max = 150)
    @Column(name = "subject", nullable = false, length = 150)
    private String subject;

    @Column(name = "faculty_id")
    private Long facultyId;

    @Size(max = 100)
    @Column(name = "faculty_name", length = 100)
    private String facultyName;

    @Size(max = 50)
    @Column(name = "room", length = 50)
    private String room;

    @NotNull(message = "Slot type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private SlotType type = SlotType.LECTURE;

    @Builder.Default
    @Column(name = "published", nullable = false)
    private Boolean published = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.published == null) {
            this.published = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

