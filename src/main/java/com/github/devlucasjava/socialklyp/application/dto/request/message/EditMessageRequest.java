package com.github.devlucasjava.socialklyp.application.dto.request.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload to edit an existing message")
public record EditMessageRequest(

        @Schema(description = "Updated message content", example = "Actually, I meant this!")
        @NotBlank(message = "Content must not be blank")
        @Size(max = 2000, message = "Message must not exceed 2000 characters")
        String content
) {}
