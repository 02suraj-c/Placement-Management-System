package org.example.service;

import org.example.exception.PlacementDriveNotFoundException;
import org.example.exception.StudentNotFoundException;
import org.example.exception.ValidationException;
import org.example.model.EligibilityResult;
import org.example.model.PlacementDrive;
import org.example.model.Student;
import org.example.repository.PlacementDriveRepository;
import org.example.repository.StudentRepository;
import org.example.util.ValidationUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of StudentEligibilityService.
 */
public class StudentEligibilityServiceImpl implements StudentEligibilityService {
    private final StudentRepository studentRepository;
    private final PlacementDriveRepository driveRepository;

    public StudentEligibilityServiceImpl() {
        this(null, null);
    }

    public StudentEligibilityServiceImpl(StudentRepository studentRepository, PlacementDriveRepository driveRepository) {
        this.studentRepository = studentRepository;
        this.driveRepository = driveRepository;
    }

    @Override
    public EligibilityResult checkEligibility(Student student, PlacementDrive drive) {
        if (student == null) {
            throw new ValidationException("Student must not be null for eligibility evaluation");
        }
        if (drive == null) {
            throw new ValidationException("Placement drive must not be null for eligibility evaluation");
        }

        List<String> reasons = new ArrayList<>();

        if (drive.isCancelled()) {
            reasons.add("Placement drive is cancelled");
        }

        // Compare CGPA
        if (student.getCgpa() < drive.getMinimumCGPA()) {
            reasons.add(String.format("Student CGPA (%.2f) is below minimum required CGPA (%.2f)",
                    student.getCgpa(), drive.getMinimumCGPA()));
        }

        // Compare Backlog count
        if (student.getBacklogCount() > drive.getMaximumAllowedBacklogs()) {
            reasons.add(String.format("Student backlogs (%d) exceeds maximum allowed backlogs (%d)",
                    student.getBacklogCount(), drive.getMaximumAllowedBacklogs()));
        }

        // Compare Branch
        if (!drive.isBranchEligible(student.getBranch())) {
            reasons.add(String.format("Student branch '%s' is not in eligible branches %s",
                    student.getBranch(), drive.getEligibleBranches()));
        }

        if (reasons.isEmpty()) {
            return EligibilityResult.eligible();
        } else {
            return EligibilityResult.ineligible(reasons);
        }
    }

    @Override
    public EligibilityResult checkEligibility(String studentId, String driveId) {
        ValidationUtil.requireNonBlank(studentId, "Student ID");
        ValidationUtil.requireNonBlank(driveId, "Drive ID");

        if (studentRepository == null || driveRepository == null) {
            throw new IllegalStateException("Repositories are required for ID-based eligibility checking");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with ID: " + studentId));

        PlacementDrive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new PlacementDriveNotFoundException("Placement drive not found with ID: " + driveId));

        return checkEligibility(student, drive);
    }
}
