package com.example.inventory.DTO;

public class ErrorResponseDTO {
    private String errorCode;

    private String errorMessage;

    private long timestamp;

    public ErrorResponseDTO(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.timestamp = System.currentTimeMillis();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
