package com.github.devlucasjava.socialklyp.application.dto.response.like;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Like data returned by the API")
public record LikeResponse(

        @Schema(description = "Like unique identifier")
        UUID id,

        @Schema(description = "ID of the profile who liked the post")
        UUID profileId,

        @Schema(description = "ID of the liked post")
        UUID postId
) {}
