package com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.B2ErrorResponse;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.upload.B2GetUploadUrlResponse;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.upload.B2UploadFileResponse;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.exception.StorageBadGatewayException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


@RequiredArgsConstructor
public class B2UploadFileClient {

    private static final Logger log = LoggerFactory.getLogger(B2UploadFileClient.class);

    private final HttpClient httpClient;
    private final ObjectMapper mapper;


    B2UploadFileResponse uploadFile(
            B2GetUploadUrlResponse uploadUrlResponse,
            byte[] fileData,
            String fileName,
            String contentType
    ) {
        log.info("Uploading file to B2. fileName={} contentType={} sizeBytes={}",
                fileName, contentType, fileData.length);

        log.info(uploadUrlResponse.authorizationToken());
        log.info(uploadUrlResponse.uploadUrl());

        String sha1 = computeSha1(fileData);
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrlResponse.uploadUrl()))
                .header("Authorization", uploadUrlResponse.authorizationToken())
                .header("X-Bz-File-Name", encodedFileName)
                .header("Content-Type", contentType)
                //.header("Content-Length",  String.valueOf(fileData.length))
                .header("X-Bz-Content-Sha1", sha1)
                .POST(HttpRequest.BodyPublishers.ofByteArray(fileData))
                .build();


        HttpResponse<String> response = sendRequest(request, fileName);
        return handleResponse(response, fileName);
    }


    private B2UploadFileResponse handleResponse(HttpResponse<String> response, String fileName) {
        int status = response.statusCode();
        String body = response.body();

        return switch (status) {
            case 200 -> {
                B2UploadFileResponse parsed = parseBody(body);
                log.info("File uploaded successfully to B2. fileId={} fileName={}", parsed.fileId(), parsed.fileName());
                yield parsed;
            }
            case 400 -> {
                B2ErrorResponse error = parseErrorSilently(body);
                log.error("B2 upload_file bad request. fileName={} code={} message={}",
                        fileName,
                        error != null ? error.code() : "unknown",
                        error != null ? error.message() : body);
                throw new StorageBadGatewayException("B2 bad request on upload_file");
            }
            case 401 -> {
                log.warn("B2 upload_file unauthorized — upload token may have expired. fileName={}", fileName);
                throw new StorageBadGatewayException("B2 unauthorized on upload_file");
            }
            case 403 -> {
                log.error("B2 upload_file forbidden. fileName={}", fileName);
                throw new StorageBadGatewayException("B2 forbidden on upload_file");
            }
            case 408 -> {
                log.warn("B2 upload_file request timeout. fileName={}", fileName);
                throw new StorageBadGatewayException("B2 request timeout on upload_file");
            }
            case 429 -> {
                log.warn("B2 upload_file rate limited. fileName={}", fileName);
                throw new StorageBadGatewayException("B2 rate limited on upload_file");
            }
            case 500, 503 -> {
                log.error("B2 upload_file server error. status={} fileName={} body={}", status, fileName, body);
                throw new StorageBadGatewayException("B2 server error on upload_file: " + status);
            }
            default -> {
                log.error("B2 upload_file unexpected response. status={} fileName={} body={}", status, fileName, body);
                throw new StorageBadGatewayException("B2 unexpected status on upload_file: " + status);
            }
        };
    }


    private HttpResponse<String> sendRequest(HttpRequest request, String fileName) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            log.error("IO error uploading file to B2. fileName={}", fileName, e);
            throw new StorageBadGatewayException("B2 IO error on upload_file");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Request interrupted uploading file to B2. fileName={}", fileName, e);
            throw new StorageBadGatewayException("B2 request interrupted on upload_file");
        }
    }

    private B2UploadFileResponse parseBody(String body) {
        try {
            return mapper.readValue(body, B2UploadFileResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse B2 upload_file response. body={}", body, e);
            throw new StorageBadGatewayException("Invalid B2 response on upload_file");
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


    private String computeSha1(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = md.digest(input);
            StringBuilder hex = new StringBuilder(40);
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-1 is guaranteed by the JVM spec — this should never happen
            throw new IllegalStateException("SHA-1 algorithm not available", e);
        }
    }
}