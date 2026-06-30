package br.com.synge.seguranca.exceptions;

import io.javalin.http.HttpStatus;

public class NotFoundException extends RuntimeException {
    private final HttpStatus status;

    public NotFoundException(String message) {
        super(message);
        this.status = HttpStatus.NOT_FOUND; // 404
    }

    public NotFoundException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
