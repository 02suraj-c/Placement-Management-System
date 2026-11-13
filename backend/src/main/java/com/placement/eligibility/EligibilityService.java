package com.placement.eligibility;

import com.placement.eligibility.dto.EligibilityCheckResponse;
import com.placement.eligibility.dto.EligibilityCriterionResult;
import com.placement.eligibility.dto.WhyNotDetail;
import com.placement.exception.ResourceNotFoundException;
import com.placement.placement.DriveStatus;
import com.placement.placement.PlacementDrive;
import com.placement.placement.PlacementRepository;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
import com.placement.util.YearUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service evaluating Student eligibility against Placement Drives with smart "Why Not" breakdown.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EligibilityService {

    private final StudentRepository studentRepository;
    private final PlacementRepository placementRepository;

    public EligibilityCheckResponse checkEligibility(Long studentId, Long driveId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID must not be null");
        }
        if (driveId == null) {
            throw new IllegalArgumentException("Drive ID must not be null");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        PlacementDrive drive = placementRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("PlacementDrive", "id", driveId));

        return evaluateEligibility(student, drive);
    }

    public boolean isProfileComplete(Student student) {
        if (student == null) {
            return false;
        }
        return student.getCgpa() != null
                && student.getDepartment() != null && !student.getDepartment().trim().isEmpty()
                && student.getActiveBacklogs() != null
                && student.getRollNumber() != null && !student.getRollNumber().trim().isEmpty()
                && student.getCourse() != null && !student.getCourse().trim().isEmpty();
    }

    public EligibilityCheckResponse evaluateEligibility(Student student, PlacementDrive drive) {
        if (student == null) {
            throw new IllegalArgumentException("Student must not be null");
        }
        if (drive == null) {
            throw new IllegalArgumentException("Placement drive must not be null");
        }

        List<String> reasons = new ArrayList<>();
        List<EligibilityCriterionResult> criteria = new ArrayList<>();
        boolean profileComplete = true;

        // Rule 0 — Profile completeness & Missing CGPA handling
        if (student.getCgpa() == null) {
            profileComplete = false;
            reasons.add("Student profile is incomplete: CGPA is missing.");
        }
        if (student.getDepartment() == null || student.getDepartment().trim().isEmpty()) {
            profileComplete = false;
            reasons.add("Student profile is incomplete: Department is missing.");
        }
        if (student.getActiveBacklogs() == null) {
            profileComplete = false;
            reasons.add("Student profile is incomplete: Active backlogs count is missing.");
        }

        criteria.add(EligibilityCriterionResult.builder()
                .criterion("PROFILE_COMPLETION")
                .passed(profileComplete)
                .message(profileComplete ? "Student profile is complete." : "Student profile is missing required academic fields.")
                .studentValue(profileComplete ? "COMPLETE" : "INCOMPLETE")
                .requiredValue("COMPLETE")
                .build());

        // Rule 1 — Drive status
        boolean driveActive = (drive.getStatus() != DriveStatus.CANCELLED && drive.getStatus() != DriveStatus.COMPLETED);
        if (drive.getStatus() == DriveStatus.CANCELLED) {
            reasons.add("Placement drive is cancelled.");
        } else if (drive.getStatus() == DriveStatus.COMPLETED) {
            reasons.add("Placement drive is completed.");
        }

        criteria.add(EligibilityCriterionResult.builder()
                .criterion("DRIVE_STATUS")
                .passed(driveActive)
                .message(driveActive ? "Drive is currently active." : "Placement drive is " + drive.getStatus())
                .studentValue(drive.getStatus() != null ? drive.getStatus().name() : null)
                .requiredValue("UPCOMING / IN_PROGRESS")
                .build());

        // Rule 2 — CGPA check against drive minimum
        Double studentCgpa = student.getCgpa();
        Double minCgpa = drive.getMinimumCgpa();
        Double cgpaShortfall = null;
        boolean cgpaPassed = true;

        if (minCgpa != null) {
            if (studentCgpa == null) {
                cgpaPassed = false;
                cgpaShortfall = minCgpa;
            } else if (studentCgpa < minCgpa) {
                cgpaPassed = false;
                cgpaShortfall = Math.round((minCgpa - studentCgpa) * 100.0) / 100.0;
                reasons.add("Student CGPA (" + studentCgpa + ") is below minimum required CGPA (" + minCgpa + ").");
            }
        }

        criteria.add(EligibilityCriterionResult.builder()
                .criterion("CGPA")
                .passed(cgpaPassed)
                .message(cgpaPassed ? "CGPA criteria satisfied." : "CGPA " + (studentCgpa != null ? studentCgpa : "N/A") + " < " + minCgpa + " required")
                .studentValue(studentCgpa)
                .requiredValue(minCgpa)
                .build());

        // Rule 3 — Active backlogs
        Integer studentBacklogs = student.getActiveBacklogs();
        Integer maxBacklogs = drive.getMaximumAllowedBacklogs();
        Integer excessBacklogs = null;
        boolean backlogsPassed = true;

        if (maxBacklogs != null) {
            if (studentBacklogs == null) {
                backlogsPassed = false;
            } else if (studentBacklogs > maxBacklogs) {
                backlogsPassed = false;
                excessBacklogs = studentBacklogs - maxBacklogs;
                reasons.add("Student active backlogs (" + studentBacklogs + ") exceeds maximum allowed backlogs (" + maxBacklogs + ").");
            }
        }

        criteria.add(EligibilityCriterionResult.builder()
                .criterion("ACTIVE_BACKLOGS")
                .passed(backlogsPassed)
                .message(backlogsPassed ? "Backlogs criteria satisfied." : (studentBacklogs != null ? studentBacklogs : "N/A") + " active backlogs > " + maxBacklogs + " allowed")
                .studentValue(studentBacklogs)
                .requiredValue(maxBacklogs)
                .build());

        // Rule 4 — Department / branch
        String eligibleBranches = drive.getEligibleBranches();
        boolean branchPassed = true;
        if (eligibleBranches != null && !eligibleBranches.trim().isEmpty()) {
            String studentDept = student.getDepartment();
            boolean matches = false;
            if (studentDept != null && !studentDept.trim().isEmpty()) {
                String normalizedDept = studentDept.trim();
                String[] branches = eligibleBranches.split(",");
                for (String branch : branches) {
                    if (branch.trim().equalsIgnoreCase(normalizedDept)) {
                        matches = true;
                        break;
                    }
                }
            }
            if (!matches) {
                branchPassed = false;
                String deptDisplay = studentDept != null ? studentDept.trim() : "";
                reasons.add("Student department '" + deptDisplay + "' is not in eligible branches " + eligibleBranches + ".");
            }
        }

        criteria.add(EligibilityCriterionResult.builder()
                .criterion("DEPARTMENT")
                .passed(branchPassed)
                .message(branchPassed ? "Department is eligible." : "Department not eligible for this drive.")
                .studentValue(student.getDepartment())
                .requiredValue(eligibleBranches)
                .build());

        // Rule 5 — Academic Year eligibility
        String targetYears = drive.getTargetYears();
        boolean yearPassed = true;
        if (targetYears != null && !targetYears.trim().isEmpty()) {
            Integer studentYear = student.getAcademicYear();
            if (!YearUtils.matchesYear(targetYears, studentYear)) {
                yearPassed = false;
                reasons.add("Placement drive is only open to " + YearUtils.formatYears(targetYears) +
                        ", but student is in " + YearUtils.getYearLabel(studentYear) + ".");
            }
        }

        criteria.add(EligibilityCriterionResult.builder()
                .criterion("ACADEMIC_YEAR")
                .passed(yearPassed)
                .message(yearPassed ? "Academic year is eligible." : "Academic year not eligible for this drive.")
                .studentValue(student.getAcademicYear())
                .requiredValue(targetYears)
                .build());

        boolean eligible = reasons.isEmpty();

        WhyNotDetail whyNot = WhyNotDetail.builder()
                .eligible(eligible)
                .reasons(new ArrayList<>(reasons))
                .cgpaShortfall(cgpaShortfall)
                .studentCgpa(studentCgpa)
                .requiredCgpa(minCgpa)
                .excessBacklogs(excessBacklogs)
                .studentBacklogs(studentBacklogs)
                .allowedBacklogs(maxBacklogs)
                .branchDisqualified(!branchPassed)
                .studentDepartment(student.getDepartment())
                .eligibleBranches(drive.getEligibleBranches())
                .yearDisqualified(!yearPassed)
                .studentYear(student.getAcademicYear())
                .targetYears(drive.getTargetYears())
                .driveInactive(!driveActive)
                .driveStatus(drive.getStatus() != null ? drive.getStatus().name() : null)
                .profileIncomplete(!profileComplete)
                .build();

        return EligibilityCheckResponse.builder()
                .studentId(student.getId())
                .driveId(drive.getId())
                .eligible(eligible)
                .profileComplete(profileComplete)
                .reasons(reasons)
                .whyNot(whyNot)
                .criteria(criteria)
                .studentCgpa(student.getCgpa())
                .minimumCgpa(drive.getMinimumCgpa())
                .studentActiveBacklogs(student.getActiveBacklogs())
                .maximumAllowedBacklogs(drive.getMaximumAllowedBacklogs())
                .studentDepartment(student.getDepartment())
                .eligibleBranches(drive.getEligibleBranches())
                .driveStatus(drive.getStatus() != null ? drive.getStatus().name() : null)
                .build();
    }
}
