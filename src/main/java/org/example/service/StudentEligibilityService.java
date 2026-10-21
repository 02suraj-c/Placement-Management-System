package org.example.service;

import org.example.model.EligibilityResult;
import org.example.model.PlacementDrive;
import org.example.model.Student;

/**
 * Service evaluating student eligibility against placement drive requirements.
 */
public interface StudentEligibilityService {
    EligibilityResult checkEligibility(Student student, PlacementDrive drive);
    EligibilityResult checkEligibility(String studentId, String driveId);
}
