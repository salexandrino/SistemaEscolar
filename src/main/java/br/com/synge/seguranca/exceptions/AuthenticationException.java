package br.com.synge.seguranca.exceptions;

import io.javalin.http.HttpStatus;

public class AuthenticationException extends RuntimeException {
    private final HttpStatus status;

    public AuthenticationException(String message) {
        super(message);
        this.status = HttpStatus.UNAUTHORIZED; // 401
    }

    public AuthenticationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
