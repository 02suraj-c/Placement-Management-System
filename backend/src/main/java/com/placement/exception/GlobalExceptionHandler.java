package com.placement.exception;

import com.placement.ai.exception.AiServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the entire application.
 * Catches validation, security, not-found, and unexpected errors
 * and returns consistent JSON error envelopes.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ── 400 – Validation ──────────────────────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                fieldErrors,
                LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(error);
    }

    // ── 400 – Illegal Argument ────────────────────────────────────────────────
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ── 401 – Unauthenticated ─────────────────────────────────────────────────
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuth(AuthenticationException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // ── 403 – Forbidden ───────────────────────────────────────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        return buildError(HttpStatus.FORBIDDEN, "Access denied: insufficient role");
    }

    // ── 404 – Not Found ───────────────────────────────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── AI Service Errors ─────────────────────────────────────────────────────
    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ApiError> handleAiError(AiServiceException ex) {
        HttpStatus status = switch (ex.getErrorType()) {
            case CLIENT_ERROR     -> HttpStatus.BAD_GATEWAY;
            case SERVER_ERROR     -> HttpStatus.BAD_GATEWAY;
            case TIMEOUT          -> HttpStatus.GATEWAY_TIMEOUT;
            case RETRIES_EXHAUSTED -> HttpStatus.SERVICE_UNAVAILABLE;
            case INVALID_RESPONSE -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
        log.warn("AI service error [{}]: {}", ex.getErrorType(), ex.getMessage());
        return buildError(status, "AI service error: " + ex.getMessage());
    }

    // ── 500 – Fallback ────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please contact support.");
    }

    // ── helpers ───────────────────────────────────────────────────────────────
    private ResponseEntity<ApiError> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ApiError(status.value(), message, null, LocalDateTime.now()));
    }

    /** Uniform error envelope. */
    public record ApiError(
            int status,
            String message,
            Map<String, String> fieldErrors,
            LocalDateTime timestamp
    ) {}
}
