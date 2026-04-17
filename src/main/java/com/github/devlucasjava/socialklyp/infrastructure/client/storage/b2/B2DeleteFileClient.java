package com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.B2ErrorResponse;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.delete.B2DeleteResponse;
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
public class B2DeleteFileClient {

    private static final Logger log = LoggerFactory.getLogger(B2DeleteFileClient.class);
    private static final String ENDPOINT = "/b2api/v4/b2_delete_file_version";

    private final B2AuthAccountClient authClient;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;


    B2DeleteResponse deleteFile(String fileId, String fileName) {
        log.info("Requesting B2 file deletion. fileId={} fileName={}", fileId, fileName);

        String token = authClient.getAuthorizationToken();
        String apiUrl = authClient.getApiUrl();

        String requestBody = buildRequestBody(fileId, fileName);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + ENDPOINT))
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = sendRequest(request, fileId, fileName);
        return handleResponse(response, fileId, fileName);
    }


    private B2DeleteResponse handleResponse(HttpResponse<String> response, String fileId, String fileName) {
        int status = response.statusCode();
        String body = response.body();

        return switch (status) {
            case 200 -> {
                B2DeleteResponse parsed = parseBody(body);
                log.info("File deleted successfully from B2. fileId={} fileName={}", parsed.fileId(), parsed.fileName());
                yield parsed;
            }
            case 400 -> {
                B2ErrorResponse error = parseErrorSilently(body);
                log.error("B2 delete_file_version bad request. fileId={} code={} message={}",
                        fileId,
                        error != null ? error.code() : "unknown",
                        error != null ? error.message() : body);
                throw new StorageBadGatewayException("B2 bad request on delete_file_version");
            }
            case 401 -> {
                log.warn("B2 delete_file_version unauthorized — token may have expired. fileId={}", fileId);
                throw new StorageBadGatewayException("B2 unauthorized on delete_file_version");
            }
            case 403 -> {
                log.error("B2 delete_file_version forbidden — key may lack deleteFiles capability. fileId={}", fileId);
                throw new StorageBadGatewayException("B2 forbidden on delete_file_version");
            }
            case 429 -> {
                log.warn("B2 delete_file_version rate limited. fileId={}", fileId);
                throw new StorageBadGatewayException("B2 rate limited on delete_file_version");
            }
            case 500, 503 -> {
                log.error("B2 delete_file_version server error. status={} fileId={} body={}", status, fileId, body);
                throw new StorageBadGatewayException("B2 server error on delete_file_version: " + status);
            }
            default -> {
                log.error("B2 delete_file_version unexpected response. status={} fileId={} body={}",
                        status, fileId, body);
                throw new StorageBadGatewayException("B2 unexpected status on delete_file_version: " + status);
            }
        };
    }


    private HttpResponse<String> sendRequest(HttpRequest request, String fileId, String fileName) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            log.error("IO error deleting file from B2. fileId={} fileName={}", fileId, fileName, e);
            throw new StorageBadGatewayException("B2 IO error on delete_file_version");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Request interrupted deleting file from B2. fileId={} fileName={}", fileId, fileName, e);
            throw new StorageBadGatewayException("B2 request interrupted on delete_file_version");
        }
    }

    private String buildRequestBody(String fileId, String fileName) {
        return String.format("{\"fileId\":\"%s\",\"fileName\":\"%s\"}", fileId, fileName);
    }

    private B2DeleteResponse parseBody(String body) {
        try {
            return mapper.readValue(body, B2DeleteResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse B2 delete_file_version response. body={}", body, e);
            throw new StorageBadGatewayException("Invalid B2 response on delete_file_version");
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