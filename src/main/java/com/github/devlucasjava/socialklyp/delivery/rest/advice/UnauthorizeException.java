package com.github.devlucasjava.socialklyp.delivery.rest.advice;

public class UnauthorizeException extends RuntimeException {
    public UnauthorizeException(String message) {
        super(message);
    }
}