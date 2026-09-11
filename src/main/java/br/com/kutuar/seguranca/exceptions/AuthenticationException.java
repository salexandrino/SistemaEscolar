package br.com.kutuar.seguranca.exceptions;

import io.javalin.http.HttpStatus;

public class AuthenticationException extends ApiException {

    public AuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }

    public AuthenticationException(String message, HttpStatus status) {
        super(message, status);
    }
}