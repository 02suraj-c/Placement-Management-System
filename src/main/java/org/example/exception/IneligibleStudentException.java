package org.example.exception;

import java.util.Collections;
import java.util.List;

/**
 * Exception thrown when a student does not meet the eligibility criteria for a placement drive.
 */
public class IneligibleStudentException extends PlacementException {
    private final List<String> reasons;

    public IneligibleStudentException(String message) {
        super(message);
        this.reasons = Collections.singletonList(message);
    }

    public IneligibleStudentException(String message, List<String> reasons) {
        super(message);
        this.reasons = reasons != null ? Collections.unmodifiableList(reasons) : Collections.emptyList();
    }

    public List<String> getReasons() {
        return reasons;
    }
}
