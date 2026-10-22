package com.placement.resume;

import com.placement.dto.ApiResponse;
import com.placement.resume.dto.ResumeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for Student Resume Management.
 * Base path: /api/resumes
 */
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    /**
     * Upload or replace student resume.
     * POST /api/resumes/upload
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ResumeResponse>> uploadResume(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        ResumeResponse response = resumeService.uploadResume(file, userDetails.getUsername());
        return new ResponseEntity<>(ApiResponse.success(response, "Resume uploaded successfully"), HttpStatus.CREATED);
    }

    /**
     * Get active resume metadata for authenticated student.
     * GET /api/resumes/me
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ResumeResponse>> getCurrentResume(
            @AuthenticationPrincipal UserDetails userDetails) {

        ResumeResponse response = resumeService.getCurrentStudentResume(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Resume details fetched successfully"));
    }

    /**
     * Download active resume file for authenticated student.
     * GET /api/resumes/me/download
     */
    @GetMapping("/me/download")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Resource> downloadCurrentResume(
            @AuthenticationPrincipal UserDetails userDetails) {

        ResumeService.ResumeDownloadInfo downloadInfo = resumeService.getCurrentStudentResumeFile(userDetails.getUsername());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(downloadInfo.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadInfo.originalFilename() + "\"")
                .body(downloadInfo.resource());
    }

    /**
     * Get active resume metadata for any student by student ID.
     * Students may only view their own resume metadata; staff/faculty may view any student's resume.
     * GET /api/resumes/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'HOD', 'OFFICER', 'PLACEMENT_OFFICER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ResumeResponse>> getResumeByStudentId(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        ResumeResponse response = resumeService.getResumeByStudentId(studentId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Resume details fetched successfully"));
    }

    /**
     * Download resume file by resume ID.
     * Students may only download their own resume; staff/faculty may download any student's resume.
     * GET /api/resumes/download/{id}
     */
    @GetMapping("/download/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'HOD', 'OFFICER', 'PLACEMENT_OFFICER', 'ADMIN')")
    public ResponseEntity<Resource> downloadResumeById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        ResumeService.ResumeDownloadInfo downloadInfo = resumeService.getResumeFile(id, userDetails.getUsername());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(downloadInfo.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadInfo.originalFilename() + "\"")
                .body(downloadInfo.resource());
    }
}
