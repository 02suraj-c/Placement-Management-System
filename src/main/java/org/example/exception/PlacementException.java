package org.example.exception;

/**
 * Base exception for all Placement Management System exceptions.
 */
public class PlacementException extends RuntimeException {
    public PlacementException(String message) {
        super(message);
    }

    public PlacementException(String message, Throwable cause) {
        super(message, cause);
    }
}
