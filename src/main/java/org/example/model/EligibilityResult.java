package org.example.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the evaluation outcome of student eligibility for a placement drive,
 * including specific reasons when ineligible.
 */
public class EligibilityResult {
    private final boolean eligible;
    private final List<String> reasons;

    private EligibilityResult(boolean eligible, List<String> reasons) {
        this.eligible = eligible;
        this.reasons = reasons != null ? Collections.unmodifiableList(new ArrayList<>(reasons)) : Collections.emptyList();
    }

    public static EligibilityResult eligible() {
        return new EligibilityResult(true, Collections.singletonList("Student meets all eligibility requirements."));
    }

    public static EligibilityResult ineligible(List<String> reasons) {
        return new EligibilityResult(false, reasons);
    }

    public static EligibilityResult ineligible(String singleReason) {
        return new EligibilityResult(false, Collections.singletonList(singleReason));
    }

    public boolean isEligible() {
        return eligible;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public String getFormattedReason() {
        if (eligible) {
            return "Eligible: Student meets all eligibility requirements.";
        }
        return "Ineligible: " + String.join("; ", reasons);
    }

    @Override
    public String toString() {
        return "EligibilityResult{" +
                "eligible=" + eligible +
                ", reasons=" + reasons +
                '}';
    }
}
