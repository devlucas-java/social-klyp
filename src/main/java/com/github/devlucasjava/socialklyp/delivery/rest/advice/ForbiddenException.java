package com.github.devlucasjava.socialklyp.delivery.rest.advice;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}