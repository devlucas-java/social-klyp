package com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.auth;

public record StorageApi(

    long absoluteMinimumPartSize,
    Allowed allowed,
    String apiUrl,
    String downloadUrl,
    long recommendedPartSize,
    String s3ApiUrl
    ) {
}