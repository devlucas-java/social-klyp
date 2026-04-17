package com.github.devlucasjava.socialklyp.application.dto.response.media;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Media data returned by the API")
@Setter
public class MediaResponse {

    @Schema(description = "Media unique identifier")
    UUID id;

    @Schema(description = "Public URL to access the media")
    String mediaUrl;

    @Schema(description = "Original file name")
    String mediaName;

    @Schema(description = "Media type (IMAGE or VIDEO)")
    String mediaType;

    @Schema(description = "ID of the post this media belongs to")
    UUID postId;

    @Schema(description = "Upload timestamp")
    LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    LocalDateTime updatedAt;
}
