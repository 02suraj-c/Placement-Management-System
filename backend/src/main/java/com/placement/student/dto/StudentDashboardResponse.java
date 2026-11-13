package com.placement.student.dto;

import com.placement.application.ApplicationStatus;
import com.placement.interview.InterviewRound;
import com.placement.notification.SystemNotification;
import com.placement.reminder.UpcomingDeadlineDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * One-call student dashboard snapshot.
 *
 * Returned by GET /api/students/dashboard and aggregates all relevant data
 * a student needs on their home screen into a single response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardResponse {

    // ── Profile ──────────────────────────────────────────────────────────
    /** Full student profile. */
    private StudentProfileResponse studentProfile;

    // ── Applications ─────────────────────────────────────────────────────
    /** Total number of placement applications submitted. */
    private int totalApplications;

    /**
     * Count of applications grouped by {@link ApplicationStatus} name.
     * e.g. {"APPLIED": 2, "SHORTLISTED": 1, "SELECTED": 1}
     */
    private Map<String, Long> applicationsByStatus;

    // ── Interviews ────────────────────────────────────────────────────────
    /** Upcoming interview rounds (status = SCHEDULED), ordered by date asc. */
    private List<InterviewRound> upcomingInterviews;

    /** Total number of interview rounds across all applications. */
    private int totalInterviews;

    // ── Notifications ─────────────────────────────────────────────────────
    /** Five most recent notifications. */
    private List<SystemNotification> recentNotifications;

    /** Number of unread notifications. */
    private long unreadNotificationsCount;

    // ── Hackathons ────────────────────────────────────────────────────────
    /** Number of hackathons the student has registered for. */
    private long hackathonRegistrationsCount;

    // ── Deadlines ─────────────────────────────────────────────────────────
    /**
     * Upcoming placement-drive and hackathon deadlines closing within 7 days.
     * Sorted by deadline date ascending.
     */
    private List<UpcomingDeadlineDto> upcomingDeadlines;

    // ── Eligibility snapshot ──────────────────────────────────────────────
    /** Number of ACTIVE/UPCOMING placement drives this student is eligible for. */
    private long eligibleDrivesCount;
}

