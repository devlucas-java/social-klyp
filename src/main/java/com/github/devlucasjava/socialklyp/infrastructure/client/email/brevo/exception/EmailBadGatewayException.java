package com.github.devlucasjava.socialklyp.infrastructure.client.email.brevo.exception;

public class EmailBadGatewayException extends RuntimeException {
    public EmailBadGatewayException(String message) {
        super(message);
    }
}
