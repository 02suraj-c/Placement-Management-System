package org.example.exception;

/**
 * Exception thrown when a student cannot be found.
 */
public class StudentNotFoundException extends ResourceNotFoundException {
    public StudentNotFoundException(String message) {
        super(message);
    }
}
