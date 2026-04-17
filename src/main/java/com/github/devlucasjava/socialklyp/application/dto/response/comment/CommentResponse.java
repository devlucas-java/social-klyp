package com.github.devlucasjava.socialklyp.application.dto.response.comment;

import com.github.devlucasjava.socialklyp.application.dto.response.profile.ProfileSummary;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Comment data returned by the API")
public record CommentResponse(

        @Schema(description = "Comment unique identifier")
        UUID id,

        @Schema(description = "Comment content")
        String content,

        @Schema(description = "Profile who wrote the comment")
        ProfileSummary profile,

        @Schema(description = "Comment creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Comment last update timestamp")
        LocalDateTime updatedAt
) {
}
