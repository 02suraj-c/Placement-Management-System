package com.placement.ai.exception;

/**
 * Thrown when the AI provider returns an error, times out, or returns
 * a response that cannot be parsed as valid JSON.
 * Callers should catch this and surface a safe error to the client.
 */
public class AiServiceException extends RuntimeException {

    private final AiErrorType errorType;

    public AiServiceException(AiErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public AiServiceException(AiErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public AiErrorType getErrorType() {
        return errorType;
    }

    public enum AiErrorType {
        /** HTTP 4xx from provider (bad request, invalid key, etc.) */
        CLIENT_ERROR,
        /** HTTP 5xx or provider outage */
        SERVER_ERROR,
        /** Read/connect timeout */
        TIMEOUT,
        /** All retry attempts exhausted */
        RETRIES_EXHAUSTED,
        /** Response received but not valid JSON or missing expected fields */
        INVALID_RESPONSE
    }
}
