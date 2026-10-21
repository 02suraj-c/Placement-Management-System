package com.placement.interview.dto;

import java.time.LocalDateTime;

public class InterviewScheduleRequest {

    private Long applicationId;
    private String roundName;
    private LocalDateTime interviewDateTime;
    private String mode;
    private String location;

    public InterviewScheduleRequest() {
    }

    public InterviewScheduleRequest(Long applicationId, String roundName, LocalDateTime interviewDateTime, String mode, String location) {
        this.applicationId = applicationId;
        this.roundName = roundName;
        this.interviewDateTime = interviewDateTime;
        this.mode = mode;
        this.location = location;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getRoundName() {
        return roundName;
    }

    public void setRoundName(String roundName) {
        this.roundName = roundName;
    }

    public LocalDateTime getInterviewDateTime() {
        return interviewDateTime;
    }

    public void setInterviewDateTime(LocalDateTime interviewDateTime) {
        this.interviewDateTime = interviewDateTime;
    }

    public LocalDateTime getDateTime() {
        return interviewDateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.interviewDateTime = dateTime;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLink() {
        return location;
    }

    public void setLink(String link) {
        this.location = link;
    }
}
