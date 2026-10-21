package com.placement.ai.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Response DTO returned by PreparationPlanner.
 * Contains a full day-by-day schedule broken into weeks,
 * a summary, and per-skill coverage metadata.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreparationPlanResponse {

    // ── High-level meta ────────────────────────────────────────────────────────

    /** Student's target role (echoed back). */
    private String targetRole;

    /** Target company (echoed back, may be null). */
    private String targetCompany;

    /** The date of the placement drive. */
    private LocalDate driveDate;

    /** Today's date when the plan was generated. */
    private LocalDate planGeneratedOn;

    /** Total days available until the drive (driveDate - planGeneratedOn). */
    private int totalDaysAvailable;

    /**
     * Short executive summary of the preparation strategy
     * (2-3 sentences from the AI).
     */
    private String summary;

    // ── Skill gap input (echoed) ───────────────────────────────────────────────

    /**
     * Skills identified as gaps (student is weak / missing).
     * Provided by the caller; echoed back for transparency.
     */
    @Builder.Default
    private List<String> skillGaps = List.of();

    /**
     * Skills the student already has (used by AI to avoid over-recommending).
     */
    @Builder.Default
    private List<String> existingSkills = List.of();

    // ── Structured plan ───────────────────────────────────────────────────────

    /**
     * Weekly plan list, ordered from week 1 → last week.
     * Each week contains its daily tasks.
     */
    @Builder.Default
    private List<WeekPlan> weeklyPlan = List.of();

    /**
     * Quick-reference daily schedule for today + next 3 days
     * (derived from weeklyPlan for convenience).
     */
    @Builder.Default
    private List<DayTask> upcomingDays = List.of();

    // ── Skill coverage ────────────────────────────────────────────────────────

    /**
     * Map of skill → week number(s) where it is addressed.
     * e.g., { "Java": [1, 2], "System Design": [3, 4] }
     */
    @Builder.Default
    private Map<String, List<Integer>> skillCoverageByWeek = Map.of();

    /**
     * Recommended resources per skill gap.
     * e.g., { "System Design": ["Grokking the System Design Interview", ...] }
     */
    @Builder.Default
    private Map<String, List<String>> resourcesPerSkill = Map.of();

    // ── Revision & mock schedule ──────────────────────────────────────────────

    /**
     * List of recommended mock interview dates (subset of the drive window).
     */
    @Builder.Default
    private List<LocalDate> mockInterviewDates = List.of();

    /**
     * Revision milestones (e.g., "Complete DSA by Day 14").
     */
    @Builder.Default
    private List<String> milestones = List.of();

    // ── Warning ───────────────────────────────────────────────────────────────

    /**
     * Non-null when the available time is very short (< 3 days) or
     * the skill gap list is unusually large.
     */
    private String warning;

    // ── Nested types ──────────────────────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class WeekPlan {
        private int weekNumber;
        private String weekTheme;          // e.g. "Core Java + Data Structures"
        private List<DayTask> days;
        private List<String> weekGoals;   // 2-3 goals for this week
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DayTask {
        private int dayNumber;            // 1-based from plan start
        private LocalDate date;
        private String focus;             // primary topic for the day
        private List<String> tasks;       // 3-5 concrete tasks
        private int studyHours;           // recommended study hours
        private String practiceType;      // "CODING" | "READING" | "MOCK" | "REVISION"
    }
}
