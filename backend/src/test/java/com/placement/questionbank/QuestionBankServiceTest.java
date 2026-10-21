package com.placement.questionbank;

import com.placement.exception.ResourceNotFoundException;
import com.placement.questionbank.dto.QuestionFilterRequest;
import com.placement.questionbank.dto.QuestionRequest;
import com.placement.questionbank.dto.QuestionResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QuestionBankService.
 * AI / DB are fully mocked — zero Spring context overhead.
 *
 * TC-QB-01  Create a valid question → saved and returned with id
 * TC-QB-02  Get by valid id          → correct response
 * TC-QB-03  Get by unknown id        → ResourceNotFoundException
 * TC-QB-04  Filter by difficulty     → delegates to spec, returns page
 * TC-QB-05  Delete by valid id       → repository.delete called once
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("QuestionBankService Unit Tests")
class QuestionBankServiceTest {

    @Mock
    private InterviewQuestionRepository questionRepository;

    @InjectMocks
    private QuestionBankService service;

    // ── shared fixtures ────────────────────────────────────────────────────────
    private QuestionRequest validRequest;
    private InterviewQuestion savedEntity;

    @BeforeEach
    void setUp() {
        validRequest = QuestionRequest.builder()
                .questionText("Explain JVM memory model")
                .answer("Heap, Stack, Metaspace …")
                .company("Google")
                .topic("Java")
                .targetRole("Software Engineer")
                .difficulty(InterviewQuestion.Difficulty.MEDIUM)
                .questionType(InterviewQuestion.QuestionType.TECHNICAL)
                .tags("jvm,memory,java")
                .yearAsked(2024)
                .source("MANUAL")
                .build();

        savedEntity = InterviewQuestion.builder()
                .id(1L)
                .questionText(validRequest.getQuestionText())
                .answer(validRequest.getAnswer())
                .company(validRequest.getCompany())
                .topic(validRequest.getTopic())
                .targetRole(validRequest.getTargetRole())
                .difficulty(validRequest.getDifficulty())
                .questionType(validRequest.getQuestionType())
                .tags(validRequest.getTags())
                .yearAsked(validRequest.getYearAsked())
                .source("MANUAL")
                .createdBy("tpo@college.edu")
                .build();
    }

    // ── TC-QB-01 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-QB-01: Create valid question → returns response with id")
    void create_validRequest_returnsResponseWithId() {
        when(questionRepository.save(any(InterviewQuestion.class))).thenReturn(savedEntity);

        QuestionResponse response = service.create(validRequest, "tpo@college.edu");

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getQuestionText()).isEqualTo("Explain JVM memory model");
        assertThat(response.getCreatedBy()).isEqualTo("tpo@college.edu");
        verify(questionRepository, times(1)).save(any(InterviewQuestion.class));
    }

    // ── TC-QB-02 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-QB-02: Get by valid id → correct response")
    void getById_validId_returnsQuestion() {
        when(questionRepository.findById(1L)).thenReturn(Optional.of(savedEntity));

        QuestionResponse response = service.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTopic()).isEqualTo("Java");
        assertThat(response.getDifficulty()).isEqualTo(InterviewQuestion.Difficulty.MEDIUM);
    }

    // ── TC-QB-03 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-QB-03: Get by unknown id → ResourceNotFoundException")
    void getById_unknownId_throwsNotFound() {
        when(questionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ── TC-QB-04 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-QB-04: Filter by HARD difficulty → page returned")
    @SuppressWarnings("unchecked")
    void filter_byDifficulty_returnsPage() {
        Page<InterviewQuestion> entityPage = new PageImpl<>(List.of(savedEntity));
        when(questionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(entityPage);

        QuestionFilterRequest filter = QuestionFilterRequest.builder()
                .difficulty(InterviewQuestion.Difficulty.HARD)
                .build();

        Page<QuestionResponse> result = service.filter(filter, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(questionRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    // ── TC-QB-05 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-QB-05: Delete by valid id → repository.delete called once")
    void delete_validId_callsRepositoryDelete() {
        when(questionRepository.findById(1L)).thenReturn(Optional.of(savedEntity));
        doNothing().when(questionRepository).delete(savedEntity);

        service.delete(1L);

        verify(questionRepository, times(1)).delete(savedEntity);
    }
}
