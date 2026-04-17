package com.github.devlucasjava.socialklyp.application.dto.request.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload to create a new post")
public record CreatePostRequest(

        @Schema(description = "Post content", example = "Hello world!")
        @NotBlank(message = "Content must not be blank")
        @Size(max = 1000, message = "Content must not exceed 1000 characters")
        String content
) {}
