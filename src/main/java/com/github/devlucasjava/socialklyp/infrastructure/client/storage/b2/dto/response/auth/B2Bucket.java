package com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record B2Bucket(
        String id,
        String name
) {}
