package com.placement.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.placement.ai.dto.PreparationPlanRequest;
import com.placement.ai.dto.PreparationPlanResponse;
import com.placement.ai.dto.PreparationPlanResponse.DayTask;
import com.placement.ai.dto.PreparationPlanResponse.WeekPlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates a personalised day-by-day preparation plan from skill gaps
 * and a placement drive date.
 *
 * Flow:
 *  1. Compute available days; emit warning if < 3 days or > 90 days.
 *  2. Build AI prompt with schedule constraints.
 *  3. Call {@link AIService} → structured JSON plan.
 *  4. Enrich the AI response: attach actual {@link LocalDate} objects to
 *     each DayTask, derive {@code upcomingDays}, compute mock-interview dates.
 *  5. Return fully-populated {@link PreparationPlanResponse}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PreparationPlanner {

    private final AIService aiService;

    // AI plans capped at 90 days (3 months) to keep prompt + response manageable
    private static final int MAX_PLAN_DAYS = 90;
    private static final int UPCOMING_DAYS_COUNT = 4;  // today + next 3

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Generate a preparation plan for the given request.
     *
     * @param request validated plan request
     * @return full preparation plan response
     */
    public PreparationPlanResponse plan(PreparationPlanRequest request) {
        LocalDate today     = LocalDate.now();
        LocalDate driveDate = request.getDriveDate();
        long totalDays      = ChronoUnit.DAYS.between(today, driveDate);

        log.info("Generating prep plan: role='{}', driveDate={}, days={}, gaps={}",
                request.getTargetRole(), driveDate, totalDays, request.getSkillGaps());

        // ── Warn on edge cases ────────────────────────────────────────────────
        String warning = buildWarning(totalDays, request.getSkillGaps().size());

        // Cap planning horizon
        int planDays = (int) Math.min(totalDays, MAX_PLAN_DAYS);

        // ── Call AI ───────────────────────────────────────────────────────────
        String systemPrompt = buildSystemPrompt(planDays, request.getDailyStudyHours());
        String userPrompt   = buildUserPrompt(request, planDays, today);

        AiPlanResult aiResult = aiService.call(systemPrompt, userPrompt,
                AiPlanResult.class);

        // ── Build response ────────────────────────────────────────────────────
        List<WeekPlan> weeklyPlan = buildWeeklyPlan(aiResult, today);
        List<DayTask>  allDays    = weeklyPlan.stream()
                .flatMap(w -> w.getDays().stream())
                .toList();

        List<DayTask> upcomingDays = allDays.stream()
                .limit(UPCOMING_DAYS_COUNT)
                .toList();

        List<LocalDate> mockDates = deriveMockDates(today, driveDate, planDays);

        return PreparationPlanResponse.builder()
                .targetRole(request.getTargetRole())
                .targetCompany(request.getTargetCompany())
                .driveDate(driveDate)
                .planGeneratedOn(today)
                .totalDaysAvailable((int) totalDays)
                .summary(nullSafe(aiResult.summary()))
                .skillGaps(request.getSkillGaps())
                .existingSkills(request.getExistingSkills())
                .weeklyPlan(weeklyPlan)
                .upcomingDays(upcomingDays)
                .skillCoverageByWeek(nullSafeMap(aiResult.skillCoverageByWeek()))
                .resourcesPerSkill(nullSafeMap(aiResult.resourcesPerSkill()))
                .mockInterviewDates(mockDates)
                .milestones(nullSafeList(aiResult.milestones()))
                .warning(warning)
                .build();
    }

    // ── Prompt builders ────────────────────────────────────────────────────────

    private static String buildSystemPrompt(int planDays, int dailyHours) {
        return """
            You are an expert placement preparation coach for engineering college students.

            Create a detailed %d-day preparation plan. The student can study %d hours/day.

            Return ONLY a single valid JSON object with this exact schema — no markdown, no prose:

            {
              "summary": "<2-3 sentence strategy overview>",
              "weeks": [
                {
                  "weekNumber": <1, 2, ...>,
                  "weekTheme":  "<theme for the week e.g. 'Core Java + Arrays'>",
                  "weekGoals":  ["<goal1>", "<goal2>", "<goal3>"],
                  "days": [
                    {
                      "dayNumber":    <1-based integer>,
                      "focus":        "<primary topic for the day>",
                      "tasks":        ["<task1>", "<task2>", "<task3>", "<task4>"],
                      "studyHours":   <integer matching daily study hours>,
                      "practiceType": "<CODING|READING|MOCK|REVISION>"
                    }
                  ]
                }
              ],
              "skillCoverageByWeek": {
                "<skill>": [<weekNumbers where addressed>]
              },
              "resourcesPerSkill": {
                "<skill>": ["<resource1>", "<resource2>"]
              },
              "milestones": [
                "<milestone1 e.g. 'Complete Core Java by Day 7'>",
                "<milestone2>",
                "<milestone3>"
              ]
            }

            Rules:
            - Cover every skill gap with at least one dedicated day.
            - Alternate heavy topics with lighter revision days.
            - Last 20%% of days should be mock interviews and full revision.
            - Each day must have exactly 4 tasks (not more, not less).
            - practiceType must be one of: CODING, READING, MOCK, REVISION.
            - milestones: 3-6 items, ordered chronologically.
            """.formatted(planDays, dailyHours);
    }

    private static String buildUserPrompt(PreparationPlanRequest req,
                                          int planDays,
                                          LocalDate today) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== STUDENT PROFILE ===\n");
        sb.append("Target Role      : ").append(req.getTargetRole()).append('\n');

        if (req.getTargetCompany() != null && !req.getTargetCompany().isBlank()) {
            sb.append("Target Company   : ").append(req.getTargetCompany()).append('\n');
        }
        sb.append("Plan Start Date  : ").append(today).append('\n');
        sb.append("Drive Date       : ").append(req.getDriveDate()).append('\n');
        sb.append("Days Available   : ").append(planDays).append('\n');
        sb.append("Daily Study Hours: ").append(req.getDailyStudyHours()).append('\n');
        sb.append("\n=== SKILL GAPS (must cover all) ===\n");
        req.getSkillGaps().forEach(s -> sb.append("  - ").append(s).append('\n'));

        if (!req.getExistingSkills().isEmpty()) {
            sb.append("\n=== EXISTING SKILLS (do NOT re-teach basics) ===\n");
            req.getExistingSkills().forEach(s -> sb.append("  - ").append(s).append('\n'));
        }
        sb.append("\nGenerate a ").append(planDays)
          .append("-day plan covering all skill gaps. Return only the JSON object.");
        return sb.toString();
    }

    // ── Response builders ──────────────────────────────────────────────────────

    private static List<WeekPlan> buildWeeklyPlan(AiPlanResult ai, LocalDate startDate) {
        if (ai.weeks() == null) return List.of();

        return ai.weeks().stream().map(w -> {
            List<DayTask> days = (w.days() != null ? w.days() : List.<AiDay>of())
                    .stream()
                    .map(d -> DayTask.builder()
                            .dayNumber(d.dayNumber())
                            .date(startDate.plusDays(d.dayNumber() - 1L))
                            .focus(nullSafe(d.focus()))
                            .tasks(nullSafeList(d.tasks()))
                            .studyHours(d.studyHours() > 0 ? d.studyHours() : 4)
                            .practiceType(validPracticeType(d.practiceType()))
                            .build())
                    .toList();

            return WeekPlan.builder()
                    .weekNumber(w.weekNumber())
                    .weekTheme(nullSafe(w.weekTheme()))
                    .weekGoals(nullSafeList(w.weekGoals()))
                    .days(days)
                    .build();
        }).toList();
    }

    /**
     * Places mock interview dates at 50%, 75%, and 90% of the available days.
     * Avoids the last 2 days (keep those for rest/travel).
     */
    private static List<LocalDate> deriveMockDates(LocalDate today,
                                                    LocalDate driveDate,
                                                    int planDays) {
        if (planDays < 7) return List.of();   // too short for mocks
        List<LocalDate> mocks = new ArrayList<>();
        int[] percentages = {50, 75, 90};
        for (int pct : percentages) {
            int dayOffset = Math.max(1, (planDays * pct) / 100);
            LocalDate mockDate = today.plusDays(dayOffset);
            if (mockDate.isBefore(driveDate.minusDays(1))) {
                mocks.add(mockDate);
            }
        }
        return Collections.unmodifiableList(mocks);
    }

    // ── Validation ─────────────────────────────────────────────────────────────

    private static String buildWarning(long totalDays, int gapCount) {
        if (totalDays < 3) {
            return "Only " + totalDays + " day(s) until the drive — " +
                   "focus on revision and mock interviews only.";
        }
        if (totalDays > MAX_PLAN_DAYS) {
            return "Drive is more than " + MAX_PLAN_DAYS + " days away. " +
                   "Plan covers first " + MAX_PLAN_DAYS + " days; revisit closer to the drive.";
        }
        if (gapCount > 10) {
            return "Large number of skill gaps (" + gapCount + "). " +
                   "Consider prioritising the top 5-6 most critical skills first.";
        }
        return null;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static String validPracticeType(String raw) {
        if (raw == null) return "READING";
        return switch (raw.toUpperCase()) {
            case "CODING", "READING", "MOCK", "REVISION" -> raw.toUpperCase();
            default -> "READING";
        };
    }

    private static String nullSafe(String s) { return s != null ? s : ""; }

    private static <T> List<T> nullSafeList(List<T> l) { return l != null ? l : List.of(); }

    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> nullSafeMap(Map<K, V> m) { return m != null ? m : Map.of(); }

    // ── Internal AI schema ─────────────────────────────────────────────────────

    record AiPlanResult(
            @JsonProperty("summary")            String                    summary,
            @JsonProperty("weeks")              List<AiWeek>              weeks,
            @JsonProperty("skillCoverageByWeek") Map<String, List<Integer>> skillCoverageByWeek,
            @JsonProperty("resourcesPerSkill")  Map<String, List<String>> resourcesPerSkill,
            @JsonProperty("milestones")         List<String>              milestones
    ) {}

    record AiWeek(
            @JsonProperty("weekNumber") int          weekNumber,
            @JsonProperty("weekTheme")  String       weekTheme,
            @JsonProperty("weekGoals")  List<String> weekGoals,
            @JsonProperty("days")       List<AiDay>  days
    ) {}

    record AiDay(
            @JsonProperty("dayNumber")    int          dayNumber,
            @JsonProperty("focus")        String       focus,
            @JsonProperty("tasks")        List<String> tasks,
            @JsonProperty("studyHours")   int          studyHours,
            @JsonProperty("practiceType") String       practiceType
    ) {}
}
