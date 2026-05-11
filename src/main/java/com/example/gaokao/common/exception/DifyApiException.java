package com.example.gaokao.common.exception;

public class DifyApiException extends RuntimeException {

    public DifyApiException(String message) {
        super(message);
    }

    public DifyApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
