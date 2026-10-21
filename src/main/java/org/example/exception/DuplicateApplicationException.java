package org.example.exception;

/**
 * Exception thrown when a student attempts to apply more than once to the same placement drive.
 */
public class DuplicateApplicationException extends PlacementException {
    public DuplicateApplicationException(String message) {
        super(message);
    }
}
