package com.github.devlucasjava.socialklyp.delivery.rest.advice;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
