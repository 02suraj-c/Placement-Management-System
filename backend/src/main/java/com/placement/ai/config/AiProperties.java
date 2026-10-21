package com.placement.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Binds all ai.* keys from application.properties.
 * Fail-fast on startup if required keys are missing.
 */
@ConfigurationProperties(prefix = "ai")
@Validated
public record AiProperties(

    /** openai | gemini | claude */
    @NotBlank String provider,

    /** Injected from env var AI_API_KEY — never hardcoded */
    @NotBlank String apiKey,

    /** e.g. gpt-4o-mini | gemini-1.5-flash | claude-3-haiku-20240307 */
    @NotBlank String model,

    /** Full chat-completions URL for the chosen provider */
    @NotBlank String baseUrl,

    /** Per-request HTTP timeout in seconds */
    @Min(5) int timeoutSeconds,

    /** Max retry attempts on transient failure */
    @Min(1) int maxRetries
) {}
