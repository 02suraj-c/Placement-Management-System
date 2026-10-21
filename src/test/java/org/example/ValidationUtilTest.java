package org.example;

import org.example.exception.ValidationException;
import org.example.util.ValidationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilTest {

    @Test
    @DisplayName("Should validate non-blank strings")
    void testRequireNonBlank() {
        assertEquals("Valid", ValidationUtil.requireNonBlank("  Valid  ", "Field"));

        assertThrows(ValidationException.class, () -> ValidationUtil.requireNonBlank(null, "Field"));
        assertThrows(ValidationException.class, () -> ValidationUtil.requireNonBlank("", "Field"));
        assertThrows(ValidationException.class, () -> ValidationUtil.requireNonBlank("   ", "Field"));
    }

    @Test
    @DisplayName("Should validate CGPA boundaries (0.0 to 10.0)")
    void testValidateCGPA() {
        assertEquals(0.0, ValidationUtil.validateCGPA(0.0, "CGPA"));
        assertEquals(10.0, ValidationUtil.validateCGPA(10.0, "CGPA"));
        assertEquals(7.5, ValidationUtil.validateCGPA(7.5, "CGPA"));

        assertThrows(ValidationException.class, () -> ValidationUtil.validateCGPA(-0.1, "CGPA"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateCGPA(10.1, "CGPA"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateCGPA(Double.NaN, "CGPA"));
    }

    @Test
    @DisplayName("Should validate non-negative numbers")
    void testValidateNonNegative() {
        assertEquals(0, ValidationUtil.validateNonNegative(0, "Count"));
        assertEquals(5, ValidationUtil.validateNonNegative(5, "Count"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateNonNegative(-1, "Count"));

        assertEquals(0.0, ValidationUtil.validateNonNegative(0.0, "Amount"));
        assertEquals(12.5, ValidationUtil.validateNonNegative(12.5, "Amount"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateNonNegative(-0.01, "Amount"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateNonNegative(Double.NaN, "Amount"));
    }

    @Test
    @DisplayName("Should validate website URLs")
    void testValidateWebsite() {
        assertEquals("", ValidationUtil.validateWebsite(null));
        assertEquals("", ValidationUtil.validateWebsite("   "));
        assertEquals("https://google.com", ValidationUtil.validateWebsite("https://google.com"));
        assertEquals("http://company.org/careers", ValidationUtil.validateWebsite("http://company.org/careers"));
        assertEquals("www.microsoft.com", ValidationUtil.validateWebsite("www.microsoft.com"));

        assertThrows(ValidationException.class, () -> ValidationUtil.validateWebsite("htt!//bad url"));
    }

    @Test
    @DisplayName("Should validate date order")
    void testValidateDateOrder() {
        LocalDate today = LocalDate.now();
        LocalDate future = today.plusDays(5);

        assertDoesNotThrow(() -> ValidationUtil.validateDateOrder(today, future));
        assertDoesNotThrow(() -> ValidationUtil.validateDateOrder(today, today)); // Same date allowed

        assertThrows(ValidationException.class, () -> ValidationUtil.validateDateOrder(future, today));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateDateOrder(null, future));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateDateOrder(today, null));
    }
}
