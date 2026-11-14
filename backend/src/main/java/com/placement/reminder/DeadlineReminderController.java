package com.placement.reminder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for the deadline reminder engine.
 *
 * <ul>
 *   <li>POST /api/reminders/trigger        – manual trigger (STAFF / ADMIN only)</li>
 *   <li>GET  /api/reminders/upcoming        – upcoming deadlines listing (all authenticated)</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class DeadlineReminderController {

    private final DeadlineReminderService deadlineReminderService;

    /**
     * Manually trigger the deadline reminder engine.
     *
     * @param days how many days ahead to look (default 2)
     * @return execution summary with counts of reminders sent
     */
    @PostMapping("/trigger")
    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    public ResponseEntity<ReminderExecutionSummary> triggerReminders(
            @RequestParam(value = "days", defaultValue = "2") int days) {

        if (days < 0 || days > 30) {
            throw new IllegalArgumentException("days must be between 0 and 30");
        }

        log.info("Manual reminder trigger requested for next {} days", days);
        ReminderExecutionSummary summary = deadlineReminderService.executeDeadlineReminders(days);
        log.info("Manual reminder trigger completed: {} reminders sent", summary.getTotalRemindersSent());
        return ResponseEntity.ok(summary);
    }

    /**
     * List upcoming deadlines (drives and hackathons) closing within {@code days} days.
     *
     * @param days look-ahead window in days (default 7)
     * @return sorted list of upcoming deadline DTOs
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<UpcomingDeadlineDto>> getUpcomingDeadlines(
            @RequestParam(value = "days", defaultValue = "7") int days) {

        if (days < 0 || days > 90) {
            throw new IllegalArgumentException("days must be between 0 and 90");
        }

        List<UpcomingDeadlineDto> deadlines = deadlineReminderService.getUpcomingDeadlines(days);
        return ResponseEntity.ok(deadlines);
    }
}

