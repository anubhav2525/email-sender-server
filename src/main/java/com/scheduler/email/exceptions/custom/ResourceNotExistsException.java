package com.scheduler.email.exceptions.custom;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotExistsException extends RuntimeException {
    public ResourceNotExistsException(String message) {
        super(message);
    }
}
