package com.placement.application.dto;

public class ApplicationRequest {

    private Long studentId;
    private Long driveId;

    public ApplicationRequest() {
    }

    public ApplicationRequest(Long studentId, Long driveId) {
        this.studentId = studentId;
        this.driveId = driveId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getDriveId() {
        return driveId;
    }

    public void setDriveId(Long driveId) {
        this.driveId = driveId;
    }
}
