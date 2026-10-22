package com.placement.eligibility;

import com.placement.eligibility.dto.EligibilityCheckResponse;
import com.placement.exception.ResourceNotFoundException;
import com.placement.placement.DriveStatus;
import com.placement.placement.PlacementDrive;
import com.placement.placement.PlacementRepository;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service evaluating Student eligibility against Placement Drives.
 *
 * Package : com.placement.eligibility
 * Owner   : Person 3
 *
 * Rules:
 *   1. Drive status CANCELLED -> ineligible ("Placement drive is cancelled.")
 *   2. Student CGPA < drive minimumCgpa -> ineligible ("Student CGPA (X) is below minimum required CGPA (Y).")
 *   3. Student activeBacklogs > drive maximumAllowedBacklogs -> ineligible ("Student active backlogs (X) exceeds maximum allowed backlogs (Y).")
 *   4. Student department not in eligibleBranches -> ineligible ("Student department 'X' is not in eligible branches Y.")
 *      - If eligibleBranches is null, empty, or whitespace-only -> all departments are eligible.
 *      - Department comparison is case-insensitive and ignores surrounding whitespace.
 *   5. Final eligibility is true only if zero reasons exist.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EligibilityService {

    private final StudentRepository studentRepository;
    private final PlacementRepository placementRepository;

    /**
     * Look up student and drive by their primary keys and evaluate eligibility.
     *
     * @param studentId primary key of Student
     * @param driveId   primary key of PlacementDrive
     * @return structured EligibilityCheckResponse with all evaluation details
     * @throws ResourceNotFoundException if either entity does not exist
     */
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

    /**
     * Pure evaluation of eligibility rules against in-memory Student and PlacementDrive entities.
     * Continues evaluating all criteria so all applicable failure reasons are returned.
     *
     * @param student target student entity
     * @param drive   target placement drive entity
     * @return structured EligibilityCheckResponse
     */
    public EligibilityCheckResponse evaluateEligibility(Student student, PlacementDrive drive) {
        if (student == null) {
            throw new IllegalArgumentException("Student must not be null");
        }
        if (drive == null) {
            throw new IllegalArgumentException("Placement drive must not be null");
        }

        List<String> reasons = new ArrayList<>();

        // Rule 1 — Drive status
        if (drive.getStatus() == DriveStatus.CANCELLED) {
            reasons.add("Placement drive is cancelled.");
        }

        // Rule 2 — CGPA
        Double studentCgpa = student.getCgpa();
        Double minCgpa = drive.getMinimumCgpa();
        if (minCgpa != null && studentCgpa != null && studentCgpa < minCgpa) {
            reasons.add("Student CGPA (" + studentCgpa + ") is below minimum required CGPA (" + minCgpa + ").");
        }

        // Rule 3 — Active backlogs
        Integer studentBacklogs = student.getActiveBacklogs();
        Integer maxBacklogs = drive.getMaximumAllowedBacklogs();
        if (maxBacklogs != null && studentBacklogs != null && studentBacklogs > maxBacklogs) {
            reasons.add("Student active backlogs (" + studentBacklogs + ") exceeds maximum allowed backlogs (" + maxBacklogs + ").");
        }

        // Rule 4 — Department / branch
        String eligibleBranches = drive.getEligibleBranches();
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
                String deptDisplay = studentDept != null ? studentDept.trim() : "";
                reasons.add("Student department '" + deptDisplay + "' is not in eligible branches " + eligibleBranches + ".");
            }
        }

        boolean eligible = reasons.isEmpty();

        return EligibilityCheckResponse.builder()
                .studentId(student.getId())
                .driveId(drive.getId())
                .eligible(eligible)
                .reasons(reasons)
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
