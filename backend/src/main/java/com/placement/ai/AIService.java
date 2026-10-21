package com.placement.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.ai.config.AiProperties;
import com.placement.ai.exception.AiServiceException;
import com.placement.ai.exception.AiServiceException.AiErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/**
 * Central AI gateway used by ResumeAnalyzer, InterviewGenerator,
 * and PreparationPlanner.
 *
 * Contract
 * ────────
 * • Single public method: {@code call(systemPrompt, userPrompt, responseClass)}
 * • Requests JSON-mode output from the provider.
 * • Retries up to {@code ai.max-retries} times on transient (5xx / timeout) errors.
 * • Validates the raw JSON before deserialising; throws {@link AiServiceException}
 *   on any failure so callers never receive malformed data.
 * • API key is read from AiProperties (env var AI_API_KEY) — never hardcoded.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AIService {

    @Qualifier("aiRestClient")
    private final RestClient aiRestClient;

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Call the AI provider and deserialise the response into {@code T}.
     *
     * @param systemPrompt  system-level instruction (role + JSON-format mandate)
     * @param userPrompt    actual user content to analyse / generate
     * @param responseClass target Java type for deserialisation
     * @param <T>           response type
     * @return parsed response object
     * @throws AiServiceException on timeout, retries exhausted, or bad JSON
     */
    public <T> T call(String systemPrompt, String userPrompt, Class<T> responseClass) {
        String rawJson = callWithRetry(systemPrompt, userPrompt);
        return parseJson(rawJson, responseClass);
    }

    /**
     * Variant for generic types (e.g., {@code List<QuestionResponse>}).
     */
    public <T> T call(String systemPrompt, String userPrompt, TypeReference<T> typeRef) {
        String rawJson = callWithRetry(systemPrompt, userPrompt);
        return parseJson(rawJson, typeRef);
    }

    // ── Retry loop ─────────────────────────────────────────────────────────────

    private String callWithRetry(String systemPrompt, String userPrompt) {
        int maxAttempts = aiProperties.maxRetries();
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.debug("AI call attempt {}/{} model={}", attempt, maxAttempts,
                        aiProperties.model());
                return doCall(systemPrompt, userPrompt);

            } catch (AiServiceException e) {
                // Client errors (4xx) and unparseable response bodies are not retryable
                if (e.getErrorType() == AiErrorType.CLIENT_ERROR
                        || e.getErrorType() == AiErrorType.INVALID_RESPONSE) {
                    throw e;
                }
                lastException = e;
                log.warn("AI call attempt {}/{} failed: {} — {}",
                        attempt, maxAttempts, e.getErrorType(), e.getMessage());

                if (attempt < maxAttempts) {
                    backoff(attempt);
                }
            }
        }

        throw new AiServiceException(AiErrorType.RETRIES_EXHAUSTED,
                "AI provider failed after " + maxAttempts + " attempts",
                lastException);
    }

    // ── HTTP call ──────────────────────────────────────────────────────────────

    private String doCall(String systemPrompt, String userPrompt) {
        Map<String, Object> requestBody = buildRequestBody(systemPrompt, userPrompt);

        try {
            String rawResponse = aiRestClient.post()
                    .body(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new AiServiceException(AiErrorType.CLIENT_ERROR,
                                "AI provider returned HTTP " + res.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new AiServiceException(AiErrorType.SERVER_ERROR,
                                "AI provider server error HTTP " + res.getStatusCode());
                    })
                    .body(String.class);

            return extractContent(rawResponse);

        } catch (ResourceAccessException e) {
            // Covers java.net.SocketTimeoutException and connect failures
            throw new AiServiceException(AiErrorType.TIMEOUT,
                    "AI provider request timed out after " +
                    aiProperties.timeoutSeconds() + "s", e);
        } catch (AiServiceException e) {
            throw e;   // re-throw typed exceptions from onStatus handlers
        } catch (Exception e) {
            throw new AiServiceException(AiErrorType.SERVER_ERROR,
                    "Unexpected error calling AI provider: " + e.getMessage(), e);
        }
    }

    // ── Request builder — OpenAI-compatible Chat Completions format ────────────

    /**
     * Builds an OpenAI-compatible request body.
     *
     * Provider compatibility:
     *   • OpenAI  : works as-is (gpt-4o, gpt-4o-mini, etc.)
     *   • Gemini  : use openai-compatible endpoint
     *               (https://generativelanguage.googleapis.com/v1beta/openai/chat/completions)
     *   • Claude  : use openai-compatible endpoint via Bedrock or direct Anthropic API
     *
     * The response_format JSON-mode instruction ensures the model returns
     * only valid JSON, reducing hallucinated markdown fences.
     */
    private Map<String, Object> buildRequestBody(String systemPrompt, String userPrompt) {
        return Map.of(
            "model",  aiProperties.model(),
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user",   "content", userPrompt)
            ),
            "response_format", Map.of("type", "json_object"),
            "temperature", 0.3,          // lower = more deterministic JSON
            "max_tokens",  4096
        );
    }

    // ── Response extraction ────────────────────────────────────────────────────

    /**
     * Pulls the message content string out of the OpenAI-compatible response envelope.
     * Also strips any residual markdown fences the model may add.
     */
    private String extractContent(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new AiServiceException(AiErrorType.INVALID_RESPONSE,
                    "AI provider returned an empty body");
        }
        try {
            JsonNode root    = objectMapper.readTree(rawResponse);
            JsonNode choices = root.path("choices");

            if (!choices.isArray() || choices.isEmpty()) {
                throw new AiServiceException(AiErrorType.INVALID_RESPONSE,
                        "AI response missing 'choices' array: " + truncate(rawResponse));
            }

            String content = choices.get(0)
                    .path("message")
                    .path("content")
                    .asText(null);

            if (content == null || content.isBlank()) {
                throw new AiServiceException(AiErrorType.INVALID_RESPONSE,
                        "AI response has empty 'content'");
            }

            // Strip markdown fences if the model ignores json_object mode
            content = stripMarkdownFences(content);

            // Quick sanity check — must start with { or [
            String trimmed = content.trim();
            if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
                throw new AiServiceException(AiErrorType.INVALID_RESPONSE,
                        "AI content is not valid JSON: " + truncate(trimmed));
            }

            return trimmed;

        } catch (JsonProcessingException e) {
            throw new AiServiceException(AiErrorType.INVALID_RESPONSE,
                    "Could not parse AI response envelope", e);
        }
    }

    // ── JSON deserialisation ───────────────────────────────────────────────────

    private <T> T parseJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("AI JSON parse failure. Raw content: {}", truncate(json));
            throw new AiServiceException(AiErrorType.INVALID_RESPONSE,
                    "AI response JSON does not match expected schema: " + e.getOriginalMessage(), e);
        }
    }

    private <T> T parseJson(String json, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.error("AI JSON parse failure. Raw content: {}", truncate(json));
            throw new AiServiceException(AiErrorType.INVALID_RESPONSE,
                    "AI response JSON does not match expected schema: " + e.getOriginalMessage(), e);
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    /** Exponential back-off: 1s, 2s, 4s, … capped at 16 s. */
    private void backoff(int attempt) {
        long delayMs = Math.min((long) Math.pow(2, attempt - 1) * 1000L, 16_000L);
        log.debug("Back-off {}ms before retry", delayMs);
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private static String truncate(String s) {
        return s != null && s.length() > 300 ? s.substring(0, 300) + "…" : s;
    }

    private static String stripMarkdownFences(String s) {
        // Remove ```json ... ``` or ``` ... ``` wrappers
        String t = s.strip();
        if (t.startsWith("```")) {
            int firstNewline = t.indexOf('\n');
            if (firstNewline != -1) t = t.substring(firstNewline + 1);
            if (t.endsWith("```")) t = t.substring(0, t.length() - 3).stripTrailing();
        }
        return t;
    }
}
