package org.example.exception;

/**
 * Exception thrown when a company cannot be found.
 */
public class CompanyNotFoundException extends ResourceNotFoundException {
    public CompanyNotFoundException(String message) {
        super(message);
    }
}
