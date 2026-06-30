package br.com.synge.seguranca.exceptions;

import io.javalin.http.HttpStatus;

public class ConflictException extends RuntimeException {
    private final HttpStatus status;

    public ConflictException(String message) {
        super(message);
        this.status = HttpStatus.CONFLICT; // 409
    }

    public ConflictException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
