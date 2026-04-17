package com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record B2AuthorizeResponse(
        String accountId,
        ApiInfo apiInfo,
        Long applicationKeyExpirationTimestamp,
        String authorizationToken
) {
}