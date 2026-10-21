package org.example.exception;

/**
 * Exception thrown when validation fails for an entity or input parameter.
 */
public class ValidationException extends PlacementException {
    public ValidationException(String message) {
        super(message);
    }
}
