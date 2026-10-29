package com.placement.questionbank;

import com.placement.exception.ResourceNotFoundException;
import com.placement.questionbank.dto.QuestionFilterRequest;
import com.placement.questionbank.dto.QuestionRequest;
import com.placement.questionbank.dto.QuestionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for the Question Bank.
 * Roles that may WRITE: TPO, DEPUTY_TPO, COORDINATOR, FACULTY.
 * Role enforcement is done at the controller layer via @PreAuthorize.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QuestionBankService {

    private final InterviewQuestionRepository questionRepository;

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Transactional
    public QuestionResponse create(QuestionRequest req, String createdByUsername) {
        log.info("Creating question by user={}, topic={}", createdByUsername, req.getTopic());

        InterviewQuestion entity = InterviewQuestion.builder()
                .questionText(req.getQuestionText())
                .answer(req.getAnswer())
                .company(req.getCompany())
                .topic(req.getTopic())
                .targetRole(req.getTargetRole())
                .difficulty(req.getDifficulty())
                .questionType(req.getQuestionType())
                .tags(req.getTags())
                .yearAsked(req.getYearAsked())
                .marks(req.getMarks() != null ? req.getMarks() : 10)
                .source(req.getSource() != null ? req.getSource() : "MANUAL")
                .createdBy(createdByUsername)
                .build();

        return QuestionResponse.from(questionRepository.save(entity));
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    public QuestionResponse getById(Long id) {
        return QuestionResponse.from(findOrThrow(id));
    }

    /**
     * Paginated list with optional combined filters.
     */
    public Page<QuestionResponse> filter(QuestionFilterRequest filter, Pageable pageable) {
        return questionRepository
                .findAll(InterviewQuestionSpec.withFilters(filter), pageable)
                .map(QuestionResponse::from);
    }

    /** All distinct company names (for frontend dropdowns). */
    public List<String> getDistinctCompanies() {
        return questionRepository.findDistinctCompanies();
    }

    /** All distinct topic names (for frontend dropdowns). */
    public List<String> getDistinctTopics() {
        return questionRepository.findDistinctTopics();
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Transactional
    public QuestionResponse update(Long id, QuestionRequest req, String updatedByUsername) {
        log.info("Updating question id={} by user={}", id, updatedByUsername);

        InterviewQuestion entity = findOrThrow(id);
        entity.setQuestionText(req.getQuestionText());
        entity.setAnswer(req.getAnswer());
        entity.setCompany(req.getCompany());
        entity.setTopic(req.getTopic());
        entity.setTargetRole(req.getTargetRole());
        entity.setDifficulty(req.getDifficulty());
        entity.setQuestionType(req.getQuestionType());
        entity.setTags(req.getTags());
        entity.setYearAsked(req.getYearAsked());
        if (req.getMarks() != null) {
            entity.setMarks(req.getMarks());
        }
        if (req.getSource() != null) {
            entity.setSource(req.getSource());
        }
        // createdBy intentionally NOT updated

        return QuestionResponse.from(questionRepository.save(entity));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        log.info("Deleting question id={}", id);
        InterviewQuestion entity = findOrThrow(id);
        questionRepository.delete(entity);
    }

    // ── BULK SAVE (used by InterviewGenerator in Step 4) ─────────────────────

    @Transactional
    public List<QuestionResponse> saveAll(List<InterviewQuestion> questions) {
        return questionRepository.saveAll(questions)
                .stream()
                .map(QuestionResponse::from)
                .toList();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private InterviewQuestion findOrThrow(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "InterviewQuestion not found with id: " + id));
    }
}
