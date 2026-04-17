package com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.upload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

/**
 * Maps the JSON response returned by b2_upload_file.
 * Unknown fields are ignored to keep the record resilient against B2 API additions.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record B2UploadFileResponse(
        String fileId,
        String fileName,
        String fileUrl,
        String accountId,
        String bucketId,
        Long contentLength,
        String contentSha1,
        String contentType
) {
}