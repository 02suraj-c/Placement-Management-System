package com.placement.resume;

import com.placement.exception.ResourceNotFoundException;
import com.placement.resume.dto.ResumeResponse;
import com.placement.student.Student;
import com.placement.student.StudentRepository;
import com.placement.user.User;
import com.placement.user.UserRepository;
import com.placement.user.UserRole;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L; // 5 MB
    private static final List<String> ALLOWED_EXTENSIONS = List.of(".pdf", ".docx");

    private final ResumeRepository resumeRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir:uploads/resumes}")
    private String uploadDir;

    private Path storageLocation;

    @PostConstruct
    public void init() {
        this.storageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageLocation);
            log.info("Resume storage directory initialized at: {}", this.storageLocation);
        } catch (IOException e) {
            log.error("Could not initialize resume storage directory: {}", this.storageLocation, e);
            throw new IllegalStateException("Could not initialize resume storage directory", e);
        }
    }

    @Transactional
    public ResumeResponse uploadResume(MultipartFile file, String userEmail) {
        log.info("Processing resume upload for user: {}", userEmail);

        Student student = studentRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + userEmail));

        validateFile(file);

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume"
        );

        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("Filename contains invalid path sequence: " + originalFilename);
        }

        String extension = extractExtension(originalFilename);
        String safeStoredFileName = UUID.randomUUID().toString() + extension;
        Path targetPath = this.storageLocation.resolve(safeStoredFileName).normalize();

        // Ensure target path remains within storage location (prevent path traversal)
        if (!targetPath.startsWith(this.storageLocation)) {
            throw new IllegalArgumentException("Cannot store file outside current storage directory");
        }

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Resume binary securely saved as: {}", safeStoredFileName);
        } catch (IOException ex) {
            log.error("Failed to store resume file for user: {}", userEmail, ex);
            throw new IllegalStateException("Failed to store resume file on disk: " + ex.getMessage(), ex);
        }

        // Deactivate previously active resume for this student
        resumeRepository.findByStudentIdAndActiveTrue(student.getId()).ifPresent(existing -> {
            existing.setActive(false);
            resumeRepository.save(existing);
            log.info("Deactivated previous resume id: {} for student: {}", existing.getId(), student.getId());
        });

        StudentResume resume = StudentResume.builder()
                .student(student)
                .originalFileName(originalFilename)
                .storedFileName(safeStoredFileName)
                .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .fileSize(file.getSize())
                .storagePath(targetPath.toString())
                .active(true)
                .build();

        StudentResume savedResume = resumeRepository.save(resume);
        log.info("Saved resume metadata with ID: {} for student: {}", savedResume.getId(), student.getId());

        return mapToResponse(savedResume);
    }

    @Transactional(readOnly = true)
    public ResumeResponse getCurrentStudentResume(String userEmail) {
        StudentResume resume = resumeRepository.findByStudentUserEmailAndActiveTrue(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("No active resume found for user: " + userEmail));
        return mapToResponse(resume);
    }

    @Transactional(readOnly = true)
    public ResumeResponse getResumeByStudentId(Long studentId, String currentUserEmail) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentUserEmail));

        // Enforce ownership if caller is a student
        if (currentUser.getRole() == UserRole.ROLE_STUDENT) {
            if (!student.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You are not authorized to access another student's resume");
            }
        }

        StudentResume resume = resumeRepository.findByStudentIdAndActiveTrue(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Active resume not found for student id: " + studentId));

        return mapToResponse(resume);
    }

    @Transactional(readOnly = true)
    public ResumeDownloadInfo getResumeFile(Long resumeId, String currentUserEmail) {
        StudentResume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", resumeId));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentUserEmail));

        // Enforce ownership if caller is a student
        if (currentUser.getRole() == UserRole.ROLE_STUDENT) {
            if (!resume.getStudent().getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You are not authorized to download another student's resume");
            }
        }

        try {
            Path filePath = Paths.get(resume.getStoragePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Resume file not found on disk or is not readable");
            }

            return new ResumeDownloadInfo(resource, resume.getOriginalFileName(), resume.getContentType());
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Error retrieving resume file: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ResumeDownloadInfo getCurrentStudentResumeFile(String userEmail) {
        StudentResume resume = resumeRepository.findByStudentUserEmailAndActiveTrue(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("No active resume found for user: " + userEmail));

        return getResumeFile(resume.getId(), userEmail);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Resume file size (" + (file.getSize() / 1024 / 1024) + "MB) exceeds maximum limit of 5MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String ext = extractExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Unsupported file extension: " + ext + ". Allowed formats are PDF (.pdf) and Word (.docx).");
        }
    }

    private String extractExtension(String filename) {
        int dotIdx = filename.lastIndexOf('.');
        if (dotIdx == -1 || dotIdx == filename.length() - 1) {
            throw new IllegalArgumentException("File must have a valid extension (.pdf or .docx)");
        }
        return filename.substring(dotIdx);
    }

    public ResumeResponse mapToResponse(StudentResume resume) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .studentId(resume.getStudent() != null ? resume.getStudent().getId() : null)
                .studentName(resume.getStudent() != null && resume.getStudent().getUser() != null ? resume.getStudent().getUser().getName() : null)
                .rollNumber(resume.getStudent() != null ? resume.getStudent().getRollNumber() : null)
                .originalFileName(resume.getOriginalFileName())
                .contentType(resume.getContentType())
                .fileSize(resume.getFileSize())
                .formattedSize(formatFileSize(resume.getFileSize()))
                .active(resume.isActive())
                .uploadedAt(resume.getUploadedAt())
                .downloadUrl("/api/resumes/download/" + resume.getId())
                .build();
    }

    private String formatFileSize(Long bytes) {
        if (bytes == null) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }

    public record ResumeDownloadInfo(Resource resource, String originalFilename, String contentType) {}
}
