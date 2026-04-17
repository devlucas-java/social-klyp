package com.github.devlucasjava.socialklyp.application.dto.response.post;

import com.github.devlucasjava.socialklyp.application.dto.response.media.MediaResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Post data returned by the API")
public record PostResponse(

        @Schema(description = "Post unique identifier")
        UUID id,

        @Schema(description = "Post content")
        String content,

        @Schema(description = "First media of the post (thumbnail)")
        MediaResponse media,

        @Schema(description = "Post creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Post last update timestamp")
        LocalDateTime updatedAt
) {
}