package br.com.synge.seguranca.exceptions;

import io.javalin.http.HttpStatus;

public class ValidationException extends RuntimeException {
    private final HttpStatus status;

    public ValidationException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST; // 400
    }

    public ValidationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
