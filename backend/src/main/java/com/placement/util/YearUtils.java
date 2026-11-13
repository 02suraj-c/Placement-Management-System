package com.placement.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility methods for multi-year tagging, filtering, and validation.
 * Supports values like "3,4", "1,2,3,4", "2nd Year, 3rd Year", "All".
 */
public final class YearUtils {

    private YearUtils() {}

    /**
     * Determines whether the given student's academic year matches the targetYears specification.
     *
     * @param targetYears comma-separated or space-separated list of target years (e.g. "3,4", "2nd Year", "ALL")
     * @param studentYear academic year of the student (1, 2, 3, or 4)
     * @return true if matches or if targetYears is unrestricted
     */
    public static boolean matchesYear(String targetYears, Integer studentYear) {
        if (targetYears == null || targetYears.trim().isEmpty()) {
            return true; // No restriction means open to all years
        }
        if (studentYear == null) {
            return true;
        }

        String normalized = targetYears.trim().toUpperCase();
        if (normalized.equals("ALL") || normalized.equals("ANY") || normalized.equals("ALL YEARS")) {
            return true;
        }

        Set<Integer> parsedYears = parseYears(targetYears);
        if (parsedYears.isEmpty()) {
            return true;
        }
        return parsedYears.contains(studentYear);
    }

    /**
     * Parse years from string into a Set of Integers (1-4).
     */
    public static Set<Integer> parseYears(String targetYears) {
        if (targetYears == null || targetYears.trim().isEmpty()) {
            return Collections.emptySet();
        }

        return Arrays.stream(targetYears.split("[,;\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(YearUtils::extractYearNumber)
                .filter(y -> y != null && y >= 1 && y <= 4)
                .collect(Collectors.toSet());
    }

    /**
     * Extract integer year (1-4) from token like "1", "1st", "Year 2", "3rd Year".
     */
    public static Integer extractYearNumber(String token) {
        if (token == null) return null;
        String digits = token.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return null;
        try {
            int val = Integer.parseInt(digits);
            return (val >= 1 && val <= 4) ? val : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Formats a collection of years into a standard string: "1st, 2nd, 3rd Year".
     */
    public static String formatYears(String targetYears) {
        Set<Integer> years = parseYears(targetYears);
        if (years.isEmpty() || years.size() == 4) {
            return "All Academic Years (1st - 4th)";
        }
        return years.stream()
                .sorted()
                .map(YearUtils::getYearLabel)
                .collect(Collectors.joining(", "));
    }

    public static String getYearLabel(Integer year) {
        if (year == null) return "";
        return switch (year) {
            case 1 -> "1st Year";
            case 2 -> "2nd Year";
            case 3 -> "3rd Year";
            case 4 -> "4th Year";
            default -> year + "th Year";
        };
    }

    /**
     * Calculate academic year from semester (1-8).
     */
    public static int calculateYearFromSemester(Integer semester) {
        if (semester == null || semester < 1) return 1;
        if (semester > 8) return 4;
        return (semester + 1) / 2;
    }

    /**
     * Get allowable semesters for a given academic year.
     * E.g. Year 1 -> [1, 2], Year 2 -> [3, 4], Year 3 -> [5, 6], Year 4 -> [7, 8].
     */
    public static List<Integer> getSemestersForYear(int year) {
        int clampedYear = Math.max(1, Math.min(4, year));
        int sem1 = clampedYear * 2 - 1;
        int sem2 = clampedYear * 2;
        return List.of(sem1, sem2);
    }
}

