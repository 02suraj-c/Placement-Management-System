package org.example.exception;

/**
 * Exception thrown when an application cannot be found.
 */
public class ApplicationNotFoundException extends ResourceNotFoundException {
    public ApplicationNotFoundException(String message) {
        super(message);
    }
}
