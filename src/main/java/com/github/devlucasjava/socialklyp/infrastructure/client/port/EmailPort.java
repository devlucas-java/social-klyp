package com.github.devlucasjava.socialklyp.infrastructure.client.port;

public interface EmailPort {
    void sendWelcome(String toEmail, String toName);
    void sendVerifyEmail(String toEmail, String verifyLink);
    void sendVerificationCode(String toEmail, String code);
}