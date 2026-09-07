package com.scheduler.email.exceptions.custom;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.ALREADY_REPORTED)
public class ResourceAlreadyDeletedException extends RuntimeException {
    public ResourceAlreadyDeletedException(String message) {
        super(message);
    }
}