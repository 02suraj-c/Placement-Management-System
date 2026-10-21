package org.example.util;

import org.example.exception.ValidationException;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Utility class providing common validation routines.
 */
public final class ValidationUtil {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)?([a-zA-Z0-9.-]+(\\.[a-zA-Z]{2,})+)(:[0-9]{1,5})?(/.*)?$",
            Pattern.CASE_INSENSITIVE
    );

    private ValidationUtil() {
        // utility class
    }

    public static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " must not be null or blank");
        }
        return value.trim();
    }

    public static double validateCGPA(double cgpa, String fieldName) {
        if (Double.isNaN(cgpa) || cgpa < 0.0 || cgpa > 10.0) {
            throw new ValidationException(fieldName + " must be between 0.0 and 10.0. Provided: " + cgpa);
        }
        return cgpa;
    }

    public static double validateNonNegative(double value, String fieldName) {
        if (Double.isNaN(value) || value < 0.0) {
            throw new ValidationException(fieldName + " cannot be negative. Provided: " + value);
        }
        return value;
    }

    public static int validateNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new ValidationException(fieldName + " cannot be negative. Provided: " + value);
        }
        return value;
    }

    public static String validateWebsite(String website) {
        if (website == null || website.trim().isEmpty()) {
            return "";
        }
        String trimmed = website.trim();
        if (!URL_PATTERN.matcher(trimmed).matches()) {
            throw new ValidationException("Invalid website URL format: " + trimmed);
        }
        return trimmed;
    }

    public static void validateDateOrder(LocalDate deadline, LocalDate driveDate) {
        if (deadline == null) {
            throw new ValidationException("Application deadline is required");
        }
        if (driveDate == null) {
            throw new ValidationException("Drive date is required");
        }
        if (deadline.isAfter(driveDate)) {
            throw new ValidationException("Application deadline (" + deadline + ") cannot be after drive date (" + driveDate + ")");
        }
    }
}
