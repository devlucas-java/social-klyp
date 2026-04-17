package com.github.devlucasjava.socialklyp.infrastructure.client.email.brevo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.devlucasjava.socialklyp.infrastructure.client.email.brevo.exception.EmailBadGatewayException;
import com.github.devlucasjava.socialklyp.infrastructure.client.port.EmailPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
public class BrevoEmailAdapter implements EmailPort {

    private static final Logger log = LoggerFactory.getLogger(BrevoEmailAdapter.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public BrevoEmailAdapter(HttpClient httpClient, ObjectMapper mapper) {
        this.httpClient = httpClient;
        this.mapper = mapper;
    }


    @Override
    public void sendWelcome(String toEmail, String toName) {
        log.info("Sending welcome email to={}", toEmail);

        String html = loadTemplate("email-email-welcome.html")
                .replace("{{NAME}}", toName != null ? toName : "there");

        send(toEmail, toName, "Welcome to Klyp 🔥", html);
    }

    @Override
    public void sendVerifyEmail(String toEmail, String verifyLink) {
        log.info("Sending verify email to={}", toEmail);

        String html = loadTemplate("email-verify.html")
                .replace("{{VERIFY_LINK}}", verifyLink);

        send(toEmail, null, "Verify your Klyp email", html);
    }

    @Override
    public void sendVerificationCode(String toEmail, String code) {
        log.info("Sending verification code email to={}", toEmail);

        if (code == null || code.length() != 6) {
            throw new IllegalArgumentException("Code must be exactly 6 characters");
        }

        String html = loadTemplate("email-code.html")
                .replace("{{CODE}}", code)
                .replace("{{D1}}", String.valueOf(code.charAt(0)))
                .replace("{{D2}}", String.valueOf(code.charAt(1)))
                .replace("{{D3}}", String.valueOf(code.charAt(2)))
                .replace("{{D4}}", String.valueOf(code.charAt(3)))
                .replace("{{D5}}", String.valueOf(code.charAt(4)))
                .replace("{{D6}}", String.valueOf(code.charAt(5)));

        send(toEmail, null, "Your Klyp verification code", html);
    }


    private void send(String toEmail, String toName, String subject, String htmlContent) {
        String body = buildBody(toEmail, toName, subject, htmlContent);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BREVO_API_URL))
                .header("api-key", apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();

            if (status == 201 || status == 200) {
                log.info("Email sent successfully via Brevo. to={} subject={}", toEmail, subject);
            } else {
                log.error("Brevo returned error. status={} body={}", status, response.body());
                throw new EmailBadGatewayException("Failed to send email via Brevo. status=" + status);
            }

        } catch (IOException e) {
            log.error("IO error sending email via Brevo. to={}", toEmail, e);
            throw new EmailBadGatewayException("IO error sending email");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while sending email via Brevo. to={}", toEmail, e);
            throw new EmailBadGatewayException("Interrupted while sending email");
        }
    }

    private String buildBody(String toEmail, String toName, String subject, String htmlContent) {
        try {
            // Escape the HTML for JSON
            String escapedHtml = mapper.writeValueAsString(htmlContent);
            // Remove surrounding quotes added by ObjectMapper
            escapedHtml = escapedHtml.substring(1, escapedHtml.length() - 1);

            String toNameSafe = toName != null ? toName : toEmail;

            return String.format("""
                            {
                              "sender": { "name": "%s", "email": "%s" },
                              "to": [ { "email": "%s", "name": "%s" } ],
                              "subject": "%s",
                              "htmlContent": "%s"
                            }
                            """,
                    senderName, senderEmail,
                    toEmail, toNameSafe,
                    subject,
                    escapedHtml
            );
        } catch (Exception e) {
            throw new EmailBadGatewayException("Failed to build email body");
        }
    }

    private String loadTemplate(String fileName) {
        try {
            var stream = getClass().getResourceAsStream("/templates/email/" + fileName);
            if (stream == null) {
                throw new EmailBadGatewayException("Email template not found: " + fileName);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load email template. file={}", fileName, e);
            throw new EmailBadGatewayException("Failed to load email template: " + fileName);
        }
    }
}