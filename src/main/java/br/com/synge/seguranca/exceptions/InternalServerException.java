package br.com.synge.seguranca.exceptions;

import io.javalin.http.HttpStatus;

public class InternalServerException extends ApiException {

    public InternalServerException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalServerException(String message, HttpStatus status) {
        super(message, status);
    }
}