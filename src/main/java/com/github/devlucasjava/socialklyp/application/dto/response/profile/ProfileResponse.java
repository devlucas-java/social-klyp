package com.github.devlucasjava.socialklyp.application.dto.response.profile;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Profile data returned by the API")
public record ProfileResponse(

        @Schema(description = "Profile unique identifier")
        UUID id,

        @Schema(description = "Display name of the profile")
        String displayName,

        @Schema(description = "Profile bio")
        String bio,

        @Schema(description = "URL of the profile picture")
        String profilePictureUrl,

        @Schema(description = "Whether the profile is private")
        boolean isPrivate,

        @Schema(description = "ID of the associated user")
        UUID userId
) {}
