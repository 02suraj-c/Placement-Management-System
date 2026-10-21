package com.placement.questionbank;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link InterviewQuestion}.
 * JpaSpecificationExecutor enables dynamic filter combinations.
 */
@Repository
public interface InterviewQuestionRepository
        extends JpaRepository<InterviewQuestion, Long>,
                JpaSpecificationExecutor<InterviewQuestion> {

    // ── named queries kept intentionally simple; complex filters go via Spec ──

    Page<InterviewQuestion> findByCompanyIgnoreCase(String company, Pageable pageable);

    Page<InterviewQuestion> findByTopicIgnoreCase(String topic, Pageable pageable);

    Page<InterviewQuestion> findByDifficulty(
            InterviewQuestion.Difficulty difficulty, Pageable pageable);

    Page<InterviewQuestion> findByQuestionType(
            InterviewQuestion.QuestionType questionType, Pageable pageable);

    /**
     * Distinct companies available in the bank (for frontend dropdowns).
     */
    @Query("SELECT DISTINCT q.company FROM InterviewQuestion q " +
           "WHERE q.company IS NOT NULL ORDER BY q.company")
    List<String> findDistinctCompanies();

    /**
     * Distinct topics available in the bank.
     */
    @Query("SELECT DISTINCT q.topic FROM InterviewQuestion q ORDER BY q.topic")
    List<String> findDistinctTopics();

    /**
     * Full-text search across question text and tags (LIKE-based; replace with
     * a full-text index for production).
     */
    @Query("SELECT q FROM InterviewQuestion q WHERE " +
           "LOWER(q.questionText) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(q.tags)         LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<InterviewQuestion> searchByKeyword(@Param("keyword") String keyword,
                                            Pageable pageable);
}
