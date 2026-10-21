package com.placement.ai;

import com.placement.ai.dto.PreparationPlanRequest;
import com.placement.ai.dto.PreparationPlanResponse;
import com.placement.ai.exception.AiServiceException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PreparationPlanner.
 * AIService is fully mocked — no real HTTP or date side-effects.
 *
 * TC-PP-01  Normal 14-day plan → weekly plan built, mock dates derived
 * TC-PP-02  Drive in 2 days    → warning emitted, plan still returned
 * TC-PP-03  Drive > 90 days    → plan capped at 90, warning emitted
 * TC-PP-04  >10 skill gaps     → large-gap warning attached
 * TC-PP-05  AI timeout         → AiServiceException(TIMEOUT) propagated
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PreparationPlanner Unit Tests")
class PreparationPlannerTest {

    @Mock private AIService aiService;

    @InjectMocks private PreparationPlanner planner;

    // ── Helpers ────────────────────────────────────────────────────────────────

    private PreparationPlanRequest requestWithDays(int daysFromNow, List<String> gaps) {
        return PreparationPlanRequest.builder()
                .targetRole("Software Engineer")
                .targetCompany("Amazon")
                .driveDate(LocalDate.now().plusDays(daysFromNow))
                .skillGaps(gaps)
                .existingSkills(List.of("Python", "SQL"))
                .dailyStudyHours(5)
                .build();
    }

    /**
     * Returns a minimal AiPlanResult-equivalent Map that the service can
     * process (AIService is mocked, so we skip actual JSON parsing).
     * We stub aiService.call to return this Map, which PreparationPlanner
     * internally casts to its private AiPlanResult record via Jackson.
     *
     * Since AiPlanResult is private we return an object the ObjectMapper
     * would produce if it deserialized matching JSON — here we simulate this
     * by returning a Map with the correct key structure.
     */
    private PreparationPlanner.AiPlanResult fakeAiResult(int weekCount, int daysPerWeek) {
        List<PreparationPlanner.AiWeek> weeks = new java.util.ArrayList<>();
        int dayCounter = 1;
        for (int w = 1; w <= weekCount; w++) {
            List<PreparationPlanner.AiDay> days = new java.util.ArrayList<>();
            for (int d = 0; d < daysPerWeek; d++) {
                days.add(new PreparationPlanner.AiDay(
                    dayCounter++,
                    "Focus day " + (dayCounter - 1),
                    List.of("Task A", "Task B", "Task C", "Task D"),
                    5,
                    "CODING"
                ));
            }
            weeks.add(new PreparationPlanner.AiWeek(
                w,
                "Week " + w + " theme",
                List.of("Goal 1", "Goal 2"),
                days
            ));
        }
        return new PreparationPlanner.AiPlanResult(
            "Focus on DSA and Java core concepts first.",
            weeks,
            Map.of("Java", List.of(1, 2), "DSA", List.of(1, 2)),
            Map.of("Java", List.of("Effective Java", "Java Brains")),
            List.of("Complete Java by Day 7", "Mock test by Day 12")
        );
    }

    // ── TC-PP-01 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-PP-01: 14-day plan → 2 weeks built, mock dates derived, no warning")
    void plan_14Days_buildsWeeksAndMockDates() {
        when(aiService.call(anyString(), anyString(), any(Class.class)))
                .thenReturn(fakeAiResult(2, 7));

        PreparationPlanResponse resp =
                planner.plan(requestWithDays(14, List.of("Java", "DSA")));

        assertThat(resp.getTotalDaysAvailable()).isEqualTo(14);
        assertThat(resp.getWeeklyPlan()).hasSize(2);
        assertThat(resp.getWeeklyPlan().get(0).getWeekNumber()).isEqualTo(1);
        assertThat(resp.getUpcomingDays()).hasSize(4);   // today + next 3

        // Mock dates: 50%, 75%, 90% of 14 days = day 7, 10, 12
        assertThat(resp.getMockInterviewDates()).isNotEmpty();
        assertThat(resp.getMockInterviewDates()).allMatch(d -> d.isAfter(LocalDate.now()));

        // All DayTask dates should be >= today
        resp.getWeeklyPlan().forEach(week ->
            week.getDays().forEach(day ->
                assertThat(day.getDate()).isAfterOrEqualTo(LocalDate.now())));

        assertThat(resp.getWarning()).isNull();
        assertThat(resp.getSummary()).isNotBlank();
        assertThat(resp.getMilestones()).hasSize(2);
    }

    // ── TC-PP-02 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-PP-02: Drive in 2 days → short-timeline warning, plan returned")
    void plan_2DayDrive_warningEmitted() {
        when(aiService.call(anyString(), anyString(), any(Class.class)))
                .thenReturn(fakeAiResult(1, 2));

        PreparationPlanResponse resp =
                planner.plan(requestWithDays(2, List.of("Java")));

        assertThat(resp.getWarning()).isNotNull()
                .contains("2 day");
        // Plan is still returned despite the warning
        assertThat(resp.getWeeklyPlan()).isNotEmpty();
    }

    // ── TC-PP-03 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-PP-03: Drive in 120 days → capped at 90, warning emitted")
    void plan_120DayDrive_cappedAt90() {
        // Capture user prompt to verify planDays = 90
        ArgumentCaptor<String> userPromptCaptor = ArgumentCaptor.forClass(String.class);
        when(aiService.call(anyString(), userPromptCaptor.capture(), any(Class.class)))
                .thenReturn(fakeAiResult(13, 7));

        PreparationPlanResponse resp =
                planner.plan(requestWithDays(120, List.of("Java", "System Design")));

        // totalDaysAvailable = 120 (actual), but plan prompt uses 90
        assertThat(resp.getTotalDaysAvailable()).isEqualTo(120);
        String prompt = userPromptCaptor.getValue();
        assertThat(prompt).contains("90");   // planDays cap applied to prompt

        assertThat(resp.getWarning()).isNotNull()
                .contains("90");
    }

    // ── TC-PP-04 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-PP-04: 11 skill gaps → large-gap warning attached")
    void plan_tooManySkillGaps_warningAttached() {
        when(aiService.call(anyString(), anyString(), any(Class.class)))
                .thenReturn(fakeAiResult(4, 7));

        List<String> manyGaps = List.of(
            "Java","DSA","System Design","DBMS","OS","Networks",
            "Spring Boot","Microservices","Docker","Kubernetes","AWS"
        ); // 11 gaps

        PreparationPlanResponse resp =
                planner.plan(requestWithDays(30, manyGaps));

        assertThat(resp.getWarning()).isNotNull()
                .contains("11")
                .contains("skill gaps");
    }

    // ── TC-PP-05 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-PP-05: AI timeout → AiServiceException(TIMEOUT) propagated")
    void plan_aiTimeout_propagatesException() {
        when(aiService.call(anyString(), anyString(), any(Class.class)))
                .thenThrow(new AiServiceException(
                        AiServiceException.AiErrorType.TIMEOUT,
                        "AI provider request timed out after 30s"));

        assertThatThrownBy(() ->
                planner.plan(requestWithDays(14, List.of("Java"))))
                .isInstanceOf(AiServiceException.class)
                .satisfies(e -> assertThat(((AiServiceException) e).getErrorType())
                        .isEqualTo(AiServiceException.AiErrorType.TIMEOUT));
    }
}
