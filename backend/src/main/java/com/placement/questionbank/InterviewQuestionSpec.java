package com.placement.questionbank;

import com.placement.questionbank.dto.QuestionFilterRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for dynamic, combinatorial filtering of InterviewQuestion.
 * Used by QuestionBankService to avoid N query methods.
 */
public final class InterviewQuestionSpec {

    private InterviewQuestionSpec() {}

    public static Specification<InterviewQuestion> withFilters(QuestionFilterRequest f) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(f.getCompany())) {
                predicates.add(cb.like(
                    cb.lower(root.get("company")),
                    "%" + f.getCompany().toLowerCase() + "%"
                ));
            }
            if (hasText(f.getTopic())) {
                predicates.add(cb.like(
                    cb.lower(root.get("topic")),
                    "%" + f.getTopic().toLowerCase() + "%"
                ));
            }
            if (hasText(f.getTargetRole())) {
                predicates.add(cb.like(
                    cb.lower(root.get("targetRole")),
                    "%" + f.getTargetRole().toLowerCase() + "%"
                ));
            }
            if (f.getDifficulty() != null) {
                predicates.add(cb.equal(root.get("difficulty"), f.getDifficulty()));
            }
            if (f.getQuestionType() != null) {
                predicates.add(cb.equal(root.get("questionType"), f.getQuestionType()));
            }
            if (f.getYearAsked() != null) {
                predicates.add(cb.equal(root.get("yearAsked"), f.getYearAsked()));
            }
            if (hasText(f.getKeyword())) {
                String kw = "%" + f.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("questionText")), kw),
                    cb.like(cb.lower(root.get("tags")), kw)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
