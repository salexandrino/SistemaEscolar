package br.com.kutuar.seguranca.exceptions;

import io.javalin.http.HttpStatus;

public class NotFoundException extends ApiException {

    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(String message, HttpStatus status) {
        super(message, status);
    }
}