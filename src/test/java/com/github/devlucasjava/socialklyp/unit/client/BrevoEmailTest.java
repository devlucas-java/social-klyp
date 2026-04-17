package com.github.devlucasjava.socialklyp.unit.client;

import com.github.devlucasjava.socialklyp.infrastructure.client.port.EmailPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class BrevoEmailTest {

    @Autowired
    private EmailPort emailPort;

    private static final String TEST_EMAIL = "1uc4sm4c3d00@gmail.com";
    private static final String TEST_NAME = "Lucas";

    @Test
    void sendWelcomeEmail_shouldDeliverWithoutErrors() {
        assertDoesNotThrow(() ->
                emailPort.sendWelcome(TEST_EMAIL, TEST_NAME)
        );
    }

    @Test
    void sendVerifyEmail_shouldDeliverWithoutErrors() {
        String fakeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";
        String verifyLink = "https://klyp.app/verify?token=" + fakeToken;

        assertDoesNotThrow(() ->
                emailPort.sendVerifyEmail(TEST_EMAIL, verifyLink)
        );
    }

    @Test
    void sendVerificationCode_shouldDeliverWithoutErrors() {
        assertDoesNotThrow(() ->
                emailPort.sendVerificationCode(TEST_EMAIL, "482951")
        );
    }

    @Test
    void sendVerificationCode_shouldThrowWhenCodeIsTooShort() {
        assertThrows(
                IllegalArgumentException.class,
                () -> emailPort.sendVerificationCode(TEST_EMAIL, "123")
        );
    }

    @Test
    void sendVerificationCode_shouldThrowWhenCodeIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> emailPort.sendVerificationCode(TEST_EMAIL, null)
        );
    }
}