package com.placement.ai;

import com.placement.ai.exception.AiServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Extracts plain text from uploaded PDF or DOCX resumes.
 *
 * Constraints enforced before extraction:
 *   • Max file size : 5 MB
 *   • Allowed types : application/pdf | application/vnd.openxmlformats-officedocument.wordprocessingml.document
 *   • Min text yield: 50 characters (guards against image-only / scanned PDFs)
 *   • Max text size : 15 000 characters (prevents prompt injection / oversized AI calls)
 */
@Component
@Slf4j
public class ResumeTextExtractor {

    // ── Constants ──────────────────────────────────────────────────────────────
    private static final long   MAX_FILE_BYTES    = 5 * 1024 * 1024L;   // 5 MB
    private static final int    MIN_TEXT_CHARS    = 50;
    private static final int    MAX_TEXT_CHARS    = 15_000;

    private static final String PDF_MIME   = "application/pdf";
    private static final String DOCX_MIME  =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Validates and extracts text from a resume file.
     *
     * @param file the uploaded MultipartFile
     * @return {@link ExtractionResult} containing extracted text and format label
     * @throws IllegalArgumentException on size / type / empty-text violations
     * @throws AiServiceException       if IO fails during extraction
     */
    public ExtractionResult extract(MultipartFile file) {
        validateFile(file);

        String contentType = resolveContentType(file);
        String text;

        try {
            text = switch (contentType) {
                case PDF_MIME  -> extractFromPdf(file);
                case DOCX_MIME -> extractFromDocx(file);
                default        -> throw new IllegalArgumentException(
                        "Unsupported file type: " + contentType +
                        ". Only PDF and DOCX are accepted.");
            };
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Text extraction failed for file={}", file.getOriginalFilename(), e);
            throw new AiServiceException(
                    AiServiceException.AiErrorType.INVALID_RESPONSE,
                    "Failed to extract text from resume: " + e.getMessage(), e);
        }

        text = text.strip();

        if (text.length() < MIN_TEXT_CHARS) {
            throw new IllegalArgumentException(
                    "Could not extract readable text from the uploaded file. " +
                    "If this is a scanned PDF, please upload a text-based version.");
        }

        // Truncate to MAX_TEXT_CHARS to keep AI prompt size bounded
        String warning = null;
        if (text.length() > MAX_TEXT_CHARS) {
            log.warn("Resume text truncated from {} to {} chars", text.length(), MAX_TEXT_CHARS);
            text    = text.substring(0, MAX_TEXT_CHARS);
            warning = "Resume text was very long and has been truncated to " +
                      MAX_TEXT_CHARS + " characters for analysis.";
        }

        String format = contentType.equals(PDF_MIME) ? "PDF" : "DOCX";
        log.info("Extracted {} chars from {} ({})", text.length(),
                file.getOriginalFilename(), format);

        return new ExtractionResult(text, format, warning);
    }

    // ── Validation ─────────────────────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file must not be empty.");
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            throw new IllegalArgumentException(
                    "File size " + (file.getSize() / 1024) + " KB exceeds the 5 MB limit.");
        }
        String ct = resolveContentType(file);
        if (!ct.equals(PDF_MIME) && !ct.equals(DOCX_MIME)) {
            throw new IllegalArgumentException(
                    "Invalid file type '" + ct + "'. Only PDF and DOCX are accepted.");
        }
    }

    /**
     * Resolves content type from declared MIME and file extension fallback.
     * Browsers sometimes report 'application/octet-stream' for DOCX.
     */
    private String resolveContentType(MultipartFile file) {
        String ct = file.getContentType();
        if (ct == null || ct.equals("application/octet-stream")) {
            String name = file.getOriginalFilename();
            if (name != null && name.toLowerCase().endsWith(".pdf"))  return PDF_MIME;
            if (name != null && name.toLowerCase().endsWith(".docx")) return DOCX_MIME;
        }
        return ct != null ? ct : "application/octet-stream";
    }

    // ── PDF extraction ─────────────────────────────────────────────────────────

    private String extractFromPdf(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            if (doc.isEncrypted()) {
                throw new IllegalArgumentException(
                        "The PDF is password-protected. Please upload an unprotected version.");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(doc);
        }
    }

    // ── DOCX extraction ────────────────────────────────────────────────────────

    private String extractFromDocx(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
             XWPFDocument doc = new XWPFDocument(is)) {

            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph para : doc.getParagraphs()) {
                String text = para.getText();
                if (text != null && !text.isBlank()) {
                    sb.append(text).append('\n');
                }
            }
            // Also read text from tables
            doc.getTables().forEach(table ->
                table.getRows().forEach(row ->
                    row.getTableCells().forEach(cell -> {
                        String t = cell.getText();
                        if (t != null && !t.isBlank()) sb.append(t).append(' ');
                    })
                )
            );
            return sb.toString();
        }
    }

    // ── Result record ──────────────────────────────────────────────────────────

    /**
     * Value object returned by {@link #extract}.
     *
     * @param text    cleaned resume text (max 15 000 chars)
     * @param format  "PDF" | "DOCX"
     * @param warning non-null if text was truncated; null otherwise
     */
    public record ExtractionResult(String text, String format, String warning) {}
}
