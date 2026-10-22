package com.placement.attendance;

import com.placement.student.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "crt_attendance",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_id", "session_name", "session_date"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CRTAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "session_name", nullable = false, length = 150)
    private String sessionName;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "present", nullable = false)
    private boolean present;

    @Column(name = "trainer_name", length = 100)
    private String trainerName;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "recorded_by", length = 100)
    private String recordedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
