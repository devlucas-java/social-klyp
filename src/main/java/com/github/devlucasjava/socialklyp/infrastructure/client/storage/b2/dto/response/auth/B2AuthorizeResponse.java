package com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.auth;

public record B2AuthorizeResponse(
        String accountId,
        ApiInfo apiInfo,
        Long applicationKeyExpirationTimestamp,
        String authorizationToken
) {
}