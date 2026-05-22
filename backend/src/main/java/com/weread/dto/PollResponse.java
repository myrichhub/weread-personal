package com.weread.dto;

public class PollResponse {
    public enum Status { WAITING, SCANNED, SUCCESS, EXPIRED }

    private Status status;
    private String token;

    public PollResponse(Status status, String token) {
        this.status = status;
        this.token = token;
    }

    public Status getStatus() { return status; }
    public String getToken() { return token; }
}
