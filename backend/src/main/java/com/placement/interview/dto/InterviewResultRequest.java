package com.placement.interview.dto;

public class InterviewResultRequest {

    private String result;
    private Double score;
    private String remarks;

    public InterviewResultRequest() {
    }

    public InterviewResultRequest(String result, Double score, String remarks) {
        this.result = result;
        this.score = score;
        this.remarks = remarks;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
