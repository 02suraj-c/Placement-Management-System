package com.placement.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.placement.ai.dto.InterviewGenerationRequest;
import com.placement.ai.dto.InterviewGenerationResponse;
import com.placement.ai.dto.InterviewGenerationResponse.GeneratedQuestion;
import com.placement.questionbank.InterviewQuestion;
import com.placement.questionbank.QuestionBankService;
import com.placement.questionbank.dto.QuestionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Generates interview questions via the AI provider and optionally persists
 * them to the question bank.
 *
 * Flow:
 *  1. Validate & sanitise the request (count cap, skill dedup).
 *  2. Build system + user prompts.
 *  3. Call {@link AIService} → JSON array of questions.
 *  4. Map AI JSON → {@link GeneratedQuestion} DTOs.
 *  5. If {@code saveToBank=true}, convert to {@link InterviewQuestion} entities
 *     via {@link QuestionBankService#saveAll} and collect IDs.
 *  6. Return {@link InterviewGenerationResponse}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewGenerator {

    private final AIService          aiService;
    private final QuestionBankService questionBankService;

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Generate interview questions from the given parameters.
     *
     * @param request        generation parameters (validated by caller)
     * @param callerUsername JWT subject — stored as {@code createdBy} when saving
     * @return generation response with questions (and saved IDs when persisted)
     */
    public InterviewGenerationResponse generate(InterviewGenerationRequest request,
                                                String callerUsername) {

        // Deduplicate + clean skills list (case-insensitive deduplication)
        java.util.Set<String> seen = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        List<String> skills = request.getSkills().stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .filter(seen::add)
                .toList();

        log.info("Generating {} {} questions for role='{}', company='{}', skills={}",
                request.getCount(), request.getDifficulty(),
                request.getTargetRole(), request.getTargetCompany(), skills);

        // ── Step 1: call AI ────────────────────────────────────────────────────
        String systemPrompt = buildSystemPrompt(request.getCount());
        String userPrompt   = buildUserPrompt(request, skills);

        // AI returns a JSON object wrapping an array: { "questions": [...] }
        AiGenerationResult aiResult = aiService.call(
                systemPrompt, userPrompt, AiGenerationResult.class);

        List<AiQuestion> aiQuestions = aiResult.questions() != null
                ? aiResult.questions()
                : List.of();

        // Cap to requested count (AI occasionally returns extra items)
        if (aiQuestions.size() > request.getCount()) {
            aiQuestions = aiQuestions.subList(0, request.getCount());
        }

        // ── Step 2: map to GeneratedQuestion DTOs ─────────────────────────────
        List<GeneratedQuestion> generated = aiQuestions.stream()
                .map(q -> toGeneratedQuestion(q, skills))
                .filter(Objects::nonNull)
                .toList();

        // ── Step 3: optionally persist ─────────────────────────────────────────
        List<Long> savedIds = List.of();
        if (request.isSaveToBank() && !generated.isEmpty()) {
            savedIds = persistToBank(generated, request, callerUsername);
            log.info("Persisted {} questions to bank by user={}", savedIds.size(), callerUsername);
        }

        return InterviewGenerationResponse.builder()
                .questions(generated)
                .totalGenerated(generated.size())
                .savedToBank(request.isSaveToBank() && !savedIds.isEmpty())
                .savedQuestionIds(savedIds)
                .targetRole(request.getTargetRole())
                .targetCompany(request.getTargetCompany())
                .difficulty(request.getDifficulty())
                .questionType(request.getQuestionType())
                .build();
    }

    // ── Prompt templates ───────────────────────────────────────────────────────

    private static String buildSystemPrompt(int count) {
        return """
            You are an expert technical interviewer for a college placement portal.

            Generate exactly %d interview questions. Return ONLY a JSON object \
            with this exact schema — no markdown, no prose:

            {
              "questions": [
                {
                  "questionText":    "<the interview question>",
                  "answer":          "<model answer, 3-5 sentences>",
                  "hint":            "<1-sentence approach hint for the candidate>",
                  "followUp":        "<one natural follow-up question>",
                  "relatedSkills":   ["<skill1>", "<skill2>"],
                  "estimatedMinutes": <integer 1-15>
                }
              ]
            }

            Rules:
            - Every question must be unique and non-trivial.
            - Answers must be accurate and concise.
            - relatedSkills must be a subset of the requested skills list.
            - estimatedMinutes reflects realistic interview time for that question.
            - If questionType is CODING, include a sample input/output in the answer.
            - If questionType is HR, questions must be behavioural (STAR-format answers).
            """.formatted(count);
    }

    private static String buildUserPrompt(InterviewGenerationRequest req,
                                          List<String> skills) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== GENERATION PARAMETERS ===\n");
        sb.append("Target Role    : ").append(req.getTargetRole()).append('\n');

        if (req.getTargetCompany() != null && !req.getTargetCompany().isBlank()) {
            sb.append("Target Company : ").append(req.getTargetCompany()).append('\n');
        }
        sb.append("Skills         : ").append(String.join(", ", skills)).append('\n');
        sb.append("Difficulty     : ").append(req.getDifficulty()).append('\n');
        sb.append("Question Type  : ").append(req.getQuestionType()).append('\n');
        sb.append("Count          : ").append(req.getCount()).append('\n');

        if (req.getYear() != null) {
            sb.append("Year Context   : ").append(req.getYear())
              .append(" (generate questions relevant to this year's hiring trends)\n");
        }

        sb.append("\nGenerate exactly ").append(req.getCount())
          .append(" questions. Return only the JSON object.");
        return sb.toString();
    }

    // ── Mapping helpers ────────────────────────────────────────────────────────

    private static GeneratedQuestion toGeneratedQuestion(AiQuestion q,
                                                          List<String> requestedSkills) {
        if (q == null || q.questionText() == null || q.questionText().isBlank()) {
            return null;   // skip malformed entries silently
        }

        // Intersect relatedSkills with the originally requested skills
        List<String> related = (q.relatedSkills() != null)
                ? q.relatedSkills().stream()
                    .filter(s -> requestedSkills.stream()
                            .anyMatch(rs -> rs.equalsIgnoreCase(s)))
                    .distinct().toList()
                : List.of();

        int minutes = (q.estimatedMinutes() > 0 && q.estimatedMinutes() <= 60)
                ? q.estimatedMinutes() : 5;

        return GeneratedQuestion.builder()
                .questionText(q.questionText().trim())
                .answer(q.answer() != null ? q.answer().trim() : "")
                .hint(q.hint() != null ? q.hint().trim() : "")
                .followUp(q.followUp() != null ? q.followUp().trim() : "")
                .relatedSkills(related)
                .estimatedMinutes(minutes)
                .build();
    }

    // ── Bank persistence ───────────────────────────────────────────────────────

    private List<Long> persistToBank(List<GeneratedQuestion> questions,
                                     InterviewGenerationRequest req,
                                     String createdBy) {

        List<InterviewQuestion> entities = questions.stream()
                .map(gq -> InterviewQuestion.builder()
                        .questionText(gq.getQuestionText())
                        .answer(gq.getAnswer())
                        .company(req.getTargetCompany())
                        .topic(String.join(", ", gq.getRelatedSkills().isEmpty()
                                ? req.getSkills() : gq.getRelatedSkills()))
                        .targetRole(req.getTargetRole())
                        .difficulty(req.getDifficulty())
                        .questionType(req.getQuestionType())
                        .tags(String.join(",", gq.getRelatedSkills()))
                        .yearAsked(req.getYear())
                        .source("AI_GENERATED")
                        .createdBy(createdBy)
                        .build())
                .toList();

        List<QuestionResponse> saved = questionBankService.saveAll(entities);
        return saved.stream()
                .map(QuestionResponse::getId)
                .toList();
    }

    // ── Internal AI schema ─────────────────────────────────────────────────────

    /** Outer wrapper: { "questions": [...] } */
    record AiGenerationResult(
            @JsonProperty("questions") List<AiQuestion> questions
    ) {}

    /** One AI-generated question entry. */
    record AiQuestion(
            @JsonProperty("questionText")     String       questionText,
            @JsonProperty("answer")           String       answer,
            @JsonProperty("hint")             String       hint,
            @JsonProperty("followUp")         String       followUp,
            @JsonProperty("relatedSkills")    List<String> relatedSkills,
            @JsonProperty("estimatedMinutes") int          estimatedMinutes
    ) {}
}
