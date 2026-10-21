package com.placement.ai;

import com.placement.ai.dto.InterviewGenerationRequest;
import com.placement.ai.dto.InterviewGenerationResponse;
import com.placement.ai.exception.AiServiceException;
import com.placement.questionbank.InterviewQuestion;
import com.placement.questionbank.QuestionBankService;
import com.placement.questionbank.dto.QuestionResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InterviewGenerator.
 * AIService and QuestionBankService are fully mocked.
 *
 * TC-IG-01  Valid request, saveToBank=false → questions returned, bank not called
 * TC-IG-02  saveToBank=true → questions persisted, savedQuestionIds returned
 * TC-IG-03  AI returns more questions than requested → list capped to count
 * TC-IG-04  Duplicate skills in request → deduplicated before prompt
 * TC-IG-05  AI fails (RETRIES_EXHAUSTED) → AiServiceException propagated
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InterviewGenerator Unit Tests")
class InterviewGeneratorTest {

    @Mock private AIService          aiService;
    @Mock private QuestionBankService questionBankService;

    @InjectMocks private InterviewGenerator generator;

    private static final String CALLER = "tpo@college.edu";

    private InterviewGenerationRequest baseRequest() {
        return InterviewGenerationRequest.builder()
                .targetRole("Software Engineer")
                .targetCompany("Google")
                .skills(List.of("Java", "Spring Boot", "System Design"))
                .difficulty(InterviewQuestion.Difficulty.MEDIUM)
                .questionType(InterviewQuestion.QuestionType.TECHNICAL)
                .count(3)
                .saveToBank(false)
                .year(2024)
                .build();
    }

    /** Builds a fake AiGenerationResult record. */
    private InterviewGenerator.AiGenerationResult fakeAiResult(int questionCount) {
        List<InterviewGenerator.AiQuestion> questions = new java.util.ArrayList<>();
        for (int i = 1; i <= questionCount; i++) {
            questions.add(new InterviewGenerator.AiQuestion(
                "Explain topic " + i + " in Java",
                "Answer to topic " + i,
                "Think about memory model",
                "How would you optimise it?",
                List.of("Java", "Spring Boot"),
                5
            ));
        }
        return new InterviewGenerator.AiGenerationResult(questions);
    }

    // ── TC-IG-01 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-IG-01: saveToBank=false → questions returned, bank not touched")
    void generate_noPersist_returnsQuestionsOnly() {
        when(aiService.call(anyString(), anyString(), any(Class.class)))
                .thenReturn(fakeAiResult(3));

        InterviewGenerationResponse resp =
                generator.generate(baseRequest(), CALLER);

        assertThat(resp.getQuestions()).hasSize(3);
        assertThat(resp.isSavedToBank()).isFalse();
        assertThat(resp.getSavedQuestionIds()).isEmpty();
        assertThat(resp.getTargetRole()).isEqualTo("Software Engineer");
        assertThat(resp.getDifficulty()).isEqualTo(InterviewQuestion.Difficulty.MEDIUM);

        verifyNoInteractions(questionBankService);
    }

    // ── TC-IG-02 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-IG-02: saveToBank=true → persisted, savedQuestionIds populated")
    void generate_saveToBank_persistsAndReturnsIds() {
        when(aiService.call(anyString(), anyString(), any(Class.class)))
                .thenReturn(fakeAiResult(2));

        // Mock bank save to return 2 responses with ids 10, 11
        QuestionResponse qr1 = QuestionResponse.builder().id(10L).build();
        QuestionResponse qr2 = QuestionResponse.builder().id(11L).build();
        when(questionBankService.saveAll(anyList())).thenReturn(List.of(qr1, qr2));

        InterviewGenerationRequest req = baseRequest().toBuilder()
                .count(2)
                .saveToBank(true)
                .build();

        InterviewGenerationResponse resp = generator.generate(req, CALLER);

        assertThat(resp.isSavedToBank()).isTrue();
        assertThat(resp.getSavedQuestionIds()).containsExactly(10L, 11L);
        assertThat(resp.getTotalGenerated()).isEqualTo(2);

        verify(questionBankService, times(1)).saveAll(anyList());
    }

    // ── TC-IG-03 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-IG-03: AI returns 5 questions but count=3 → capped to 3")
    void generate_aiReturnsExtra_cappedToRequestedCount() {
        when(aiService.call(anyString(), anyString(), any(Class.class)))
                .thenReturn(fakeAiResult(5));  // AI returned 5

        InterviewGenerationRequest req = baseRequest().toBuilder().count(3).build();
        InterviewGenerationResponse resp = generator.generate(req, CALLER);

        assertThat(resp.getQuestions()).hasSize(3);
        assertThat(resp.getTotalGenerated()).isEqualTo(3);
    }

    // ── TC-IG-04 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-IG-04: Duplicate skills in request → deduplicated in prompt")
    void generate_duplicateSkills_deduplicated() {
        // Capture the user prompt passed to aiService to verify deduplication
        ArgumentCaptor<String> userPromptCaptor = ArgumentCaptor.forClass(String.class);

        when(aiService.call(anyString(), userPromptCaptor.capture(), any(Class.class)))
                .thenReturn(fakeAiResult(1));

        InterviewGenerationRequest req = baseRequest().toBuilder()
                .skills(List.of("Java", "java", "JAVA", "Spring Boot"))  // 3 dupes of Java
                .count(1)
                .build();

        generator.generate(req, CALLER);

        String userPrompt = userPromptCaptor.getValue();
        // Should only mention Java once (case-insensitive dedup)
        long javaCount = java.util.Arrays.stream(userPrompt.split("[,:\\n]"))
                .map(String::trim)
                .filter(s -> s.equalsIgnoreCase("Java"))
                .count();
        assertThat(javaCount).isEqualTo(1);
    }

    // ── TC-IG-05 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-IG-05: AI fails with RETRIES_EXHAUSTED → exception propagated")
    void generate_aiFailure_propagatesException() {
        when(aiService.call(anyString(), anyString(), any(Class.class)))
                .thenThrow(new AiServiceException(
                        AiServiceException.AiErrorType.RETRIES_EXHAUSTED,
                        "AI provider failed after 3 attempts"));

        assertThatThrownBy(() -> generator.generate(baseRequest(), CALLER))
                .isInstanceOf(AiServiceException.class)
                .satisfies(e -> assertThat(((AiServiceException) e).getErrorType())
                        .isEqualTo(AiServiceException.AiErrorType.RETRIES_EXHAUSTED));

        verifyNoInteractions(questionBankService);
    }
}
