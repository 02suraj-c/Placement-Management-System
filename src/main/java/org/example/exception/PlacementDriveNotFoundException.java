package org.example.exception;

/**
 * Exception thrown when a placement drive cannot be found.
 */
public class PlacementDriveNotFoundException extends ResourceNotFoundException {
    public PlacementDriveNotFoundException(String message) {
        super(message);
    }
}
