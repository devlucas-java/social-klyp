package com.github.devlucasjava.socialklyp.delivery.rest.advice;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
