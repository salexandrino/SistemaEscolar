package br.com.synge.seguranca.exceptions;

import io.javalin.http.HttpStatus;

public class AuthorizationException extends RuntimeException {
    private final HttpStatus status;

    public AuthorizationException(String message) {
        super(message);
        this.status = HttpStatus.FORBIDDEN; // 403
    }

    public AuthorizationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
