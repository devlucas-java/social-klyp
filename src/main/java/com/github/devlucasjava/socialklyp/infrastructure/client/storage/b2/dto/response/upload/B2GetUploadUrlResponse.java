package com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.upload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record B2GetUploadUrlResponse(
        String bucketId,
        String uploadUrl,
        String authorizationToken
) {
}