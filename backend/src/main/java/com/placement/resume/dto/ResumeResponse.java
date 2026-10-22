package com.placement.resume.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResumeResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private String rollNumber;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private String formattedSize;
    private boolean active;
    private LocalDateTime uploadedAt;
    private String downloadUrl;
}
