package com.github.devlucasjava.socialklyp.application.dto.request.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload to create a comment on a post")
public record CreateCommentRequest(

        @Schema(description = "Comment content", example = "Great post!")
        @NotBlank(message = "Content must not be blank")
        @Size(max = 512, message = "Content must not exceed 512 characters")
        String content
) {}
