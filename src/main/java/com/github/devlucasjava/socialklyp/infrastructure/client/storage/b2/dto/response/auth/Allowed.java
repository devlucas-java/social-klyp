package com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Allowed(
        List<B2Bucket> buckets,
        List<String> capabilities,
        String namePrefix
) {}
