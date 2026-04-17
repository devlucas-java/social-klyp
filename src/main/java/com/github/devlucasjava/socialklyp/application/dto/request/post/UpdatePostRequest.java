package com.github.devlucasjava.socialklyp.application.dto.request.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload to update an existing post")
public record UpdatePostRequest(

        @Schema(description = "Updated post content", example = "Updated content here")
        @NotBlank(message = "Content must not be blank")
        @Size(max = 1000, message = "Content must not exceed 1000 characters")
        String content
) {}
