package br.com.synge.seguranca.exceptions;

import io.javalin.http.HttpStatus;

public class InternalServerException extends RuntimeException {
    private final HttpStatus status;

    public InternalServerException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
    }

    public InternalServerException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
