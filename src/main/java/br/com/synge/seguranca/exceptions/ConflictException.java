package br.com.synge.seguranca.exceptions;

import io.javalin.http.HttpStatus;

public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

    public ConflictException(String message, HttpStatus status) {
        super(message, status);
    }
}