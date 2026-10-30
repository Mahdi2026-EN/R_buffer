package com.example.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * An expected error that is shown to the client with a stable "code"
 * (the front end uses the code to display a translated message).
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
