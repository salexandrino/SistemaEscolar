package br.com.synge.seguranca.exceptions;

import io.javalin.http.HttpStatus;

public class AuthorizationException extends ApiException {

    public AuthorizationException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }

    public AuthorizationException(String message, HttpStatus status) {
        super(message, status);
    }
}