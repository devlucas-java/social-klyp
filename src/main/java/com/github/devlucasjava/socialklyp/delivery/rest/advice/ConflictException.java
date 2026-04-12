package com.github.devlucasjava.socialklyp.delivery.rest.advice;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
