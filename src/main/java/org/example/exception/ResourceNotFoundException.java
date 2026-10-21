package org.example.exception;

/**
 * Base exception for requested resources that could not be found.
 */
public class ResourceNotFoundException extends PlacementException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
