package com.example.gaokao.common.exception;

public class DifyApiException extends RuntimeException {

    private final String errorCode;

    public DifyApiException(String message) {
        super(message);
        this.errorCode = "DIFY_ERROR";
    }

    public DifyApiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "DIFY_ERROR";
    }

    public DifyApiException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public DifyApiException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
