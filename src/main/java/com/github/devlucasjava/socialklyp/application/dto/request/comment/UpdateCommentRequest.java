package com.github.devlucasjava.socialklyp.application.dto.request.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload to update an existing comment")
public record UpdateCommentRequest(

        @Schema(description = "Updated comment content", example = "Actually, I disagree!")
        @NotBlank(message = "Content must not be blank")
        @Size(max = 512, message = "Content must not exceed 512 characters")
        String content
) {}
