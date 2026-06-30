package br.com.synge.seguranca.exceptions;

import io.javalin.http.HttpStatus;

public class BusinessException extends ApiException {

    public BusinessException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

    public BusinessException(String message, HttpStatus status) {
        super(message, status);
    }
}