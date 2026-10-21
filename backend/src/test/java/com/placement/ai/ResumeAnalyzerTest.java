package com.placement.ai;

import com.placement.ai.dto.ResumeAnalysisRequest;
import com.placement.ai.dto.ResumeAnalysisResponse;
import com.placement.ai.exception.AiServiceException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ResumeAnalyzer.
 * Both AIService and ResumeTextExtractor are mocked — no real files or HTTP.
 *
 * TC-RA-01  Valid PDF → full analysis response with correct scoreBand
 * TC-RA-02  Score clamped to 100 when AI returns > 100
 * TC-RA-03  Oversized file (> 5 MB)  → IllegalArgumentException from extractor
 * TC-RA-04  Scanned PDF (< 50 chars) → IllegalArgumentException from extractor
 * TC-RA-05  AI returns malformed JSON → AiServiceException(INVALID_RESPONSE)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResumeAnalyzer Unit Tests")
class ResumeAnalyzerTest {

    @Mock private AIService           aiService;
    @Mock private ResumeTextExtractor textExtractor;

    @InjectMocks private ResumeAnalyzer analyzer;

    private static final MultipartFile DUMMY_PDF = new MockMultipartFile(
            "resume", "resume.pdf", "application/pdf",
            "dummy content".getBytes());

    private static final ResumeAnalysisRequest BASIC_REQUEST =
            ResumeAnalysisRequest.builder()
                    .targetRole("Software Engineer")
                    .targetCompany("Google")
                    .build();

    private static final ResumeTextExtractor.ExtractionResult GOOD_EXTRACTION =
            new ResumeTextExtractor.ExtractionResult(
                    "John Doe\nSoftware Engineer\nJava Spring Boot Docker Kubernetes",
                    "PDF", null);

    // ── TC-RA-01 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-RA-01: Valid PDF + AI response → correct DTO with scoreBand")
    void analyze_validPdf_returnsFullResponse() {
        when(textExtractor.extract(any())).thenReturn(GOOD_EXTRACTION);
        stubAiWithGoodResult();

        ResumeAnalysisResponse resp = analyzer.analyze(DUMMY_PDF, BASIC_REQUEST);

        assertThat(resp.getAtsScore()).isEqualTo(78);
        assertThat(resp.getScoreBand()).isEqualTo("Good");
        assertThat(resp.getMatchedKeywords()).contains("Java", "Spring Boot");
        assertThat(resp.getMissingKeywords()).contains("Kubernetes");
        assertThat(resp.getTopImprovements()).hasSize(5);
        assertThat(resp.getFileFormat()).isEqualTo("PDF");
        assertThat(resp.getWarning()).isNull();
    }

    // ── TC-RA-02 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-RA-02: AI returns atsScore=120 → clamped to 100, band=Excellent")
    void analyze_aiScoreOver100_clamped() {
        when(textExtractor.extract(any())).thenReturn(GOOD_EXTRACTION);
        stubAiWithScore(120);

        ResumeAnalysisResponse resp = analyzer.analyze(DUMMY_PDF, BASIC_REQUEST);

        assertThat(resp.getAtsScore()).isEqualTo(100);
        assertThat(resp.getScoreBand()).isEqualTo("Excellent");
    }

    // ── TC-RA-03 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-RA-03: Oversized file → IllegalArgumentException before AI call")
    void analyze_oversizedFile_throwsIllegalArgument() {
        when(textExtractor.extract(any()))
                .thenThrow(new IllegalArgumentException("File size exceeds the 5 MB limit"));

        assertThatThrownBy(() -> analyzer.analyze(DUMMY_PDF, BASIC_REQUEST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5 MB");

        verifyNoInteractions(aiService);
    }

    // ── TC-RA-04 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-RA-04: Scanned PDF yields < 50 chars → IllegalArgumentException")
    void analyze_scannedPdf_throwsIllegalArgument() {
        when(textExtractor.extract(any()))
                .thenThrow(new IllegalArgumentException(
                        "Could not extract readable text from the uploaded file"));

        assertThatThrownBy(() -> analyzer.analyze(DUMMY_PDF, BASIC_REQUEST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("readable text");

        verifyNoInteractions(aiService);
    }

    // ── TC-RA-05 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-RA-05: AI malformed JSON → AiServiceException(INVALID_RESPONSE)")
    void analyze_aiMalformedJson_throwsAiServiceException() {
        when(textExtractor.extract(any())).thenReturn(GOOD_EXTRACTION);
        when(aiService.call(anyString(), anyString(), any(Class.class)))
                .thenThrow(new AiServiceException(
                        AiServiceException.AiErrorType.INVALID_RESPONSE,
                        "JSON does not match expected schema"));

        assertThatThrownBy(() -> analyzer.analyze(DUMMY_PDF, BASIC_REQUEST))
                .isInstanceOf(AiServiceException.class)
                .satisfies(e -> assertThat(((AiServiceException) e).getErrorType())
                        .isEqualTo(AiServiceException.AiErrorType.INVALID_RESPONSE));
    }

    // ── Stub helpers ──────────────────────────────────────────────────────────

    private void stubAiWithGoodResult() {
        stubAiWithScore(78);
    }

    @SuppressWarnings("unchecked")
    private void stubAiWithScore(int score) {
        doReturn(buildAiResult(score)).when(aiService)
                .call(anyString(), anyString(), any(Class.class));
    }

    /**
     * Builds a fake AiResumeResult-equivalent object.
     * Because AiResumeResult is private to ResumeAnalyzer, we stub
     * aiService.call to return a Map that Jackson can deserialise
     * into the private record — here we replicate the expected DTO values
     * directly since we're mocking at the AIService boundary.
     *
     * In practice this stub makes AIService return an object that
     * ResumeAnalyzer's toResponse() maps to the final DTO.
     * We achieve this by injecting a pre-built ResumeAnalysisResponse
     * through a spy instead — see note below.
     *
     * SIMPLIFICATION ASSUMPTION: We use a @Spy on ResumeAnalyzer
     * and stub the public method directly for score-clamping tests,
     * while for the full-response test we verify DTO structure.
     */
    private ResumeAnalyzer.AiResumeResult buildAiResult(int score) {
        return new ResumeAnalyzer.AiResumeResult(
            score,
            score >= 80 ? "Excellent" : score >= 60 ? "Good" : score >= 40 ? "Needs Work" : "Poor",
            List.of("Java", "Spring Boot", "Docker"),
            List.of("Kubernetes", "Microservices"),
            Map.of(
                "Contact",        "Present and complete.",
                "Summary",        "Add a 2-3 sentence professional summary.",
                "Education",      "Good — include CGPA.",
                "Experience",     "Quantify achievements with metrics.",
                "Skills",         "Present and categorized well.",
                "Projects",       "Add GitHub links and tech stack details.",
                "Certifications", "Section not found — add this section.",
                "Achievements",   "Section not found — add this section."
            ),
            Map.of(
                "Contact", true, "Summary", false, "Education", true,
                "Experience", true, "Skills", true, "Projects", true,
                "Certifications", false, "Achievements", false
            ),
            List.of(
                "Add a professional summary highlighting your core expertise.",
                "Quantify achievements (e.g., 'Reduced latency by 30%').",
                "Include Kubernetes certification to match JD keywords.",
                "Add GitHub / portfolio links to project descriptions.",
                "List Certifications section with relevant online courses."
            )
        );
    }
}
