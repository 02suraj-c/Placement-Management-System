package com.placement.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.ai.config.AiProperties;
import com.placement.ai.exception.AiServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AIService.
 * RestClient is mocked at the fluent-builder chain level.
 *
 * TC-AI-01  Happy path: valid JSON response → deserialized correctly
 * TC-AI-02  Markdown fences stripped before parsing
 * TC-AI-03  Empty body from provider → INVALID_RESPONSE exception
 * TC-AI-04  Timeout (ResourceAccessException) → retried, then RETRIES_EXHAUSTED
 * TC-AI-05  4xx CLIENT_ERROR → not retried, thrown immediately
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AIService Unit Tests")
class AIServiceTest {

    // ── mock chain: restClient.post().body().retrieve().body(String.class) ─────
    @Mock private RestClient                         restClient;
    @Mock private RestClient.RequestBodyUriSpec      uriSpec;
    @Mock private RestClient.RequestBodySpec         bodySpec;
    @Mock private RestClient.ResponseSpec            responseSpec;

    private AiProperties props;
    private AIService    aiService;
    private ObjectMapper objectMapper;

    private static final String SYSTEM = "You are a JSON API. Respond only with JSON.";
    private static final String USER   = "Generate something";

    @BeforeEach
    void setUp() {
        props = new AiProperties(
            "openai",
            "sk-test-key",
            "gpt-4o-mini",
            "https://api.openai.com/v1/chat/completions",
            30,
            3
        );
        objectMapper = new ObjectMapper();
        aiService    = new AIService(restClient, props, objectMapper);
    }

    // helper: wire mock chain and return a fixed provider response string
    private void stubProviderResponse(String providerJson) {
        when(restClient.post()).thenReturn(uriSpec);
        when(uriSpec.body(any(Map.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(providerJson);
    }

    /** Wraps content in an OpenAI-compatible envelope. */
    private static String envelope(String content) {
        return """
            {
              "choices": [
                { "message": { "content": "%s" } }
              ]
            }
            """.formatted(content.replace("\"", "\\\""));
    }

    // ── TC-AI-01 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-AI-01: Valid JSON response → deserialized correctly")
    void call_validResponse_deserializesCorrectly() {
        String innerJson  = "{\"score\":85,\"label\":\"good\"}";
        stubProviderResponse(envelope(innerJson));

        record Result(int score, String label) {}
        Result result = aiService.call(SYSTEM, USER, Result.class);

        assertThat(result.score()).isEqualTo(85);
        assertThat(result.label()).isEqualTo("good");
    }

    // ── TC-AI-02 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-AI-02: Markdown fences stripped before parsing")
    void call_markdownFences_strippedBeforeParse() {
        String innerJson = "```json\\n{\\\"value\\\":42}\\n```";
        // Build raw content string with actual newlines for stripMarkdownFences
        String rawContent = "```json\n{\"value\":42}\n```";
        // Build the provider envelope manually
        String providerJson = """
            {
              "choices": [
                { "message": { "content": "```json\\n{\\"value\\":42}\\n```" } }
              ]
            }
            """;
        when(restClient.post()).thenReturn(uriSpec);
        when(uriSpec.body(any(Map.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(providerJson);

        record Val(int value) {}
        Val result = aiService.call(SYSTEM, USER, Val.class);

        assertThat(result.value()).isEqualTo(42);
    }

    // ── TC-AI-03 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-AI-03: Empty body from provider → INVALID_RESPONSE")
    void call_emptyBody_throwsInvalidResponse() {
        stubProviderResponse("");

        assertThatThrownBy(() -> aiService.call(SYSTEM, USER, Object.class))
                .isInstanceOf(AiServiceException.class)
                .satisfies(e -> assertThat(((AiServiceException) e).getErrorType())
                        .isEqualTo(AiServiceException.AiErrorType.INVALID_RESPONSE));
    }

    // ── TC-AI-04 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-AI-04: Timeout → retried max-retries times → RETRIES_EXHAUSTED")
    void call_timeout_retriesExhausted() {
        // Override props with maxRetries=2 to keep the test fast
        AiProperties fastProps = new AiProperties("openai","sk-key","gpt-4o-mini",
                "https://api.openai.com",1,2);
        AIService fastService  = new AIService(restClient, fastProps, objectMapper);

        when(restClient.post()).thenReturn(uriSpec);
        when(uriSpec.body(any(Map.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.body(String.class))
                .thenThrow(new ResourceAccessException("connect timed out"));

        assertThatThrownBy(() -> fastService.call(SYSTEM, USER, Object.class))
                .isInstanceOf(AiServiceException.class)
                .satisfies(e -> assertThat(((AiServiceException) e).getErrorType())
                        .isEqualTo(AiServiceException.AiErrorType.RETRIES_EXHAUSTED));

        // Should have attempted exactly maxRetries times
        verify(restClient, times(2)).post();
    }

    // ── TC-AI-05 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-AI-05: 4xx CLIENT_ERROR → not retried, thrown immediately")
    void call_clientError_notRetried() {
        when(restClient.post()).thenReturn(uriSpec);
        when(uriSpec.body(any(Map.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenAnswer(inv -> {
            // Simulate the 4xx handler being triggered
            throw new AiServiceException(AiServiceException.AiErrorType.CLIENT_ERROR,
                    "HTTP 401 Unauthorized");
        });

        assertThatThrownBy(() -> aiService.call(SYSTEM, USER, Object.class))
                .isInstanceOf(AiServiceException.class)
                .satisfies(e -> assertThat(((AiServiceException) e).getErrorType())
                        .isEqualTo(AiServiceException.AiErrorType.CLIENT_ERROR));

        // Only 1 attempt — no retry on 4xx
        verify(restClient, times(1)).post();
    }
}
