package com.scheduler.email.exceptions.custom;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
public class HttpOperationNotAllowedException extends RuntimeException {
    public HttpOperationNotAllowedException(String message) {
        super(message);
    }
}
