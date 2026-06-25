package br.com.synge.seguranca.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // Default para 400, pode ser sobrescrito
public class DomainException extends RuntimeException {

    private final HttpStatus status;

    public DomainException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public DomainException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
