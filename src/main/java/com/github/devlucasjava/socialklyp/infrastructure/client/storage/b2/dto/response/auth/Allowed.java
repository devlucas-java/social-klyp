package com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.auth;

import java.util.List;

public record Allowed(
    List<String> buckets,
    List<String> capabilities,
    String namePrefix
    ) {
}
