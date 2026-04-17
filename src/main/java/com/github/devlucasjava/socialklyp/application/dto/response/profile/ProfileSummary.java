package com.github.devlucasjava.socialklyp.application.dto.response.profile;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Minimal profile info for comment authorship")
public record ProfileSummary(
        UUID id,
        String displayName,
        String profilePictureUrl
) {}
