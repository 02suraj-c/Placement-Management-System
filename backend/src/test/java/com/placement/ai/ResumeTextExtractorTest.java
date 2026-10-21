package com.placement.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ResumeTextExtractor — guards on file validation.
 * No Spring context, no real file I/O (PDFBox/POI) needed for guard tests.
 *
 * TC-EX-01  Null / empty file → IllegalArgumentException
 * TC-EX-02  File > 5 MB      → IllegalArgumentException (size check)
 * TC-EX-03  Wrong MIME (PNG)  → IllegalArgumentException (type check)
 * TC-EX-04  .docx uploaded with octet-stream MIME → resolved via extension
 * TC-EX-05  Valid small PDF (real bytes) → extraction succeeds with text
 */
@DisplayName("ResumeTextExtractor Unit Tests")
class ResumeTextExtractorTest {

    private final ResumeTextExtractor extractor = new ResumeTextExtractor();

    // ── TC-EX-01 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-EX-01: Empty MultipartFile → IllegalArgumentException")
    void extract_emptyFile_throws() {
        MockMultipartFile empty = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> extractor.extract(empty))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be empty");
    }

    // ── TC-EX-02 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-EX-02: File > 5 MB → IllegalArgumentException with KB info")
    void extract_oversizedFile_throws() {
        byte[] big = new byte[6 * 1024 * 1024]; // 6 MB
        MockMultipartFile file = new MockMultipartFile(
                "file", "big.pdf", "application/pdf", big);

        assertThatThrownBy(() -> extractor.extract(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5 MB");
    }

    // ── TC-EX-03 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-EX-03: PNG file → IllegalArgumentException (wrong type)")
    void extract_wrongMimeType_throws() {
        MockMultipartFile png = new MockMultipartFile(
                "file", "photo.png", "image/png", "fakepng".getBytes());

        assertThatThrownBy(() -> extractor.extract(png))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid file type");
    }

    // ── TC-EX-04 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-EX-04: .docx with octet-stream MIME → resolved via extension")
    void extract_docxWithOctetStream_resolvedByExtension() {
        // Should NOT throw on the MIME resolution step; it will fail later on
        // POI parsing the dummy bytes — but that's a different exception path.
        MockMultipartFile docx = new MockMultipartFile(
                "file", "resume.docx", "application/octet-stream",
                "PKFAKE".getBytes()); // not a real DOCX but passes type check

        // Verify that the MIME check passed (does not throw "Invalid file type")
        assertThatThrownBy(() -> extractor.extract(docx))
                .hasMessageNotContaining("Invalid file type");
    }

    // ── TC-EX-05 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-EX-05: Minimal real PDF bytes → extraction succeeds")
    void extract_minimalRealPdf_returnsText() throws Exception {
        // Minimal valid single-page PDF with embedded text "Hello World"
        byte[] pdfBytes = buildMinimalPdf();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", pdfBytes);

        ResumeTextExtractor.ExtractionResult result = extractor.extract(file);

        assertThat(result.format()).isEqualTo("PDF");
        assertThat(result.text()).isNotBlank();
        assertThat(result.warning()).isNull();
    }

    // ── helper: build a minimal text-bearing PDF ──────────────────────────────

    /**
     * Builds a bare-minimum single-page PDF with the text
     * "This is a sample resume with enough words to pass the 50 char check."
     * Uses raw PDF syntax — no dependency on PDFBox for generation.
     */
    private static byte[] buildMinimalPdf() {
        // Raw PDF with embedded text stream
        String content = "This is a sample resume with enough words to pass the fifty character guard.";
        String pdf = "%PDF-1.4\n" +
            "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n" +
            "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n" +
            "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792]\n" +
            "   /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n" +
            "4 0 obj\n<< /Length " + (content.length() + 40) + " >>\nstream\n" +
            "BT /F1 12 Tf 72 720 Td (" + content + ") Tj ET\n" +
            "endstream\nendobj\n" +
            "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n" +
            "xref\n0 6\n0000000000 65535 f\n" +
            "0000000009 00000 n\n0000000062 00000 n\n0000000119 00000 n\n" +
            "0000000274 00000 n\n0000000390 00000 n\n" +
            "trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n459\n%%EOF";
        return pdf.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
    }
}
