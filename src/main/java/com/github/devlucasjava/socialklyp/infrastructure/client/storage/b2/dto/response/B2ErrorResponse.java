package com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response;

public record B2ErrorResponse(
        String status,
        String code,
        String message
) {
}
