package com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.auth.B2AuthorizeResponse;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.B2ErrorResponse;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.exception.StorageBadGatewayException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class B2AuthAccountClient {

    private static final Logger log = LoggerFactory.getLogger(B2AuthAccountClient.class);
    private static final String AUTHORIZE_URL = "https://api.backblazeb2.com/b2api/v4/b2_authorize_account";

    private final String keyId;
    private final String key;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final AtomicReference<B2AuthorizeResponse> auth = new AtomicReference<>();

    String getAuthorizationToken() {
        ensureAuthorized();
        return auth.get().authorizationToken();
    }

    String getApiUrl() {
        ensureAuthorized();
        return auth.get().apiInfo().storageApi().apiUrl();
    }

    private void ensureAuthorized() {
        if (auth.get() == null || isExpired()) {
            synchronized (this) {
                if (auth.get() == null || isExpired()) {
                    refreshAuthorize();
                }
            }
        }
    }

    private void refreshAuthorize() {
        log.info("Requesting B2 account authorization");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AUTHORIZE_URL))
                .header("Authorization", buildBasicAuthHeader())
                .GET()
                .build();

        HttpResponse<String> response = sendRequest(request, "authorize_account");
        handleAuthorizeResponse(response);
    }

    private void handleAuthorizeResponse(HttpResponse<String> response) {
        int status = response.statusCode();
        String body = response.body();

        switch (status) {
            case 200 -> {
                B2AuthorizeResponse parsed = parseBody(body, B2AuthorizeResponse.class, "authorize_account");
                auth.set(parsed);
                log.info("B2 account authorization successful");
            }
            case 400 -> {
                B2ErrorResponse error = parseBodySilently(body, B2ErrorResponse.class);
                log.error("B2 authorization bad request. code={} message={}", error.code(), error.message());
                throw new StorageBadGatewayException("B2 bad request: " + error.message());
            }
            case 401 -> {
                log.error("B2 authorization unauthorized — check application key credentials");
                throw new StorageBadGatewayException("B2 unauthorized: invalid credentials");
            }
            case 403 -> {
                log.error("B2 authorization forbidden — key may lack permissions");
                throw new StorageBadGatewayException("B2 forbidden: insufficient permissions");
            }
            case 429 -> {
                log.warn("B2 authorization rate limited");
                throw new StorageBadGatewayException("B2 rate limited, retry later");
            }
            case 500, 503 -> {
                log.error("B2 authorization server error. status={} body={}", status, body);
                throw new StorageBadGatewayException("B2 server error: " + status);
            }
            default -> {
                log.error("B2 authorization unexpected response. status={} body={}", status, body);
                throw new StorageBadGatewayException("B2 unexpected status: " + status);
            }
        }
    }

    private boolean isExpired() {
        B2AuthorizeResponse current = auth.get();
        if (current == null || current.applicationKeyExpirationTimestamp() == null) return true;
        return System.currentTimeMillis() >= current.applicationKeyExpirationTimestamp();
    }

    private String buildBasicAuthHeader() {
        return "Basic " + Base64.getEncoder()
                .encodeToString((keyId + ":" + key).getBytes());
    }

    private HttpResponse<String> sendRequest(HttpRequest request, String operation) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            log.error("IO error during B2 operation. operation={}", operation, e);
            throw new StorageBadGatewayException("B2 IO error on " + operation);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Request interrupted during B2 operation. operation={}", operation, e);
            throw new StorageBadGatewayException("B2 request interrupted on " + operation);
        }
    }

    private <T> T parseBody(String body, Class<T> type, String operation) {
        try {
            return mapper.readValue(body, type);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse B2 response. operation={} body={}", operation, body, e);
            throw new StorageBadGatewayException("Invalid B2 response on " + operation);
        }
    }

    private <T> T parseBodySilently(String body, Class<T> type) {
        try {
            return mapper.readValue(body, type);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse B2 error body. body={}", body);
            return null;
        }
    }
}