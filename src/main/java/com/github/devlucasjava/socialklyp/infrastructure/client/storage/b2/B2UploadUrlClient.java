package com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.B2ErrorResponse;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.upload.B2GetUploadUrlResponse;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.exception.StorageBadGatewayException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


@RequiredArgsConstructor
class B2UploadUrlClient {

    private static final Logger log = LoggerFactory.getLogger(B2UploadUrlClient.class);
    private static final String ENDPOINT = "/b2api/v4/b2_get_upload_url";

    private final String bucketId;
    private final B2AuthAccountClient authClient;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    B2GetUploadUrlResponse fetchUploadUrl() {
        log.info("Fetching B2 upload URL. bucketId={}", bucketId);

        String token  = authClient.getAuthorizationToken();
        String apiUrl = authClient.getApiUrl();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + ENDPOINT + "?bucketId=" + bucketId))
                .header("Authorization", token)
                .GET()
                .build();

        HttpResponse<String> response = sendRequest(request);
        return handleResponse(response);
    }


    private B2GetUploadUrlResponse handleResponse(HttpResponse<String> response) {
        int    status = response.statusCode();
        String body   = response.body();

        return switch (status) {
            case 200 -> {
                B2GetUploadUrlResponse parsed = parseBody(body);
                log.info("B2 upload URL fetched successfully. uploadUrl={}", parsed.uploadUrl());
                yield parsed;
            }
            case 400 -> {
                B2ErrorResponse error = parseErrorSilently(body);
                log.error("B2 get_upload_url bad request. code={} message={}",
                        error != null ? error.code() : "unknown",
                        error != null ? error.message() : body);
                throw new StorageBadGatewayException("B2 bad request on get_upload_url");
            }
            case 401 -> {
                log.warn("B2 get_upload_url unauthorized — token may have expired");
                throw new StorageBadGatewayException("B2 unauthorized on get_upload_url");
            }
            case 403 -> {
                log.error("B2 get_upload_url forbidden. bucketId={}", bucketId);
                throw new StorageBadGatewayException("B2 forbidden on get_upload_url");
            }
            case 429 -> {
                log.warn("B2 get_upload_url rate limited");
                throw new StorageBadGatewayException("B2 rate limited on get_upload_url");
            }
            case 500, 503 -> {
                log.error("B2 get_upload_url server error. status={} body={}", status, body);
                throw new StorageBadGatewayException("B2 server error on get_upload_url: " + status);
            }
            default -> {
                log.error("B2 get_upload_url unexpected response. status={} body={}", status, body);
                throw new StorageBadGatewayException("B2 unexpected status on get_upload_url: " + status);
            }
        };
    }


    private HttpResponse<String> sendRequest(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            log.error("IO error fetching B2 upload URL", e);
            throw new StorageBadGatewayException("B2 IO error on get_upload_url");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Request interrupted fetching B2 upload URL", e);
            throw new StorageBadGatewayException("B2 request interrupted on get_upload_url");
        }
    }

    private B2GetUploadUrlResponse parseBody(String body) {
        try {
            return mapper.readValue(body, B2GetUploadUrlResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse B2 upload URL response. body={}", body, e);
            throw new StorageBadGatewayException("Invalid B2 response on get_upload_url");
        }
    }

    private B2ErrorResponse parseErrorSilently(String body) {
        try {
            return mapper.readValue(body, B2ErrorResponse.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse B2 error body. body={}", body);
            return null;
        }
    }
}