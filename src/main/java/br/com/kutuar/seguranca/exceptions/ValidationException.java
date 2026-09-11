package br.com.kutuar.seguranca.exceptions;

import io.javalin.http.HttpStatus;

public class ValidationException extends ApiException {

    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public ValidationException(String message, HttpStatus status) {
        super(message, status);
    }
}