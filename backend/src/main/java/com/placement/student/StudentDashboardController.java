package com.placement.student;

import com.placement.student.dto.StudentDashboardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the one-call student dashboard endpoint.
 *
 * <ul>
 *   <li>GET /api/students/dashboard – returns a complete student snapshot (student only)</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentDashboardController {

    private final StudentDashboardService studentDashboardService;

    /**
     * Returns a comprehensive dashboard snapshot for the authenticated student.
     *
     * <p>This single call aggregates:
     * <ul>
     *   <li>Student profile</li>
     *   <li>Application counts (total + breakdown by status)</li>
     *   <li>Upcoming interview rounds</li>
     *   <li>5 most-recent notifications + unread count</li>
     *   <li>Hackathon registration count</li>
     *   <li>Deadlines closing within 7 days</li>
     *   <li>Count of placement drives the student is eligible for</li>
     * </ul>
     * </p>
     *
     * @param userDetails injected by Spring Security from the JWT
     * @return {@link StudentDashboardResponse} with the full snapshot
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<StudentDashboardResponse> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        log.info("Dashboard requested for user: {}", email);

        StudentDashboardResponse dashboard = studentDashboardService.getStudentDashboard(email);
        return ResponseEntity.ok(dashboard);
    }
}

